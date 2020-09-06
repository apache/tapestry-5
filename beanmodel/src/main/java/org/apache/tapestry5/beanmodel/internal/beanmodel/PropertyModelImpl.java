// Copyright 2007, 2008, 2010, 2011, 2014 The Apache Software Foundation
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

package org.apache.tapestry5.beanmodel.internal.beanmodel;

import java.lang.annotation.Annotation;

import org.apache.tapestry5.beaneditor.Sortable;
import org.apache.tapestry5.beanmodel.BeanModel;
import org.apache.tapestry5.beanmodel.PropertyConduit;
import org.apache.tapestry5.beanmodel.PropertyModel;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.internal.util.InternalCommonsUtils;
import org.apache.tapestry5.plastic.PlasticUtils;

@SuppressWarnings("all")
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

        id = InternalCommonsUtils.extractIdFromPropertyExpression(name);

        label = InternalCommonsUtils.defaultLabel(id, messages, name);

        // TAP5-2305
        if (conduit != null)
        {
            Sortable sortableAnnotation = conduit.getAnnotation(Sortable.class);
            if (sortableAnnotation != null)
            {
                sortable = sortableAnnotation.value();
            }
            else
            {
                // Primitive types need to be converted to wrapper types before checking to see
                // if they are sortable.
                Class wrapperType = PlasticUtils.toWrapperType(getPropertyType());
                sortable = Comparable.class.isAssignableFrom(wrapperType);
            }
        }
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
        assert InternalCommonsUtils.isNonBlank(label);
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
