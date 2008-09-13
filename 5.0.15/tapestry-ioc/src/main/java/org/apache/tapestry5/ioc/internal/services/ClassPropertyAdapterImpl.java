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

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import org.apache.tapestry5.ioc.internal.util.GenericsUtils;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry5.ioc.services.PropertyAdapter;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class ClassPropertyAdapterImpl implements ClassPropertyAdapter
{
    private final Map<String, PropertyAdapter> adapters = newCaseInsensitiveMap();

    private final Class beanType;

    public ClassPropertyAdapterImpl(Class beanType, List<PropertyDescriptor> descriptors)
    {
        this.beanType = beanType;

        for (PropertyDescriptor pd : descriptors)
        {
            // Indexed properties will have a null propertyType (and a non-null
            // indexedPropertyType). We ignore indexed properties.

            if (pd.getPropertyType() == null) continue;

            Method readMethod = pd.getReadMethod();

            Class propertyType = readMethod == null ? pd.getPropertyType() : GenericsUtils.extractGenericReturnType(
                    beanType, readMethod);

            PropertyAdapter pa = new PropertyAdapterImpl(this, pd.getName(), propertyType, readMethod,
                                                         pd.getWriteMethod());

            adapters.put(pa.getName(), pa);
        }
    }

    public Class getBeanType()
    {
        return beanType;
    }

    @Override
    public String toString()
    {
        String names = InternalUtils.joinSorted(adapters.keySet());

        return String.format("<ClassPropertyAdaptor %s : %s>", beanType.getName(), names);
    }

    public List<String> getPropertyNames()
    {
        return InternalUtils.sortedKeys(adapters);
    }

    public PropertyAdapter getPropertyAdapter(String name)
    {
        return adapters.get(name);
    }

    public Object get(Object instance, String propertyName)
    {
        return adaptorFor(propertyName).get(instance);
    }

    public void set(Object instance, String propertyName, Object value)
    {
        adaptorFor(propertyName).set(instance, value);
    }

    private PropertyAdapter adaptorFor(String name)
    {
        PropertyAdapter pa = adapters.get(name);

        if (pa == null) throw new IllegalArgumentException(ServiceMessages.noSuchProperty(beanType, name));

        return pa;
    }

}
