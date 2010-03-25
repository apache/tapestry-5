// Copyright 2008, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.transform;

import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.PageLifecycleAdapter;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.FieldAccess;
import org.apache.tapestry5.services.TransformField;
import org.apache.tapestry5.services.TransformMethod;
import org.apache.tapestry5.services.TransformMethodSignature;

/**
 * Caches method return values for methods annotated with {@link Cached}.
 */
public class CachedWorker implements ComponentClassTransformWorker
{
    private final BindingSource bindingSource;

    /**
     * Handles the watching of a binding (usually a property or property expression), invalidating the
     * cache early if the watched binding's value changes.
     */
    private class SimpleMethodResultCache implements MethodResultCache
    {
        private boolean cached;
        private Object cachedValue;

        public void set(Object cachedValue)
        {
            cached = true;
            this.cachedValue = cachedValue;
        }

        public void reset()
        {
            cached = false;
            cachedValue = null;
        }

        public boolean isCached()
        {
            return cached;
        }

        public Object get()
        {
            return cachedValue;
        }
    }

    private class WatchedBindingMethodResultCache extends SimpleMethodResultCache
    {
        private final Binding binding;

        private Object cachedBindingValue;

        public WatchedBindingMethodResultCache(Binding binding)
        {
            this.binding = binding;
        }

        @Override
        public boolean isCached()
        {
            Object currentBindingValue = binding.get();

            if (!TapestryInternalUtils.isEqual(cachedBindingValue, currentBindingValue))
            {
                reset();

                cachedBindingValue = currentBindingValue;
            }

            return super.isCached();
        }
    }

    public CachedWorker(BindingSource bindingSource)
    {
        this.bindingSource = bindingSource;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<TransformMethod> methods = transformation.matchMethodsWithAnnotation(Cached.class);

        for (TransformMethod method : methods)
        {
            validateMethod(method);

            adviseMethod(transformation, method);
        }
    }

    private void adviseMethod(ClassTransformation transformation, TransformMethod method)
    {
        FieldAccess resultCacheAccess = createMethodResultCacheField(transformation, method);

        Cached annotation = method.getAnnotation(Cached.class);

        ComponentMethodAdvice advice = createAdvice(resultCacheAccess, annotation.watch());

        method.addAdvice(advice);
    }

    private FieldAccess createMethodResultCacheField(ClassTransformation transformation, TransformMethod method)
    {
        TransformField resultCacheField = transformation.createField(Modifier.PRIVATE, MethodResultCache.class
                .getName(), "cache$" + method.getName());

        return resultCacheField.getAccess();
    }

    private ComponentMethodAdvice createAdvice(final FieldAccess resultCacheAccess, final String watch)
    {
        ComponentMethodAdvice advice = new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                MethodResultCache cache = getOrCreateCache(invocation);

                if (cache.isCached())
                {
                    invocation.overrideResult(cache.get());
                    return;
                }

                invocation.proceed();

                invocation.rethrow();

                cache.set(invocation.getResult());
            }

            private MethodResultCache getOrCreateCache(ComponentMethodInvocation invocation)
            {
                MethodResultCache cache = (MethodResultCache) resultCacheAccess.read(invocation.getInstance());

                if (cache == null)
                    cache = createAndStoreCache(invocation);

                return cache;
            }

            private MethodResultCache createAndStoreCache(ComponentMethodInvocation invocation)
            {
                final MethodResultCache cache = createMethodResultCache(invocation.getComponentResources());

                invocation.getComponentResources().addPageLifecycleListener(new PageLifecycleAdapter()
                {
                    @Override
                    public void containingPageDidDetach()
                    {
                        cache.reset();
                    }
                });

                resultCacheAccess.write(invocation.getInstance(), cache);

                return cache;
            }

            private SimpleMethodResultCache createMethodResultCache(ComponentResources resources)
            {
                if (watch.equals(""))
                    return new SimpleMethodResultCache();

                Binding binding = bindingSource.newBinding("@Cached watch", resources, BindingConstants.PROP, watch);

                return new WatchedBindingMethodResultCache(binding);
            }
        };

        return advice;
    }

    private void validateMethod(TransformMethod method)
    {
        TransformMethodSignature signature = method.getSignature();

        if (signature.getReturnType().equals("void"))
            throw new IllegalArgumentException(String.format(
                    "Method %s may not be used with @Cached because it returns void.", method.getMethodIdentifier()));

        if (signature.getParameterTypes().length != 0)
            throw new IllegalArgumentException(String.format(
                    "Method %s may not be used with @Cached because it has parameters.", method.getMethodIdentifier()));
    }
}
