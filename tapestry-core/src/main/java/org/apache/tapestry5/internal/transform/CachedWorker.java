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
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.plastic.PlasticUtils.FieldInfo;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Caches method return values for methods annotated with {@link Cached}.
 */
@SuppressWarnings("all")
public class CachedWorker implements ComponentClassTransformWorker2
{
    private static final String WATCH_BINDING_PREFIX = "cache$watchBinding$";

    private static final String FIELD_PREFIX = "cache$";
    
    private static final String META_PROPERTY = "cachedWorker";

    private static final String MODIFIERS = "modifiers";
    
    private static final String RETURN_TYPE = "returnType";
    
    private static final String NAME = "name";
    
    private static final String GENERIC_SIGNATURE = "genericSignature";
    
    private static final String ARGUMENT_TYPES = "argumentTypes";
    
    private static final String CHECKED_EXCEPTION_TYPES = "checkedExceptionTypes";
    
    private static final String WATCH = "watch";

    private final BindingSource bindingSource;

    private final PerthreadManager perThreadManager;
    
    private final PropertyValueProviderWorker propertyValueProviderWorker;
    
    private final boolean multipleClassLoaders;

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

    public CachedWorker(BindingSource bindingSource, PerthreadManager perthreadManager,
            PropertyValueProviderWorker propertyValueProviderWorker,
            @Inject @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode,
            @Inject @Symbol(SymbolConstants.MULTIPLE_CLASSLOADERS) boolean multipleClassloaders)
    {
        this.bindingSource = bindingSource;
        this.perThreadManager = perthreadManager;
        this.propertyValueProviderWorker = propertyValueProviderWorker;
        this.multipleClassLoaders = !productionMode && multipleClassloaders;
    }


    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        final List<PlasticMethod> methods = plasticClass.getMethodsWithAnnotation(Cached.class);
        final Set<PlasticUtils.FieldInfo> fieldInfos = multipleClassLoaders ? new HashSet<>() : null;
        final Map<String, String> extraMethodCachedWatchMap = multipleClassLoaders ? new HashMap<>() : null;
        
        if (multipleClassLoaders)
        {
            
            // Store @Cache-annotated methods information so subclasses can 
            // know about them.
            
            model.setMeta(META_PROPERTY, toJSONArray(methods).toCompactString());
            
            // Use the information from superclasses
            
            ComponentModel parentModel = model.getParentModel();
            Set<PlasticMethod> extraMethods = new HashSet<>();
            while (parentModel != null)
            {
                extraMethods.addAll(
                        toPlasticMethodList(
                                parentModel.getMeta(META_PROPERTY), plasticClass, extraMethodCachedWatchMap));
                parentModel = parentModel.getParentModel();
            }
            
            methods.addAll(extraMethods);
            
        }

        for (PlasticMethod method : methods)
        {
            validateMethod(method);

            adviseMethod(plasticClass, method, fieldInfos, model, extraMethodCachedWatchMap);
        }
        
