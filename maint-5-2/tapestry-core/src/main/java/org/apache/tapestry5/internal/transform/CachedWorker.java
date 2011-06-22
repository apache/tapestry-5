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
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;

/**
 * Caches method return values for methods annotated with {@link Cached}.
 */
@SuppressWarnings("all")
public class CachedWorker implements ComponentClassTransformWorker
{
    private final BindingSource bindingSource;

    private final PerthreadManager perThreadManager;

    interface MethodResultCacheFactory
    {
        MethodResultCache create(Component instance);
    }

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

    public CachedWorker(BindingSource bindingSource, PerthreadManager perthreadManager)
    {
        this.bindingSource = bindingSource;
        this.perThreadManager = perthreadManager;
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
        // The key needs to reflect not just the method name, but also the containing
        // page and component (otherwise, there would be unwanted sharing of cache
        // between different instances of the same component within or across pages). This
        // name can't be calculated until page instantiation time.

        FieldAccess fieldAccess = createPerThreadValueField(transformation, method);

        Cached annotation = method.getAnnotation(Cached.class);

        MethodResultCacheFactory factory = createFactory(transformation, annotation.watch(), method);

        ComponentMethodAdvice advice = createAdvice(fieldAccess, factory);

        method.addAdvice(advice);
    }

    private FieldAccess createPerThreadValueField(ClassTransformation transformation, TransformMethod method)
    {
        TransformField field = transformation.createField(Modifier.PROTECTED, PerThreadValue.class.getName(),
                "perThreadMethodCache$" + method.getName());

        // Each instance of the component will get a new PerThreadValue.
        field.injectIndirect(new ComponentValueProvider<PerThreadValue<MethodResultCache>>()
        {
            public PerThreadValue<MethodResultCache> get(ComponentResources resources)
            {
                return perThreadManager.createValue();
            }
        });

        return field.getAccess();
    }

    private ComponentMethodAdvice createAdvice(final FieldAccess perThreadValueAccess,
            final MethodResultCacheFactory factory)
    {
        return new ComponentMethodAdvice()
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
                Component instance = invocation.getInstance();

                PerThreadValue<MethodResultCache> value = (PerThreadValue<MethodResultCache>) perThreadValueAccess
                        .read(instance);

                if (value.exists())
                    return value.get();

                return value.set(factory.create(instance));
            }
        };
    }

    private MethodResultCacheFactory createFactory(ClassTransformation transformation, final String watch,
            TransformMethod method)
    {
        if (watch.equals(""))
            return new MethodResultCacheFactory()
            {
                public MethodResultCache create(Component instance)
                {
                    return new SimpleMethodResultCache();
                }
            };

        // Each component instance will get its own Binding instance. That handles both different locales,
        // and reuse of a component (with a cached method) within a page or across pages.

        TransformField bindingField = transformation.createField(Modifier.PROTECTED, Binding.class.getName(),
                "cache$watchBinding$" + method.getName());

        final FieldAccess bindingAccess = bindingField.getAccess();

        transformation.getOrCreateMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE).addAdvice(
                new ComponentMethodAdvice()
                {
                    public void advise(ComponentMethodInvocation invocation)
                    {
                        Binding binding = bindingSource.newBinding("@Cached watch", invocation.getComponentResources(),
                                BindingConstants.PROP, watch);

                        bindingAccess.write(invocation.getInstance(), binding);

                        invocation.proceed();
                    }
                });

        return new MethodResultCacheFactory()
        {
            public MethodResultCache create(Component instance)
            {
                Binding binding = (Binding) bindingAccess.read(instance);

                return new WatchedBindingMethodResultCache(binding);
            }
        };
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
