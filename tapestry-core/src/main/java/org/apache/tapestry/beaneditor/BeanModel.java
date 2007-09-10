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

import java.util.List;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.services.BeanModelSource;

/**
 * Provides the information necessary to build a user interface to view, create or edit an instance
 * of a particular type.
 * <p>
 * BeanModels are not thread-safe, they are also not serializable.
 * 
 * @see BeanModelSource
 */
public interface BeanModel
{
    /** Returns the type of bean for which this model was initially created. */
    Class getBeanType();

    /**
     * Returns a list of the editable properties of the bean, in <em>presentation</em> order.
     */
    List<String> getPropertyNames();

    /**
     * Returns the named model.
     * 
     * @param propertyName
     *            name of property to retrieve model for (case is ignored)
     * @return the model for the property
     * @throws RuntimeException
     *             if the bean editor model does not have a property model for the provided name
     */
    PropertyModel get(String propertyName);

    /**
     * Adds a new property to the model, returning its mutable model for further refinement. The
     * property is added to the <em>end</em> of the list of properties.
     * 
     * @param propertyName
     *            name of property to add
     * @return the new property model (for further configuration)
     * @throws RuntimeException
     *             if the property already exists
     */
    PropertyModel add(String propertyName);

    /**
     * Adds a new property to the model, ordered before or after an existing property.
     * 
     * @param position
     *            controls whether the new property is ordered before or after the existing property
     * @param existingPropertyName
     *            the name of an existing property (this must exist)
     * @param propertyName
     *            the new property to add
     * @throws RuntimeException
     *             if the existing property does not exist, or if the new property already does
     *             exist
     * @return the new property model (for further configuration)
     */
    PropertyModel add(RelativePosition position, String existingPropertyName, String propertyName);

    /**
     * Adds a new property to the model, ordered before or after an existing property.
     * 
     * @param position
     *            controls whether the new property is ordered before or after the existing property
     * @param existingPropertyName
     *            the name of an existing property (this must exist)
     * @param propertyName
     *            the new property to add
     * @param conduit
     *            conduit used to read or update the property; this may be null for a synthetic or
     *            placeholder property
     * @throws RuntimeException
     *             if the existing property does not exist, or if the new property already does
     *             exist
     * @return the new property model (for further configuration)
     */
    PropertyModel add(RelativePosition position, String existingPropertyName, String propertyName,
            PropertyConduit conduit);

    /**
     * Adds a new property to the model, returning its mutable model for further refinement.
     * 
     * @param propertyName
     *            name of property to add
     * @param conduit
     *            the conduit used to read or update the property; this may be null for a synthetic
     *            or placeholder property
     * @return the model for the property
     * @throws RuntimeException
     *             if the property already exists
     */
    PropertyModel add(String propertyName, PropertyConduit conduit);

    /**
     * Removes the named properties from the model, if present. It is not considered an error to
     * remove a property that does not exist.
     * 
     * @param propertyName
     *            the names of properties to be removed (case insensitive)
     * @return the model for further modifications
     */
    BeanModel remove(String... propertyName);

    /**
     * Re-orders the properties of the model into the specified order. Existing properties that are
     * not indicated are retained, but ordered to the end of the list.
     * 
     * @param propertyName
     *            property names in order they should be displayed (case insensitive)
     * @return the model for further modifications
     */
    BeanModel reorder(String... propertyName);
}
