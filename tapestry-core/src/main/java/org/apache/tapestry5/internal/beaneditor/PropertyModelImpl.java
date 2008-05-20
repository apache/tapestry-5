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
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.ClassFabUtils;

import java.lang.annotation.Annotation;

public class PropertyModelImpl implements PropertyModel
{
    private final BeanModel model;

    private final String name;

    private final PropertyConduit conduit;

    private final String id;

    private String label;

    private String dataType;

    private boolean sortable;

    public PropertyModelImpl(BeanModel model, String name, PropertyConduit conduit, Messages messages)
    {
        this.model = model;
        this.name = name;
        this.conduit = conduit;

        id = TapestryInternalUtils.extractIdFromPropertyExpression(name);

        label = TapestryInternalUtils.defaultLabel(id, messages, name);

        // Primitive types need to be converted to wrapper types before checking to see
        // if they are sortable.

        Class wrapperType = ClassFabUtils.getWrapperType(getPropertyType());

        sortable = Comparable.class.isAssignableFrom(wrapperType);
    }

    public String getId()
    {
        return id;
    }

    public Class getPropertyType()
    {
        return conduit == null ? Object.class : conduit.getPropertyType();
    }

    public PropertyConduit getConduit()
    {
        return conduit;
    }

    public PropertyModel label(String label)
    {
        Defense.notBlank(label, "label");

        this.label = label;

        return this;
    }

    public String getLabel()
    {
        return label;
    }

    public String getPropertyName()
    {
        return name;
    }

    public BeanModel model()
    {
        return model;
    }

    public PropertyModel dataType(String dataType)
    {
        this.dataType = dataType;

        return this;
    }

    public String getDataType()
    {
        return dataType;
    }

    public boolean isSortable()
    {
        return sortable;
    }

    public PropertyModel sortable(boolean sortable)
    {
        this.sortable = sortable;

        return this;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return conduit == null ? null : conduit.getAnnotation(annotationClass);
    }
}
