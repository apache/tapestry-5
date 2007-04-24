// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry.ioc.services.PropertyAdapter;

public class ClassPropertyAdapterImpl implements ClassPropertyAdapter
{
    private final Map<String, PropertyAdapter> _adapters = newMap();

    private final Class _targetClass;

    public ClassPropertyAdapterImpl(Class targetClass, List<PropertyDescriptor> descriptors)
    {
        _targetClass = targetClass;

        for (PropertyDescriptor pd : descriptors)
        {
            // Indexed properties will have a null propertyType (and a non-null
            // indexedPropertyType). We ignore indexed properties.

            if (pd.getPropertyType() == null) continue;

            PropertyAdapter pa = new PropertyAdapterImpl(pd);

            _adapters.put(pa.getName(), pa);
        }
    }

    @Override
    public String toString()
    {
        String names = InternalUtils.joinSorted(_adapters.keySet());

        return String.format("<ClassPropertyAdaptor %s : %s>", _targetClass.getName(), names);
    }

    public List<String> getPropertyNames()
    {
        return InternalUtils.sortedKeys(_adapters);
    }

    public PropertyAdapter getPropertyAdapter(String name)
    {
        return _adapters.get(name);
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
        PropertyAdapter pa = _adapters.get(name);

        if (pa == null)
            throw new IllegalArgumentException(ServiceMessages.noSuchProperty(_targetClass, name));

        return pa;
    }

}
