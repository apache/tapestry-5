// Copyright 2006, 2007, 2008, 2010, 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.beanmodel.internal.services;

import static org.apache.tapestry5.commons.util.CollectionFactory.newCaseInsensitiveMap;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.commons.internal.services.ServiceMessages;
import org.apache.tapestry5.commons.internal.util.GenericsUtils;
import org.apache.tapestry5.commons.internal.util.InternalCommonsUtils;
import org.apache.tapestry5.commons.services.ClassPropertyAdapter;
import org.apache.tapestry5.commons.services.PropertyAdapter;
import org.apache.tapestry5.commons.util.CollectionFactory;

public class ClassPropertyAdapterImpl implements ClassPropertyAdapter
{
    private final Map<String, PropertyAdapter> adapters = newCaseInsensitiveMap();

    private final Class beanType;

    public ClassPropertyAdapterImpl(Class beanType, List<PropertyDescriptor> descriptors)
    {
        this.beanType = beanType;

        // lazy init
        Map<String, List<Method>> nonBridgeMethods = null;

        for (PropertyDescriptor pd : descriptors)
        {
            // Indexed properties will have a null propertyType (and a non-null
            // indexedPropertyType). We ignore indexed properties.

            String name = pd.getName();

            if (adapters.containsKey(name))
            {
                continue;
            }

            final Class<?> thisPropertyType = pd.getPropertyType();
            if (thisPropertyType == null)
                continue;

            Method readMethod = pd.getReadMethod();
            Method writeMethod = pd.getWriteMethod();

            // TAP5-1493
            if (readMethod != null && readMethod.isBridge())
            {
                if (nonBridgeMethods == null)
                {
                    nonBridgeMethods = groupNonBridgeMethodsByName(beanType);
                }
                readMethod = findMethodWithSameNameAndParamCount(readMethod, nonBridgeMethods);
            }

            // TAP5-1548, TAP5-1885: trying to find a getter which Introspector missed
            if (readMethod == null) {
                final String prefix = thisPropertyType != boolean.class ? "get" : "is";
                try
                {
                    Method method = beanType.getMethod(prefix + capitalize(name));
                    final Class<?> returnType = method.getReturnType();
                    if (returnType.equals(thisPropertyType) || returnType.isInstance(thisPropertyType)) {
                        readMethod = method;
                    }
                }
                catch (SecurityException e) {
                    // getter not usable.
                }
                catch (NoSuchMethodException e)
                {
                    // getter doesn't exist.
                }
            }

            if (writeMethod != null && writeMethod.isBridge())
            {
                if (nonBridgeMethods == null)
                {
                    nonBridgeMethods = groupNonBridgeMethodsByName(beanType);
                }
                writeMethod = findMethodWithSameNameAndParamCount(writeMethod, nonBridgeMethods);
            }

            // TAP5-1548, TAP5-1885: trying to find a setter which Introspector missed
            if (writeMethod == null) {
                try
                {
                    Method method = beanType.getMethod("set" + capitalize(name), pd.getPropertyType());
                    final Class<?> returnType = method.getReturnType();
                    if (returnType.equals(void.class)) {
                        writeMethod = method;
                    }
                }
                catch (SecurityException e) {
                    // setter not usable.
                }
                catch (NoSuchMethodException e)
                {
                    // setter doesn't exist.
                }
            }

            Class propertyType = readMethod == null ? thisPropertyType : GenericsUtils.extractGenericReturnType(
                    beanType, readMethod);

            PropertyAdapter pa = new PropertyAdapterImpl(this, name, propertyType, readMethod, writeMethod);

            adapters.put(pa.getName(), pa);
        }

        // Now, add any public fields (even if static) that do not conflict

        for (Field f : beanType.getFields())
        {
            String name = f.getName();

            if (!adapters.containsKey(name))
            {
                Class propertyType = GenericsUtils.extractGenericFieldType(beanType, f);
                PropertyAdapter pa = new PropertyAdapterImpl(this, name, propertyType, f);

                adapters.put(name, pa);
            }
        }
    }

    private static String capitalize(String name)
    {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Find a replacement for the method (if one exists)
     * @param method A method
     * @param groupedMethods Methods mapped by name
     * @return A method from groupedMethods with the same name / param count
     *         (default to providedmethod if none found)
     */
    private Method findMethodWithSameNameAndParamCount(Method method, Map<String, List<Method>> groupedMethods) {
        List<Method> methodGroup = groupedMethods.get(method.getName());
        if (methodGroup != null)
        {
            for (Method nonBridgeMethod : methodGroup)
            {
                if (nonBridgeMethod.getParameterTypes().length == method.getParameterTypes().length)
                {
                    // return the non-bridge method with the same name / argument count
                    return nonBridgeMethod;
                }
            }
        }

        // default to the provided method
        return method;
    }

    /**
     * Find all of the public methods that are not bridge methods and
     * group them by method name
     *
     * {@see Method#isBridge()}
     * @param type Bean type
     * @return
     */
    private Map<String, List<Method>> groupNonBridgeMethodsByName(Class type)
    {
        Map<String, List<Method>> methodGroupsByName = CollectionFactory.newMap();
        for (Method method : type.getMethods())
        {
            if (!method.isBridge())
            {
                List<Method> methodGroup = methodGroupsByName.get(method.getName());
                if (methodGroup == null)
                {
                    methodGroup = CollectionFactory.newList();
                    methodGroupsByName.put(method.getName(), methodGroup);
                }
                methodGroup.add(method);
            }
        }
        return methodGroupsByName;
    }

    @Override
    public Class getBeanType()
    {
        return beanType;
    }

    @Override
    public String toString()
    {
        String names = InternalCommonsUtils.joinSorted(adapters.keySet());

        return String.format("<ClassPropertyAdaptor %s: %s>", beanType.getName(), names);
    }

    @Override
    public List<String> getPropertyNames()
    {
        return InternalCommonsUtils.sortedKeys(adapters);
    }

    @Override
    public PropertyAdapter getPropertyAdapter(String name)
    {
        return adapters.get(name);
    }

    @Override
    public Object get(Object instance, String propertyName)
    {
        return adaptorFor(propertyName).get(instance);
    }

    @Override
    public void set(Object instance, String propertyName, Object value)
    {
        adaptorFor(propertyName).set(instance, value);
    }

    @Override
    public Annotation getAnnotation(Object instance, String propertyName, Class<? extends Annotation> annotationClass) {
    return adaptorFor(propertyName).getAnnotation(annotationClass);
    }

    private PropertyAdapter adaptorFor(String name)
    {
        PropertyAdapter pa = adapters.get(name);

        if (pa == null)
            throw new IllegalArgumentException(ServiceMessages.noSuchProperty(beanType, name));

        return pa;
    }

}
