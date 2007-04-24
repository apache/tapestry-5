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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.beaneditor.BeanEditorModel;
import org.apache.tapestry.beaneditor.Order;
import org.apache.tapestry.beaneditor.PropertyConduit;
import org.apache.tapestry.beaneditor.PropertyEditModel;
import org.apache.tapestry.internal.TapestryUtils;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry.ioc.services.PropertyAdapter;
import org.apache.tapestry.ioc.services.TypeCoercer;

public class BeanEditorModelImpl implements BeanEditorModel, Comparator<PropertyEditModel>
{
    private final Class _beanType;

    private final ClassPropertyAdapter _classPropertyAdapter;

    private final TypeCoercer _typeCoercer;

    private final Messages _messages;

    private final Map<String, PropertyEditModel> _properties = newMap();

    public BeanEditorModelImpl(Class beanType, ClassPropertyAdapter classPropertyAdapter,
            TypeCoercer typeCoercer, Messages messages)
    {
        _beanType = beanType;
        _classPropertyAdapter = classPropertyAdapter;
        _typeCoercer = typeCoercer;
        _messages = messages;
    }

    public PropertyEditModel add(String propertyName)
    {
        if (_properties.containsKey(propertyName))
            throw new RuntimeException(BeanEditorMessages.duplicatePropertyName(
                    _beanType,
                    propertyName));

        String label = defaultLabel(propertyName);

        final PropertyAdapter adapter = _classPropertyAdapter.getPropertyAdapter(propertyName);

        PropertyConduit conduit = defaultConduit(adapter);

        PropertyEditModel propertyModel = new PropertyEditModelImpl(this, propertyName)
                .label(label).conduit(conduit);

        if (adapter != null)
            propertyModel.propertyType(adapter.getType());

        if (conduit != null)
        {
            Order annotation = conduit.getAnnotation(Order.class);

            if (annotation != null)
                propertyModel.order(annotation.value());
        }

        _properties.put(propertyName, propertyModel);

        return propertyModel;
    }

    private PropertyConduit defaultConduit(final PropertyAdapter adapter)
    {
        if (adapter == null)
            return null;

        final Class propertyType = adapter.getType();

        // Eventually, we'll find a way to replace this with something that does not
        // use reflection.

        return new PropertyConduit()
        {
            public Object get(Object instance)
            {
                return adapter.get(instance);
            }

            @SuppressWarnings("unchecked")
            public void set(Object instance, Object value)
            {
                // TODO: Wrap this to provide a more useful error if the coercion
                // fails.

                Object coerced = _typeCoercer.coerce(value, propertyType);

                adapter.set(instance, coerced);
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                T result = getAnnotation(adapter.getWriteMethod(), annotationClass);

                if (result == null)
                    result = getAnnotation(adapter.getReadMethod(), annotationClass);

                return result;
            }

            private <T extends Annotation> T getAnnotation(Method method, Class<T> annotationClass)
            {
                return method == null ? null : method.getAnnotation(annotationClass);
            }

        };
    }

    public PropertyEditModel edit(String propertyName)
    {
        PropertyEditModel propertyModel = _properties.get(propertyName);

        if (propertyModel == null)
            throw new RuntimeException(BeanEditorMessages.unknownProperty(
                    _beanType,
                    propertyName,
                    _properties.keySet()));

        return propertyModel;
    }

    public PropertyEditModel get(String propertyName)
    {
        return edit(propertyName);
    }

    public List<String> getPropertyNames()
    {
        List<PropertyEditModel> propertyModels = newList(_properties.values());

        Collections.sort(propertyModels, this);

        List<String> result = newList();

        for (PropertyEditModel propertyModel : propertyModels)
            result.add(propertyModel.getPropertyName());

        return result;
    }

    public int compare(PropertyEditModel o1, PropertyEditModel o2)
    {
        int result = o1.getOrder() - o2.getOrder();

        if (result == 0)
            result = o1.getPropertyName().compareTo(o2.getPropertyName());

        return result;
    }

    private String defaultLabel(String propertyName)
    {
        String key = propertyName + "-label";

        if (_messages.contains(key))
            return _messages.get(key);

        return TapestryUtils.toUserPresentable(propertyName);
    }

}
