// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.commons.services.ClassPropertyAdapter;
import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.util.CollectionFactory;

@SuppressWarnings("unchecked")
public class PropertyAccessImpl implements PropertyAccess
{
    private final Map<Class, ClassPropertyAdapter> adapters = CollectionFactory.newConcurrentMap();

    @Override
    public Object get(Object instance, String propertyName)
    {
        return getAdapter(instance).get(instance, propertyName);
    }

    @Override
    public void set(Object instance, String propertyName, Object value)
    {
        getAdapter(instance).set(instance, propertyName, value);
    }

    @Override
    public Annotation getAnnotation(Object instance, String propertyName, Class<? extends Annotation> annotationClass) {
    return getAdapter(instance).getAnnotation(instance, propertyName, annotationClass);
    }


    /**
     * Clears the cache of adapters and asks the {@link Introspector} to clear its cache.
     */
    @Override
    public synchronized void clearCache()
    {
        adapters.clear();

        Introspector.flushCaches();
    }

    @Override
    public ClassPropertyAdapter getAdapter(Object instance)
    {
        return getAdapter(instance.getClass());
    }

    @Override
    public ClassPropertyAdapter getAdapter(Class forClass)
    {
        ClassPropertyAdapter result = adapters.get(forClass);

        if (result == null)
        {
            result = buildAdapter(forClass);
            adapters.put(forClass, result);
        }

        return result;
    }

    /**
     * Builds a new adapter and updates the _adapters cache. This not only guards access to the adapter cache, but also
     * serializes access to the Java Beans Introspector, which is not thread safe. In addition, handles the case where
     * the class in question is an interface, accumulating properties inherited from super-classes.
     */
    private synchronized ClassPropertyAdapter buildAdapter(Class forClass)
    {
        // In some race conditions, we may hit this method for the same class multiple times.
        // We just let it happen, replacing the old ClassPropertyAdapter with a new one.

        try
        {
            BeanInfo info = Introspector.getBeanInfo(forClass);

            List<PropertyDescriptor> descriptors = CollectionFactory.newList();

            addAll(descriptors, info.getPropertyDescriptors());

            // Introspector misses:
            // - interface methods not implemented in an abstract class (TAP5-921)
            // - default methods (TAP5-2449)
            addPropertiesFromExtendedInterfaces(forClass, descriptors);

            addPropertiesFromScala(forClass, descriptors);


            return new ClassPropertyAdapterImpl(forClass, descriptors);
        }
        catch (Throwable ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private static <T> void addAll(List<T> list, T[] array)
    {
        if (array.length > 0){
            list.addAll(Arrays.asList(array));
        }
    }

    private static <T> void addInterfacesRecursively(List<Class> list, Class<?> clazz)
    {
        Class<?>[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
          Class<?> iface = interfaces[i];
          addInterfacesRecursively(list, iface);
          list.add(iface);
        }
    }

    private static void addPropertiesFromExtendedInterfaces(Class forClass, List<PropertyDescriptor> descriptors)
            throws IntrospectionException
    {

        Class[] interfaces = forClass.getInterfaces();
        if (interfaces.length == 0){
            return;
        }
        LinkedList<Class> queue = CollectionFactory.newLinkedList();
        // Seed the queue
        addInterfacesRecursively(queue, forClass);

        while (!queue.isEmpty())
        {
            Class c = queue.removeFirst();

            BeanInfo info = Introspector.getBeanInfo(c);

            // Duplicates occur and are filtered out in ClassPropertyAdapter which stores
            // a property name to descriptor map.
            addAll(descriptors, info.getPropertyDescriptors());
        }
    }

    private void addPropertiesFromScala(Class forClass, List<PropertyDescriptor> descriptors)
            throws IntrospectionException
    {
        for (Method method : forClass.getMethods())
        {
            addPropertyIfScalaGetterMethod(forClass, descriptors, method);
        }
    }

    private void addPropertyIfScalaGetterMethod(Class forClass, List<PropertyDescriptor> descriptors, Method method)
            throws IntrospectionException
    {
        if (!isScalaGetterMethod(method))
            return;

        PropertyDescriptor propertyDescriptor = new PropertyDescriptor(method.getName(), forClass, method.getName(),
                null);

        // found a getter, looking for the setter now
        try
        {
            Method setterMethod = findScalaSetterMethod(forClass, method);

            propertyDescriptor.setWriteMethod(setterMethod);
        }
        catch (NoSuchMethodException e)
        {
            // ignore
        }

        // check if the same property was already discovered with java bean accessors

        addScalaPropertyIfNoJavaBeansProperty(descriptors, propertyDescriptor, method);
    }

    private void addScalaPropertyIfNoJavaBeansProperty(List<PropertyDescriptor> descriptors,
            PropertyDescriptor propertyDescriptor, Method getterMethod)
    {
        boolean found = false;

        for (PropertyDescriptor currentPropertyDescriptor : descriptors)
        {
            if (currentPropertyDescriptor.getName().equals(getterMethod.getName()))
            {
                found = true;

                break;
            }
        }

        if (!found)
            descriptors.add(propertyDescriptor);
    }

    private Method findScalaSetterMethod(Class forClass, Method getterMethod) throws NoSuchMethodException
    {
        return forClass.getMethod(getterMethod.getName() + "_$eq", getterMethod.getReturnType());
    }

    private boolean isScalaGetterMethod(Method method)
    {
        try
        {
            return Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0
                    && !method.getReturnType().equals(Void.TYPE)
                    && method.getDeclaringClass().getDeclaredField(method.getName()) != null;
        }
        catch (NoSuchFieldException ex)
        {
            return false;
        }
    }
}
