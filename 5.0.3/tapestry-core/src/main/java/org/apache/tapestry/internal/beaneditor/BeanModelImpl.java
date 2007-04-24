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
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.internal.services.CoercingPropertyConduitWrapper;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.PropertyConduitSource;

public class BeanModelImpl implements BeanModel, Comparator<PropertyModel>
{
    private final Class _beanType;

    private final PropertyConduitSource _propertyConduitSource;

    private final TypeCoercer _typeCoercer;

    private final Messages _messages;

    private final Map<String, PropertyModel> _properties = newCaseInsensitiveMap();

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
        if (_properties.containsKey(propertyName))
            throw new RuntimeException(BeanEditorMessages.duplicatePropertyName(
                    _beanType,
                    propertyName));

        PropertyConduit conduit = createConduit(propertyName);

        return add(propertyName, conduit);
    }

    public PropertyModel add(String propertyName, PropertyConduit conduit)
    {
        PropertyModel propertyModel = new PropertyModelImpl(this, propertyName, conduit, _messages);

        _properties.put(propertyName, propertyModel);

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
        List<PropertyModel> propertyModels = newList(_properties.values());

        // Sort the list of models by their order property.

        Collections.sort(propertyModels, this);

        List<String> result = newList();

        for (PropertyModel propertyModel : propertyModels)
            result.add(propertyModel.getPropertyName());

        return result;
    }

    public int compare(PropertyModel o1, PropertyModel o2)
    {
        int result = o1.getOrder() - o2.getOrder();

        if (result == 0)
            result = o1.getPropertyName().compareTo(o2.getPropertyName());

        return result;
    }

    public BeanModel remove(String... propertyNames)
    {
        _properties.keySet().removeAll(Arrays.asList(propertyNames));

        return this;
    }

}
