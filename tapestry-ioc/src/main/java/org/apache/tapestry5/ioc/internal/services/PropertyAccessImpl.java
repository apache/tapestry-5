// Copyright 2006, 2007, 2008 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry5.ioc.services.PropertyAccess;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PropertyAccessImpl implements PropertyAccess
{
    private final Map<Class, ClassPropertyAdapter> adapters = CollectionFactory.newConcurrentMap();

    public Object get(Object instance, String propertyName)
    {
        return getAdapter(instance).get(instance, propertyName);
    }

    public void set(Object instance, String propertyName, Object value)
    {
        getAdapter(instance).set(instance, propertyName, value);
    }

    /**
     * Clears the cache of adapters and asks the Introspector to clear its cache.
     */
    public synchronized void clearCache()
    {
        adapters.clear();

        Introspector.flushCaches();
    }

    public ClassPropertyAdapter getAdapter(Object instance)
    {
        return getAdapter(instance.getClass());
    }

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

                if (forClass.isInterface()) addPropertiesFromExtendedInterfaces(forClass, descriptors);

                return new ClassPropertyAdapterImpl(forClass, descriptors);
        }
        catch (Throwable ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private <T> void addAll(List<T> list, T[] array)
    {
        list.addAll(Arrays.asList(array));
    }

    private void addPropertiesFromExtendedInterfaces(Class forClass, List<PropertyDescriptor> descriptors)
            throws IntrospectionException
    {
        LinkedList<Class> queue = CollectionFactory.newLinkedList();

        // Seed the queue
        addAll(queue, forClass.getInterfaces());

        while (!queue.isEmpty())
        {
            Class c = queue.removeFirst();

            BeanInfo info = Introspector.getBeanInfo(c);

            addAll(descriptors, info.getPropertyDescriptors());
            addAll(queue, c.getInterfaces());
        }
    }

}
