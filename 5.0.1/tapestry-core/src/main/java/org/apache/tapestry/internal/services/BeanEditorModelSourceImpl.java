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

import java.util.Map;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.beaneditor.BeanEditorModel;
import org.apache.tapestry.events.InvalidationListener;
import org.apache.tapestry.internal.beaneditor.BeanEditorModelImpl;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.ioc.services.PropertyAdapter;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.ioc.util.StrategyRegistry;
import org.apache.tapestry.services.BeanEditorModelSource;

public class BeanEditorModelSourceImpl implements BeanEditorModelSource, InvalidationListener
{
    private final TypeCoercer _typeCoercer;

    private final PropertyAccess _propertyAccess;

    private final StrategyRegistry<String> _registry;

    public BeanEditorModelSourceImpl(final TypeCoercer typeCoercer,
            final PropertyAccess propertyAccess, Map<Class, String> configuration)
    {
        _typeCoercer = typeCoercer;
        _propertyAccess = propertyAccess;

        _registry = StrategyRegistry.newInstance(String.class, configuration);
    }

    public void objectWasInvalidated()
    {
        _registry.clearCache();
    }

    public BeanEditorModel create(Class beanClass, ComponentResources resources)
    {
        Defense.notNull(beanClass, "beanClass");
        Defense.notNull(resources, "resources");

        Messages messages = resources.getMessages();

        ClassPropertyAdapter adapter = _propertyAccess.getAdapter(beanClass);

        BeanEditorModel model = new BeanEditorModelImpl(beanClass, adapter, _typeCoercer, messages);

        for (String propertyName : adapter.getPropertyNames())
        {
            PropertyAdapter pa = adapter.getPropertyAdapter(propertyName);

            if (pa.isRead() && pa.isUpdate())
            {
                String editorType = _registry.get(pa.getType());

                // If an unregistered type, then ignore the property.

                if (editorType.equals(""))
                    continue;

                model.add(propertyName).editorType(editorType);
            }
        }

        return model;
    }
}
