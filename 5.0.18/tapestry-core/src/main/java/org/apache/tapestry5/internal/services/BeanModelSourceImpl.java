// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.ReorderProperties;
import org.apache.tapestry5.internal.beaneditor.BeanModelImpl;
import org.apache.tapestry5.internal.beaneditor.BeanModelUtils;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.*;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.ComponentLayer;
import org.apache.tapestry5.services.DataTypeAnalyzer;
import org.apache.tapestry5.services.PropertyConduitSource;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

public class BeanModelSourceImpl implements BeanModelSource
{
    private final TypeCoercer typeCoercer;

    private final PropertyAccess propertyAccess;

    private final PropertyConduitSource propertyConduitSource;

    private final ClassFactory classFactory;

    private final DataTypeAnalyzer dataTypeAnalyzer;

    private final ObjectLocator locator;


    private static class PropertyOrder implements Comparable<PropertyOrder>
    {
        final String propertyName;

        final int classDepth;

        final int sortKey;

        public PropertyOrder(final String propertyName, int classDepth, int sortKey)
        {
            this.propertyName = propertyName;
            this.classDepth = classDepth;
            this.sortKey = sortKey;
        }

        public int compareTo(PropertyOrder o)
        {
            int result = classDepth - o.classDepth;

            if (result == 0) result = sortKey - o.sortKey;

            if (result == 0) result = propertyName.compareTo(o.propertyName);

            return result;
        }
    }

    /**
     * @param classAdapter  defines the bean that contains the properties
     * @param propertyNames the initial set of property names, which will be rebuilt in the correct order
     */
    private void orderProperties(ClassPropertyAdapter classAdapter, List<String> propertyNames)
    {
        List<PropertyOrder> properties = CollectionFactory.newList();

        for (String name : propertyNames)
        {
            PropertyAdapter pa = classAdapter.getPropertyAdapter(name);

            Method readMethod = pa.getReadMethod();

            Location location = classFactory.getMethodLocation(readMethod);

            properties.add(new PropertyOrder(name, computeDepth(readMethod), location.getLine()));
        }

        Collections.sort(properties);

        propertyNames.clear();

        for (PropertyOrder po : properties)
        {
            propertyNames.add(po.propertyName);
        }
    }

    private static int computeDepth(Method method)
    {
        int depth = 0;
        Class c = method.getDeclaringClass();

        // When the method originates in an interface, the parent may be null, not Object.

        while (c != null && c != Object.class)
        {
            depth++;
            c = c.getSuperclass();
        }

        return depth;
    }

    public BeanModelSourceImpl(TypeCoercer typeCoercer, PropertyAccess propertyAccess,
                               PropertyConduitSource propertyConduitSource, @ComponentLayer ClassFactory classFactory,
                               @Primary DataTypeAnalyzer dataTypeAnalyzer, ObjectLocator locator)
    {
        this.typeCoercer = typeCoercer;
        this.propertyAccess = propertyAccess;
        this.propertyConduitSource = propertyConduitSource;
        this.classFactory = classFactory;
        this.dataTypeAnalyzer = dataTypeAnalyzer;
        this.locator = locator;
    }

    public <T> BeanModel<T> createDisplayModel(Class<T> beanClass, Messages messages)
    {
        return create(beanClass, false, messages);
    }

    public <T> BeanModel<T> createEditModel(Class<T> beanClass, Messages messages)
    {
        return create(beanClass, true, messages);
    }

    public <T> BeanModel<T> create(Class<T> beanClass, boolean filterReadOnlyProperties, Messages messages)
    {
        Defense.notNull(beanClass, "beanClass");
        Defense.notNull(messages, "messages");

        ClassPropertyAdapter adapter = propertyAccess.getAdapter(beanClass);

        BeanModel<T> model = new BeanModelImpl<T>(beanClass, propertyConduitSource, typeCoercer, messages,
                                                  locator);

        for (final String propertyName : adapter.getPropertyNames())
        {
            PropertyAdapter pa = adapter.getPropertyAdapter(propertyName);

            if (!pa.isRead()) continue;

            if (pa.getAnnotation(NonVisual.class) != null) continue;

            if (filterReadOnlyProperties && !pa.isUpdate()) continue;

            final String dataType = dataTypeAnalyzer.identifyDataType(pa);

            // If an unregistered type, then ignore the property.

            if (dataType == null) continue;

            model.add(propertyName).dataType(dataType);
        }

        // First, order the properties based on the location of the getter method
        // within the class.

        List<String> propertyNames = model.getPropertyNames();

        orderProperties(adapter, propertyNames);

        model.reorder(propertyNames.toArray(new String[propertyNames.size()]));

        // Next, check for an annotation with specific ordering information.

        ReorderProperties reorderAnnotation = beanClass.getAnnotation(ReorderProperties.class);

        if (reorderAnnotation != null)
        {
            BeanModelUtils.reorder(model, reorderAnnotation.value());
        }


        return model;
    }
}
