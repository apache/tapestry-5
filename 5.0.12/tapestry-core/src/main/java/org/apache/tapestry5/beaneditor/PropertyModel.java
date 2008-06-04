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

package org.apache.tapestry5.beaneditor;

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.ioc.AnnotationProvider;

/**
 * Part of a {@link org.apache.tapestry5.beaneditor.BeanModel} that defines the attributes of a single property of a
 * bean.
 * <p/>
 * <p/>
 * A PropertyModel is also an {@link AnnotationProvider}, as long as the {@link org.apache.tapestry5.PropertyConduit} is
 * non-null.  When there is no property conduit, then {@link org.apache.tapestry5.ioc.AnnotationProvider#getAnnotation(Class)}
 * will return null.
 */
public interface PropertyModel extends AnnotationProvider
{
    /**
     * Returns the name of the property (which may, in fact, be a property expression).
     */
    String getPropertyName();

    /**
     * Returns the id used to access other resources (this is based on the property name, but with any excess
     * punctuation stripped out).
     */
    String getId();

    /**
     * Returns a user-presentable label for the property.
     */
    String getLabel();

    /**
     * Returns the type of the property.
     */
    Class getPropertyType();

    /**
     * Returns a logical name for the type of UI needed to view or edit the property. This is initially determined from
     * the property type.
     */
    String getDataType();

    /**
     * Changes the data type for the property.
     *
     * @param dataType
     * @return the property model, for further changes
     */
    PropertyModel dataType(String dataType);

    /**
     * Returns an object used to read or update the property. For virtual properties (properties that do not actually
     * exist on the bean), the conduit may be null.
     */
    PropertyConduit getConduit();

    /**
     * Changes the label for the property to the provided value.
     *
     * @param label new label for property
     * @return the property model, for further changes
     */
    PropertyModel label(String label);

    /**
     * Returns the containing model, often used for "fluent" construction of the model.
     */
    BeanModel model();

    /**
     * Returns true if the property can be used for sorting. By default, this is true only if the property type
     * implements Comparable.
     */
    boolean isSortable();

    /**
     * Updates sortable and returns the model for further changes.
     *
     * @return the property model, for further changes
     */
    PropertyModel sortable(boolean sortable);
}
