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

package org.apache.tapestry.internal.beaneditor;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.util.List;
import java.util.Map;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.beaneditor.RelativePosition;
import org.apache.tapestry.internal.services.CoercingPropertyConduitWrapper;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.PropertyConduitSource;

public class BeanModelImpl implements BeanModel
{
    private final Class _beanType;

    private final PropertyConduitSource _propertyConduitSource;

    private final TypeCoercer _typeCoercer;

    private final Messages _messages;

    private final Map<String, PropertyModel> _properties = newCaseInsensitiveMap();

    // The list of property names, in desired order (generally not alphabetical order).

    private final List<String> _propertyNames = CollectionFactory.newList();

    public BeanModelImpl(Class beanType, PropertyConduitSource propertyConduitSource,
            TypeCoercer typeCoercer, Messages messages)
    {
        _beanType = beanType;
        _propertyConduitSource = propertyConduitSource;
        _typeCoercer = typeCoercer;
        _messages = messages;
    }

    public Class getBeanType()
    {
        return _beanType;
    }

    public PropertyModel add(String propertyName)
    {
        PropertyConduit conduit = createConduit(propertyName);

        return add(propertyName, conduit);
    }

    private void validateNewPropertyName(String propertyName)
    {
        notBlank(propertyName, "propertyName");

        if (_properties.containsKey(propertyName))
            throw new RuntimeException(BeanEditorMessages.duplicatePropertyName(
                    _beanType,
                    propertyName));
    }

    public PropertyModel add(RelativePosition position, String existingPropertyName,
            String propertyName, PropertyConduit conduit)
    {
        notNull(position, "position");

        validateNewPropertyName(propertyName);

        // Locate the existing one.

        PropertyModel existing = get(existingPropertyName);

        // Use the case normalized property name.

        int pos = _propertyNames.indexOf(existing.getPropertyName());

        PropertyModel newModel = new PropertyModelImpl(this, propertyName, conduit, _messages);

        _properties.put(propertyName, newModel);

        int offset = position == RelativePosition.AFTER ? 1 : 0;

        _propertyNames.add(pos + offset, propertyName);

        return newModel;
    }

    public PropertyModel add(RelativePosition position, String existingPropertyName,
            String propertyName)
    {
        PropertyConduit conduit = createConduit(propertyName);

        return add(position, existingPropertyName, propertyName, conduit);
    }

    public PropertyModel add(String propertyName, PropertyConduit conduit)
    {
        validateNewPropertyName(propertyName);

        PropertyModel propertyModel = new PropertyModelImpl(this, propertyName, conduit, _messages);

        _properties.put(propertyName, propertyModel);

        // Remember the order in which the properties were added.

        _propertyNames.add(propertyName);

        return propertyModel;
    }

    private CoercingPropertyConduitWrapper createConduit(String propertyName)
    {
        return new CoercingPropertyConduitWrapper(_propertyConduitSource.create(
                _beanType,
                propertyName), _typeCoercer);
    }

    public PropertyModel get(String propertyName)
    {
        PropertyModel propertyModel = _properties.get(propertyName);

        if (propertyModel == null)
            throw new RuntimeException(BeanEditorMessages.unknownProperty(
                    _beanType,
                    propertyName,
                    _properties.keySet()));

        return propertyModel;
    }

    public List<String> getPropertyNames()
    {
        return CollectionFactory.newList(_propertyNames);
    }

    public BeanModel remove(String... propertyNames)
    {
        for (String propertyName : propertyNames)
        {
            PropertyModel model = _properties.get(propertyName);

            if (model == null) continue;

            // De-referencing from the model is needed because the name provided may not be a
            // case-exact match, so we get the normalized or canonical name from the model because
            // that's the one in _propertyNames.

            _propertyNames.remove(model.getPropertyName());

            _properties.remove(propertyName);
        }

        return this;
    }

}
