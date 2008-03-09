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

package org.apache.tapestry.internal.beaneditor;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.beaneditor.RelativePosition;
import org.apache.tapestry.internal.services.CoercingPropertyConduitWrapper;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.ioc.services.ClassFabUtils;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.PropertyConduitSource;

import java.util.List;
import java.util.Map;

public class BeanModelImpl<T> implements BeanModel<T>
{
    private final Class<T> _beanType;

    private final PropertyConduitSource _propertyConduitSource;

    private final TypeCoercer _typeCoercer;

    private final Messages _messages;

    private final ObjectLocator _locator;

    private final Map<String, PropertyModel> _properties = newCaseInsensitiveMap();

    // The list of property names, in desired order (generally not alphabetical order).

    private final List<String> _propertyNames = CollectionFactory.newList();

    public BeanModelImpl(
            Class<T> beanType, PropertyConduitSource
            propertyConduitSource,
            TypeCoercer typeCoercer, Messages
            messages, ObjectLocator locator)

    {
        _beanType = beanType;
        _propertyConduitSource = propertyConduitSource;
        _typeCoercer = typeCoercer;
        _messages = messages;
        _locator = locator;
    }

    public Class<T> getBeanType()
    {
        return _beanType;
    }

    public T newInstance()
    {
        return _locator.autobuild(_beanType);
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
        return new CoercingPropertyConduitWrapper(_propertyConduitSource.create(_beanType,
                                                                                propertyName), _typeCoercer);
    }

    public PropertyModel get(String propertyName)
    {
        PropertyModel propertyModel = _properties.get(propertyName);

        if (propertyModel == null)
            throw new RuntimeException(BeanEditorMessages.unknownProperty(_beanType,
                                                                          propertyName,
                                                                          _properties.keySet()));

        return propertyModel;
    }

    public PropertyModel getById(String propertyId)
    {
        for (PropertyModel model : _properties.values())
        {
            if (model.getId().equalsIgnoreCase(propertyId)) return model;
        }

        // Not found, so we throw an exception. A bit of work to set
        // up the exception however.

        List<String> ids = CollectionFactory.newList();

        for (PropertyModel model : _properties.values())
        {
            ids.add(model.getId());
        }

        throw new RuntimeException(BeanEditorMessages.unknownPropertyId(_beanType,
                                                                        propertyId, ids));

    }

    public List<String> getPropertyNames()
    {
        return CollectionFactory.newList(_propertyNames);
    }

    public BeanModel exclude(String... propertyNames)
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

    public BeanModel reorder(String... propertyNames)
    {
        List<String> remainingPropertyNames = newList(_propertyNames);
        List<String> reorderedPropertyNames = newList();

        for (String name : propertyNames)
        {
            PropertyModel model = get(name);

            // Get the canonical form (which may differ from name in terms of case)
            String canonical = model.getPropertyName();

            reorderedPropertyNames.add(canonical);

            remainingPropertyNames.remove(canonical);
        }

        _propertyNames.clear();
        _propertyNames.addAll(reorderedPropertyNames);

        // Any unspecified names are ordered to the end. Don't want them? Remove them instead.
        _propertyNames.addAll(remainingPropertyNames);

        return this;
    }

    public BeanModel include(String... propertyNames)
    {
        List<String> reorderedPropertyNames = newList();
        Map<String, PropertyModel> reduced = CollectionFactory.newCaseInsensitiveMap();


        for (String name : propertyNames)
        {

            PropertyModel model = get(name);

            String canonical = model.getPropertyName();

            reorderedPropertyNames.add(canonical);
            reduced.put(canonical, model);

        }

        _propertyNames.clear();
        _propertyNames.addAll(reorderedPropertyNames);

        _properties.clear();
        _properties.putAll(reduced);

        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("BeanModel[");
        builder.append(ClassFabUtils.toJavaClassName(_beanType));

        builder.append(" properties:");
        String sep = "";

        for (String name : _propertyNames)
        {
            builder.append(sep);
            builder.append(name);

            sep = ", ";
        }

        builder.append("]");

        return builder.toString();
    }
}
