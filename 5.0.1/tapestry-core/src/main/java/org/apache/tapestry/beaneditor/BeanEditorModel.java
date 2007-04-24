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

/**
 * Provides the information necessary to build a user interface to create or edit an instance of a
 * particular type.
 * <p>
 * BeanEditorModels are not threadsafe, they are also not serializable.
 */
public interface BeanEditorModel
{
    /**
     * Returns a list of the editable properties of the bean, in <em>presentation</em> order.
     * 
     * @return
     */
    List<String> getPropertyNames();

    /**
     * Returns the named model.
     * 
     * @param propertyName
     *            name of property to retrieve model for
     * @return the model for the property
     * @throws RuntimeException
     *             if the bean editor model does not have a property model for the provided name
     */
    PropertyEditModel get(String propertyName);

    /**
     * Adds a new property to the model, returning its mutable model for further refinement.
     * 
     * @param propertyName
     *            name of property to add
     * @return the model for the property
     * @throws RuntimeException
     *             if the property already exists
     */
    PropertyEditModel add(String propertyName);
}
