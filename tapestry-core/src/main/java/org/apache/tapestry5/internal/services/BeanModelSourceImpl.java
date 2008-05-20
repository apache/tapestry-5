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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.NonVisual;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.internal.beaneditor.BeanModelImpl;
import org.apache.tapestry.ioc.LoggerSource;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.annotation.Primary;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.services.*;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.ComponentLayer;
import org.apache.tapestry.services.DataTypeAnalyzer;
import org.apache.tapestry.services.PropertyConduitSource;

import java.util.List;
import java.util.Map;

public class BeanModelSourceImpl implements BeanModelSource
{
    private final LoggerSource loggerSource;

    private final TypeCoercer typeCoercer;

    private final PropertyAccess propertyAccess;

    private final PropertyConduitSource propertyConduitSource;

    private final ClassFactory classFactory;

    private final DataTypeAnalyzer dataTypeAnalyzer;

    private final ObjectLocator locator;

    public BeanModelSourceImpl(LoggerSource loggerSource, TypeCoercer typeCoercer, PropertyAccess propertyAccess,
                               PropertyConduitSource propertyConduitSource, @ComponentLayer ClassFactory classFactory,
                               @Primary DataTypeAnalyzer dataTypeAnalyzer, ObjectLocator locator)
    {
        this.loggerSource = loggerSource;
        this.typeCoercer = typeCoercer;
        this.propertyAccess = propertyAccess;
        this.propertyConduitSource = propertyConduitSource;
        this.classFactory = classFactory;
        this.dataTypeAnalyzer = dataTypeAnalyzer;
        this.locator = locator;
    }

    public <T> BeanModel<T> create(Class<T> beanClass, boolean filterReadOnlyProperties, ComponentResources resources)
    {
        Defense.notNull(beanClass, "beanClass");
        Defense.notNull(resources, "resources");

        Messages messages = resources.getMessages();

        ClassPropertyAdapter adapter = propertyAccess.getAdapter(beanClass);

        final BeanModel<T> model = new BeanModelImpl<T>(beanClass, propertyConduitSource, typeCoercer, messages,
                                                        locator);

        List<String> propertyNames = CollectionFactory.newList();

        Map<String, Runnable> worksheet = CollectionFactory.newMap();

        for (final String propertyName : adapter.getPropertyNames())
        {
            PropertyAdapter pa = adapter.getPropertyAdapter(propertyName);

            if (!pa.isRead()) continue;

            if (pa.getAnnotation(NonVisual.class) != null) continue;

            if (filterReadOnlyProperties && !pa.isUpdate()) continue;

            final String dataType = dataTypeAnalyzer.identifyDataType(pa);

            // If an unregistered type, then ignore the property.

            if (dataType == null) continue;

            propertyNames.add(propertyName);

            // We need to defer execution of this; we want to add them in proper order, not
            // alphabetical order.

            Runnable worker = new Runnable()
            {
                public void run()
                {
                    model.add(propertyName).dataType(dataType);
                }
            };

            worksheet.put(propertyName, worker);
        }

        // Determine the correct order to add the properties.

        List<String> orderedNames = TapestryInternalUtils.orderProperties(loggerSource
                .getLogger(beanClass), adapter, classFactory, propertyNames);

        for (String propertyName : orderedNames)
        {
            Runnable r = worksheet.get(propertyName);

            // This actually adds the property to the model, but we're doing it
            // in orderedNames order, not propertyNames order (which is alphabetical).
            // The default ordering comes from method ordering within the class.

            r.run();
        }

        return model;
    }
}
