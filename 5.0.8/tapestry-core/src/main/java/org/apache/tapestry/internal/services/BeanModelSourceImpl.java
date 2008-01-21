// Copyright 2007 The Apache Software Foundation
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
import org.apache.tapestry.ioc.annotations.Primary;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.ioc.services.*;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.ComponentLayer;
import org.apache.tapestry.services.DataTypeAnalyzer;
import org.apache.tapestry.services.PropertyConduitSource;

import java.util.List;
import java.util.Map;

public class BeanModelSourceImpl implements BeanModelSource
{
    private final LoggerSource _loggerSource;

    private final TypeCoercer _typeCoercer;

    private final PropertyAccess _propertyAccess;

    private final PropertyConduitSource _propertyConduitSource;

    private final ClassFactory _classFactory;

    private final DataTypeAnalyzer _dataTypeAnalyzer;

    public BeanModelSourceImpl(LoggerSource loggerSource, TypeCoercer typeCoercer, PropertyAccess propertyAccess,
                               PropertyConduitSource propertyConduitSource, @ComponentLayer ClassFactory classFactory,
                               @Primary DataTypeAnalyzer dataTypeAnalyzer)
    {
        _loggerSource = loggerSource;
        _typeCoercer = typeCoercer;
        _propertyAccess = propertyAccess;
        _propertyConduitSource = propertyConduitSource;
        _classFactory = classFactory;
        _dataTypeAnalyzer = dataTypeAnalyzer;
    }

    public BeanModel create(Class beanClass, boolean filterReadOnlyProperties, ComponentResources resources)
    {
        notNull(beanClass, "beanClass");
        notNull(resources, "resources");

        Messages messages = resources.getMessages();

        ClassPropertyAdapter adapter = _propertyAccess.getAdapter(beanClass);

        final BeanModel model = new BeanModelImpl(beanClass, _propertyConduitSource, _typeCoercer, messages);

        List<String> propertyNames = newList();

        Map<String, Runnable> worksheet = newMap();

        for (final String propertyName : adapter.getPropertyNames())
        {
            PropertyAdapter pa = adapter.getPropertyAdapter(propertyName);

            if (!pa.isRead()) continue;

            if (pa.getAnnotation(NonVisual.class) != null) continue;

            if (filterReadOnlyProperties && !pa.isUpdate()) continue;

            final String dataType = _dataTypeAnalyzer.identifyDataType(pa);

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

        List<String> orderedNames = TapestryInternalUtils.orderProperties(_loggerSource
                .getLogger(beanClass), adapter, _classFactory, propertyNames);

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
