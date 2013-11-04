// Copyright 2008, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import java.util.List;

/**
 * Caches method return values for methods annotated with {@link Cached}.
 */
@SuppressWarnings("all")
public class CachedWorker implements ComponentClassTransformWorker2
{
    private final BindingSource bindingSource;

    private final PerthreadManager perThreadManager;

    interface MethodResultCacheFactory
    {
        MethodResultCache create(Object instance);
    }


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

    /**
     * When there is no watch, all cached methods look the same.
     */
    private final MethodResultCacheFactory nonWatchFactory = new MethodResultCacheFactory()
    {
        public MethodResultCache create(Object instance)
        {
            return new SimpleMethodResultCache();
        }
    };

    /**
     * Handles the watching of a binding (usually a property or property expression), invalidating the
     * cache early if the watched binding's value changes.
     */
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


    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        List<PlasticMethod> methods = plasticClass.getMethodsWithAnnotation(Cached.class);

        for (PlasticMethod method : methods)
        {
            validateMethod(method);

            adviseMethod(plasticClass, method);
        }
    }

    private void adviseMethod(PlasticClass plasticClass, PlasticMethod method)
    {
        // Every instance of the clas srequires its own per-thread value. This handles the case of multiple
        // pages containing the component, or the same page containing the component multiple times.

        PlasticField cacheField =
                plasticClass.introduceField(PerThreadValue.class, "cache$" + method.getDescription().methodName);

        cacheField.injectComputed(new ComputedValue<PerThreadValue>()
        {
            public PerThreadValue get(InstanceContext context)
            {
                // Each instance will get a new PerThreadValue
                return perThreadManager.createValue();
            }
        });

        Cached annotation = method.getAnnotation(Cached.class);

        MethodResultCacheFactory factory = createFactory(plasticClass, annotation.watch(), method);

        MethodAdvice advice = createAdvice(cacheField, factory);

        method.addAdvice(advice);
    }


    private MethodAdvice createAdvice(PlasticField cacheField,
                                      final MethodResultCacheFactory factory)
    {
        final FieldHandle fieldHandle = cacheField.getHandle();

        return new MethodAdvice()
        {
            public void advise(MethodInvocation invocation)
            {
                MethodResultCache cache = getOrCreateCache(invocation);

                if (cache.isCached())
                {
                    invocation.setReturnValue(cache.get());
                    return;
                }

                invocation.proceed();

                if(!invocation.didThrowCheckedException())
                {
                    cache.set(invocation.getReturnValue());
                }
            }

            private MethodResultCache getOrCreateCache(MethodInvocation invocation)
            {
                Object instance = invocation.getInstance();

                // The PerThreadValue is created in the instance constructor.

                PerThreadValue<MethodResultCache> value = (PerThreadValue<MethodResultCache>) fieldHandle
                        .get(instance);

                // But it will be empty when first created, or at the start of a new request.
                if (value.exists())
                {
                    return value.get();
                }

                // Use the factory to create a MethodResultCache for the combination of instance, method, and thread.

                return value.set(factory.create(instance));
            }
        };
    }


    private MethodResultCacheFactory createFactory(PlasticClass plasticClass, final String watch,
                                                   PlasticMethod method)
    {
        // When there's no watch, a shared factory that just returns a new SimpleMethodResultCache
        // will suffice.
        if (watch.equals(""))
        {
            return nonWatchFactory;
        }

        // Because of the watch, its necessary to create a factory for instances of this component and method.

        final FieldHandle bindingFieldHandle = plasticClass.introduceField(Binding.class, "cache$watchBinding$" + method.getDescription().methodName).getHandle();


        // Each component instance will get its own Binding instance. That handles both different locales,
        // and reuse of a component (with a cached method) within a page or across pages. However, the binding can't be initialized
        // until the page loads.

        plasticClass.introduceInterface(PageLifecycleListener.class);
        plasticClass.introduceMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_DESCRIPTION).addAdvice(new MethodAdvice()
        {
            public void advise(MethodInvocation invocation)
            {
                ComponentResources resources = invocation.getInstanceContext().get(ComponentResources.class);

                Binding binding = bindingSource.newBinding("@Cached watch", resources,
                        BindingConstants.PROP, watch);

                bindingFieldHandle.set(invocation.getInstance(), binding);

                invocation.proceed();
            }
        });

        return new MethodResultCacheFactory()
        {
            public MethodResultCache create(Object instance)
            {
                Binding binding = (Binding) bindingFieldHandle.get(instance);

                return new WatchedBindingMethodResultCache(binding);
            }
        };
    }

    private void validateMethod(PlasticMethod method)
    {
        MethodDescription description = method.getDescription();

        if (description.returnType.equals("void"))
            throw new IllegalArgumentException(String.format(
                    "Method %s may not be used with @Cached because it returns void.", method.getMethodIdentifier()));

        if (description.argumentTypes.length != 0)
            throw new IllegalArgumentException(String.format(
                    "Method %s may not be used with @Cached because it has parameters.", method.getMethodIdentifier()));
    }
}
