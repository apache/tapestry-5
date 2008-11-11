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

package org.apache.tapestry5.internal.beaneditor;

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.beaneditor.RelativePosition;
import org.apache.tapestry5.internal.services.CoercingPropertyConduitWrapper;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.PropertyConduitSource;

import java.util.List;
import java.util.Map;

public class BeanModelImpl<T> implements BeanModel<T>
{
    private final Class<T> beanType;

    private final PropertyConduitSource propertyConduitSource;

    private final TypeCoercer typeCoercer;

    private final Messages messages;

    private final ObjectLocator locator;

    private final Map<String, PropertyModel> properties = CollectionFactory.newCaseInsensitiveMap();

    // The list of property names, in desired order (generally not alphabetical order).

    private final List<String> propertyNames = CollectionFactory.newList();

    public BeanModelImpl(
            Class<T> beanType, PropertyConduitSource
            propertyConduitSource,
            TypeCoercer typeCoercer, Messages
            messages, ObjectLocator locator)

    {
        this.beanType = beanType;
        this.propertyConduitSource = propertyConduitSource;
        this.typeCoercer = typeCoercer;
        this.messages = messages;
        this.locator = locator;
    }

    public Class<T> getBeanType()
    {
        return beanType;
    }

    public T newInstance()
    {
        return locator.autobuild(beanType);
    }

    public PropertyModel add(String propertyName)
    {
        PropertyConduit conduit = createConduit(propertyName);

        return add(propertyName, conduit);
    }

    private void validateNewPropertyName(String propertyName)
    {
        Defense.notBlank(propertyName, "propertyName");

        if (properties.containsKey(propertyName))
            throw new RuntimeException(BeanEditorMessages.duplicatePropertyName(
                    beanType,
                    propertyName));
    }

    public PropertyModel add(RelativePosition position, String existingPropertyName,
                             String propertyName, PropertyConduit conduit)
    {
        Defense.notNull(position, "position");

        validateNewPropertyName(propertyName);

        // Locate the existing one.

        PropertyModel existing = get(existingPropertyName);

        // Use the case normalized property name.

        int pos = propertyNames.indexOf(existing.getPropertyName());

        PropertyModel newModel = new PropertyModelImpl(this, propertyName, conduit, messages);

        properties.put(propertyName, newModel);

        int offset = position == RelativePosition.AFTER ? 1 : 0;

        propertyNames.add(pos + offset, propertyName);

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

        PropertyModel propertyModel = new PropertyModelImpl(this, propertyName, conduit, messages);

        properties.put(propertyName, propertyModel);

        // Remember the order in which the properties were added.

        propertyNames.add(propertyName);

        return propertyModel;
    }

    private CoercingPropertyConduitWrapper createConduit(String propertyName)
    {
        return new CoercingPropertyConduitWrapper(propertyConduitSource.create(beanType,
                                                                               propertyName), typeCoercer);
    }

    public PropertyModel get(String propertyName)
    {
        PropertyModel propertyModel = properties.get(propertyName);

        if (propertyModel == null)
            throw new RuntimeException(BeanEditorMessages.unknownProperty(beanType,
                                                                          propertyName,
                                                                          properties.keySet()));

        return propertyModel;
    }

    public PropertyModel getById(String propertyId)
    {
        for (PropertyModel model : properties.values())
        {
            if (model.getId().equalsIgnoreCase(propertyId)) return model;
        }

        // Not found, so we throw an exception. A bit of work to set
        // up the exception however.

        List<String> ids = CollectionFactory.newList();

        for (PropertyModel model : properties.values())
        {
            ids.add(model.getId());
        }

        throw new RuntimeException(BeanEditorMessages.unknownPropertyId(beanType,
                                                                        propertyId, ids));

    }

    public List<String> getPropertyNames()
    {
        return CollectionFactory.newList(propertyNames);
    }

    public BeanModel exclude(String... propertyNames)
    {
        for (String propertyName : propertyNames)
        {
            PropertyModel model = properties.get(propertyName);

            if (model == null) continue;

            // De-referencing from the model is needed because the name provided may not be a
            // case-exact match, so we get the normalized or canonical name from the model because
            // that's the one in propertyNames.

            this.propertyNames.remove(model.getPropertyName());

            properties.remove(propertyName);
        }

        return this;
    }

    public BeanModel reorder(String... propertyNames)
    {
        List<String> remainingPropertyNames = CollectionFactory.newList(this.propertyNames);
        List<String> reorderedPropertyNames = CollectionFactory.newList();

        for (String name : propertyNames)
        {
            PropertyModel model = get(name);

            // Get the canonical form (which may differ from name in terms of case)
            String canonical = model.getPropertyName();

            reorderedPropertyNames.add(canonical);

            remainingPropertyNames.remove(canonical);
        }

        this.propertyNames.clear();
        this.propertyNames.addAll(reorderedPropertyNames);

        // Any unspecified names are ordered to the end. Don't want them? Remove them instead.
        this.propertyNames.addAll(remainingPropertyNames);

        return this;
    }

    public BeanModel include(String... propertyNames)
    {
        List<String> reorderedPropertyNames = CollectionFactory.newList();
        Map<String, PropertyModel> reduced = CollectionFactory.newCaseInsensitiveMap();


        for (String name : propertyNames)
        {

            PropertyModel model = get(name);

            String canonical = model.getPropertyName();

            reorderedPropertyNames.add(canonical);
            reduced.put(canonical, model);

        }

        this.propertyNames.clear();
        this.propertyNames.addAll(reorderedPropertyNames);

        properties.clear();
        properties.putAll(reduced);

        return this;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("BeanModel[");
        builder.append(ClassFabUtils.toJavaClassName(beanType));

        builder.append(" properties:");
        String sep = "";

        for (String name : propertyNames)
        {
            builder.append(sep);
            builder.append(name);

            sep = ", ";
        }

        builder.append("]");

        return builder.toString();
    }
}