        if (multipleClassLoaders && !fieldInfos.isEmpty())
        {
            this.propertyValueProviderWorker.add(plasticClass, fieldInfos);
        }        
    }
    
    private Collection<PlasticMethod> toPlasticMethodList(String meta, PlasticClass plasticClass,
            Map<String, String> extraMethodCachedWatchMap) 
    {
        final JSONArray array = new JSONArray(meta);
        List<PlasticMethod> methods = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++)
        {
            final JSONObject jsonObject = array.getJSONObject(i);
            final PlasticMethod plasticMethod = toPlasticMethod(jsonObject, plasticClass, extraMethodCachedWatchMap);
            if (plasticMethod != null)
            {
                methods.add(plasticMethod);
            }
        }
        return methods;
    }


    private static PlasticMethod toPlasticMethod(JSONObject jsonObject, PlasticClass plasticClass,
            Map<String, String> extraMethodCachedWatchMap) 
    {
        final int modifiers = jsonObject.getInt(MODIFIERS);
        
        // We cannot override final methods
        if (Modifier.isFinal(modifiers)) 
        {
            return null;
        }
        
        final String returnType = jsonObject.getString(RETURN_TYPE);
        final String methodName = jsonObject.getString(NAME);
        final String genericSignature = jsonObject.getStringOrDefault(GENERIC_SIGNATURE, null);
        final JSONArray argumentTypesArray = jsonObject.getJSONArray(ARGUMENT_TYPES);
        final String[] argumentTypes = argumentTypesArray.stream()
                .collect(Collectors.toList()).toArray(new String[argumentTypesArray.size()]);
        final JSONArray checkedExceptionTypesArray = jsonObject.getJSONArray(CHECKED_EXCEPTION_TYPES);
        final String[] checkedExceptionTypes = checkedExceptionTypesArray.stream()
                .collect(Collectors.toList()).toArray(new String[checkedExceptionTypesArray.size()]);
        
        if (!extraMethodCachedWatchMap.containsKey(methodName))
        {
            extraMethodCachedWatchMap.put(methodName, jsonObject.getString(WATCH));
        }
        
        return plasticClass.introduceMethod(new MethodDescription(
                modifiers, returnType, methodName, argumentTypes, 
                genericSignature, checkedExceptionTypes));
    }

    private static JSONArray toJSONArray(List<PlasticMethod> methods)
    {
        final JSONArray array = new JSONArray();
        for (PlasticMethod method : methods) 
        {
            array.add(toJSONObject(method));
        }
        return array;
    }

    private static JSONObject toJSONObject(PlasticMethod method) 
    {
        final MethodDescription description = method.getDescription();
        
        return new JSONObject(
                MODIFIERS, description.modifiers,
                RETURN_TYPE, description.returnType,
                NAME, description.methodName,
                GENERIC_SIGNATURE, description.genericSignature,
                ARGUMENT_TYPES, new JSONArray(description.argumentTypes),
                CHECKED_EXCEPTION_TYPES, new JSONArray(description.checkedExceptionTypes),
                WATCH, method.getAnnotation(Cached.class).watch());
    }

    private void adviseMethod(PlasticClass plasticClass, PlasticMethod method, Set<FieldInfo> fieldInfos,
            MutableComponentModel model, Map<String, String> extraMethodCachedWatchMap)
    {
        // Every instance of the class requires its own per-thread value. This handles the case of multiple
        // pages containing the component, or the same page containing the component multiple times.

        PlasticField cacheField =
                plasticClass.introduceField(PerThreadValue.class, getFieldName(method));

        cacheField.injectComputed(new ComputedValue<PerThreadValue>()
        {
            public PerThreadValue get(InstanceContext context)
            {
                // Each instance will get a new PerThreadValue
                return perThreadManager.createValue();
            }
        });
        
        if (multipleClassLoaders)
        {
            fieldInfos.add(PlasticUtils.toFieldInfo(cacheField));
            cacheField.createAccessors(PropertyAccessType.READ_ONLY);
        }

        Cached annotation = method.getAnnotation(Cached.class);

        final String expression = annotation != null ? 
                annotation.watch() : 
                    extraMethodCachedWatchMap.get(method.getDescription().methodName);
        MethodResultCacheFactory factory = createFactory(plasticClass, expression, method, fieldInfos, model);

        MethodAdvice advice = createAdvice(cacheField, factory);

        method.addAdvice(advice);
    }

    private String getFieldName(PlasticMethod method) {
        return getFieldName(method, FIELD_PREFIX);
    }
    
    private String getFieldName(PlasticMethod method, String prefix) 
    {
        final String methodName = method.getDescription().methodName;
        final String className = method.getPlasticClass().getClassName();
        return getFieldName(prefix, methodName, className);
    }

    private String getFieldName(String prefix, final String methodName, final String className) 
    {
        final StringBuilder builder = new StringBuilder(prefix);
        builder.append(methodName);
        if (multipleClassLoaders)
        {
            builder.append("_");
            builder.append(className.replace('.', '_'));
        }
        return builder.toString();
    }

    private MethodAdvice createAdvice(PlasticField cacheField,
                                      final MethodResultCacheFactory factory)
    {
        final FieldHandle fieldHandle = cacheField.getHandle();
        final String fieldName = multipleClassLoaders ? cacheField.getName() : null;

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

                PerThreadValue<MethodResultCache> value = (PerThreadValue<MethodResultCache>) (
                        multipleClassLoaders ?
                        PropertyValueProvider.get(instance, fieldName) :
                        fieldHandle.get(instance));

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
                                                   PlasticMethod method, Set<FieldInfo> fieldInfos,
                                                   MutableComponentModel model)
    {
        // When there's no watch, a shared factory that just returns a new SimpleMethodResultCache
        // will suffice.
        if (watch.equals(""))
        {
            return nonWatchFactory;
        }

        // Because of the watch, its necessary to create a factory for instances of this component and method.

        final String bindingFieldName = WATCH_BINDING_PREFIX + method.getDescription().methodName;
        final PlasticField bindingField = plasticClass.introduceField(Binding.class, bindingFieldName);
        final FieldHandle bindingFieldHandle = bindingField.getHandle();
        
        if (multipleClassLoaders)
        {
            fieldInfos.add(PlasticUtils.toFieldInfo(bindingField));
            try
            {
                bindingField.createAccessors(PropertyAccessType.READ_WRITE);
            }
            catch (IllegalArgumentException e)
            {
                // Method already implemented in superclass, so, given we only
                // care the method exists, we ignore this exception
            }
        }

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

                final Object instance = invocation.getInstance();
                if (multipleClassLoaders)
                {
                    PropertyValueProvider.set(instance, bindingFieldName, binding);
                }
                else 
                {
                    bindingFieldHandle.set(instance, binding);
                }

                invocation.proceed();
            }
        });

        return new MethodResultCacheFactory()
        {
            public MethodResultCache create(Object instance)
            {
                Binding binding = (Binding) (
                        multipleClassLoaders ? 
                        PropertyValueProvider.get(instance, bindingFieldName) :
                        bindingFieldHandle.get(instance));
                
                return new WatchedBindingMethodResultCache(binding);
            }

            private Object getCacheBinding(final String methodName, String bindingFieldName, Object instance, ComponentModel model) 
            {
                Object value = PropertyValueProvider.get(instance, bindingFieldName);
                while (value == null && model.getParentModel() != null)
                {
                    model = model.getParentModel();
                    bindingFieldName = getFieldName(WATCH_BINDING_PREFIX, 
                            methodName, model.getComponentClassName());
                    value = PropertyValueProvider.get(instance, bindingFieldName);
                }
                return value;
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
