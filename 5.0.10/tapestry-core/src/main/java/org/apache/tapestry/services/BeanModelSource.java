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

package org.apache.tapestry.services;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.OrderBefore;

/**
 * Used by a component to create a default {@link BeanModel} for a particular bean class. Also provides support to the
 * model by generating validation information for individual fields.
 * <p/>
 * BeanModels are the basis for the {@link org.apache.tapestry.corelib.components.BeanEditor} and {@link
 * org.apache.tapestry.corelib.components.Grid} comopnents.
 *
 * @see org.apache.tapestry.services.PropertyConduitSource
 */
public interface BeanModelSource
{
    /**
     * Creates a new model used for editing the indicated bean class. The model will represent all read/write properties
     * of the bean. The order of the properties is defined by the {@link OrderBefore} annotation on the getter or setter
     * methods. The labels for the properties are derived from the property names, but if the component's message
     * catalog has keys of the form <code>propertyName-label</code>, then those will be used instead.
     * <p/>
     * Models are <em>mutable</em>, so they are not cached, a fresh instance is created each time.
     *
     * @param beanClass                class of object to be edited
     * @param filterReadOnlyProperties if true, then properties that are read-only will be skipped (leaving only
     *                                 read-write properties). If false, then both read-only and read-write properties
     *                                 will be included.
     * @param resources                used when resolving resources, especially component messages (used to access
     *                                 labels)
     * @return a model
     */
    BeanModel create(Class beanClass, boolean filterReadOnlyProperties, ComponentResources resources);
}
