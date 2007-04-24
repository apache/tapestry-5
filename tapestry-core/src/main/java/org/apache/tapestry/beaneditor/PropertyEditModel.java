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

package org.apache.tapestry.beaneditor;

/**
 * Part of a {@link BeanEditorModel} that defines the attributes of a single edittable property of a
 * bean.
 */
public interface PropertyEditModel
{
    /** Returns the name of the property. */
    String getPropertyName();

    /** Returns a user-presentable label for the property. */
    String getLabel();

    /** Returns the order vlaue, used to sort the properties into presentation order. */
    int getOrder();

    /** Returns the type of the property. */
    Class getPropertyType();

    /**
     * Returns a logical name for the type of UI needed to edit the property. This is initially
     * determined from the property type.
     */
    String getEditorType();

    /**
     * Changes the editor type for the property.
     * 
     * @param editorType
     * @return the property edit model, for further changes
     */
    PropertyEditModel editorType(String editorType);

    /**
     * Returns an object used to read or update the property. For virtual properties (properties
     * that do not actually exist on the bean), the conduit may be null until one is provided via
     * {@link MutablePropertyEditModel#conduit(PropertyConduit)}.
     */
    PropertyConduit getConduit();

    /**
     * Changes the property type for the property.
     * 
     * @param propertyType
     *            the new property type for the property.
     * @return the property edit model, for further changes
     */
    PropertyEditModel propertyType(Class propertyType);

    /**
     * Changes the label for the property to the provided value.
     * 
     * @param label
     *            new label for property
     * @return the property edit model, for further changes
     */
    PropertyEditModel label(String label);

    /**
     * Changes the order for the property. The properties are sorted by order (and then by name for
     * identical orders) when building the user interface.
     */
    PropertyEditModel order(int order);

    /**
     * Changes the conduit for the property to the provided value.
     * 
     * @param conduit
     *            new conduit object (used to read or update the property value on an instance)
     * @return the property edit model, for further changes
     */
    PropertyEditModel conduit(PropertyConduit conduit);

    // "Navigation" methods useful for the "fluent" style of configuration.

    /**
     * Delegates to {@link BeanEditorModel#get(String)}.
     */
    PropertyEditModel get(String propertyName);

    /**
     * Delegates to {@link BeanEditorModel#add(String)}.
     */
    PropertyEditModel add(String propertyName);

    /** Returns the containing model. */
    BeanEditorModel model();
}
