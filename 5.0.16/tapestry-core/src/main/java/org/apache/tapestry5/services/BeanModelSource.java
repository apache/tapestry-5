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

package org.apache.tapestry5.services;

import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.ioc.Messages;

/**
 * Used by a component to create a default {@link org.apache.tapestry5.beaneditor.BeanModel} for a particular bean
 * class. Also provides support to the model by generating validation information for individual fields.
 * <p/>
 * BeanModels are the basis for the {@link org.apache.tapestry5.corelib.components.BeanEditor} and {@link
 * org.apache.tapestry5.corelib.components.Grid} comopnents.
 *
 * @see org.apache.tapestry5.services.PropertyConduitSource
 */
public interface BeanModelSource
{
    /**
     * Creates a new model used for editing the indicated bean class. The model will represent all read/write properties
     * of the bean. The order of properties is determined from the order of the getter methods in the code, and can be
     * overridden with the {@link org.apache.tapestry5.beaneditor.ReorderProperties} annotation. The labels for the
     * properties are derived from the property names, but if the component's message catalog has keys of the form
     * <code>propertyName-label</code>, then those will be used instead.
     * <p/>
     * Models are <em>mutable</em>, so they are not cached, a fresh instance is created each time.
     *
     * @param beanClass                class of object to be edited
     * @param filterReadOnlyProperties if true, then properties that are read-only will be skipped (leaving only
     *                                 read-write properties, appropriate for {@link org.apache.tapestry5.corelib.components.BeanEditForm},
     *                                 etc.). If false, then both read-only and read-write properties will be included
     *                                 (appropriate for {@link org.apache.tapestry5.corelib.components.Grid} or {@link
     *                                 org.apache.tapestry5.corelib.components.BeanDisplay}).
     * @param messages                 Used to find explicit overrides of
     * @return a model
     * @deprecated use {@link #createDisplayModel(Class, org.apache.tapestry5.ioc.Messages)} or {@link
     *             #createEditModel(Class, org.apache.tapestry5.ioc.Messages)}
     */
    <T> BeanModel<T> create(Class<T> beanClass, boolean filterReadOnlyProperties, Messages messages);

    /**
     * Creates a model for display purposes; this may include properties which are read-only.
     *
     * @param beanClass class of object to be edited
     * @param messages
     * @return a model containing properties that can be presented to the user
     */
    <T> BeanModel<T> createDisplayModel(Class<T> beanClass, Messages messages);

    /**
     * Creates a model for edit and update purposes, only properties that are fully read-write are included.
     *
     * @param beanClass class of object to be edited
     * @param messages
     * @return a model containing properties that can be presented to the user
     */
    <T> BeanModel<T> createEditModel(Class<T> beanClass, Messages messages);
}
