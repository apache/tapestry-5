// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import java.util.List;

/**
 * Provides the information necessary to build a user interface to view, create or edit an instance of a particular
 * type.
 * <p/>
 * BeanModels are not thread-safe, they are also not serializable.
 * <p/>
 * Here, and in {@link org.apache.tapestry5.beaneditor.PropertyModel}, the term "propertyName" is used for simplicitly.
 * However, a full {@linkplain org.apache.tapestry5.services.PropertyConduitSource#create(Class, String) property
 * expression} may be utilized when {@linkplain #add(String) adding new properties to an existing BeanModel}.
 *
 * @see org.apache.tapestry5.services.BeanModelSource
 */
public interface BeanModel<T>
{
    /**
     * Returns the type of bean for which this model was initially created.
     */
    Class<T> getBeanType();


    /**
     * Creates a new bean instance.  This is based on {@link org.apache.tapestry5.ioc.ObjectLocator#autobuild(Class)},
     * so a public constructor will be used, and dependencies injected.
     *
     * @return new instance of the bean
     */
    T newInstance();

    /**
     * Returns a list of the editable properties of the bean, in <em>presentation</em> order.
     */
    List<String> getPropertyNames();

    /**
     * Returns the named model.
     *
     * @param propertyName name of property to retrieve model for (case is ignored)
     * @return the model for the property
     * @throws RuntimeException if the bean editor model does not have a property model for the provided name
     */
    PropertyModel get(String propertyName);

    /**
     * Returns the identified model.  Property ids are a stripped version of the property name. Case is ignored.
     *
     * @param propertyId matched caselessly against {@link org.apache.tapestry5.beaneditor.PropertyModel#getId()}
     * @throws RuntimeException if the bean editor model does not have a property model with the indicated id
     */
    PropertyModel getById(String propertyId);

    /**
     * Adds a new property to the model, returning its mutable model for further refinement. The property is added to
     * the <em>end</em> of the list of properties. The property must be real (but may have been excluded if there was no
     * {@linkplain org.apache.tapestry5.beaneditor.DataType datatype} associated with the property). To add a synthetic
     * property, use {@link #add(String, org.apache.tapestry5.PropertyConduit)}
     *
     * @param propertyName name of property to add
     * @return the new property model (for further configuration)
     * @throws RuntimeException if the property already exists
     */
    PropertyModel add(String propertyName);

    /**
     * Adds a new property to the model (as with {@link #add(String)}), ordered before or after an existing property.
     *
     * @param position             controls whether the new property is ordered before or after the existing property
     * @param existingPropertyName the name of an existing property (this must exist)
     * @param propertyName         the new property to add
     * @return the new property model (for further configuration)
     * @throws RuntimeException if the existing property does not exist, or if the new property already does exist
     */
    PropertyModel add(RelativePosition position, String existingPropertyName, String propertyName);

    /**
     * Adds a new property to the model, ordered before or after an existing property.
     *
     * @param position             controls whether the new property is ordered before or after the existing property
     * @param existingPropertyName the name of an existing property (this must exist)
     * @param propertyName         the new property to add
     * @param conduit              conduit used to read or update the property; this may be null for a synthetic or
     *                             placeholder property
     * @return the new property model (for further configuration)
     * @throws RuntimeException if the existing property does not exist, or if the new property already does exist
     */
    PropertyModel add(RelativePosition position, String existingPropertyName, String propertyName,
                      PropertyConduit conduit);

    /**
     * Adds a new, synthetic property to the model, returning its mutable model for further refinement.
     *
     * @param propertyName name of property to add
     * @param conduit      the conduit used to read or update the property; this may be null for a synthetic or
     *                     placeholder property
     * @return the model for the property
     * @throws RuntimeException if the property already exists
     */
    PropertyModel add(String propertyName, PropertyConduit conduit);

    /**
     * Removes the named properties from the model, if present. It is not considered an error to remove a property that
     * does not exist.
     *
     * @param propertyNames the names of properties to be removed (case insensitive)
     * @return the model for further modifications
     */
    BeanModel exclude(String... propertyNames);

    /**
     * Re-orders the properties of the model into the specified order. Existing properties that are not indicated are
     * retained, but ordered to the end of the list.
     *
     * @param propertyNames property names in order they should be displayed (case insensitive)
     * @return the model for further modifications
     */
    BeanModel reorder(String... propertyNames);

    /**
     * Re-orders the properties of the model into the specified order. Existing properties that are not indicated are
     * <<removed>>.
     *
     * @param propertyNames the names of properties to be retained
     * @return the model for further modifications
     */
    BeanModel include(String... propertyNames);
}
