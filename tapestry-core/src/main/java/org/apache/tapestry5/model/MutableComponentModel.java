// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.model;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.ioc.Location;

/**
 * Mutable version of {@link org.apache.tapestry5.model.ComponentModel} used during the transformation phase.
 */
public interface MutableComponentModel extends ComponentModel
{
    /**
     * Adds a new formal parameter to the model. Each parameter has a unique name (though access to parameters is case
     * insensitive).
     *
     * @param name                 new, unique name for the parameter
     * @param required             if true, the parameter must be bound
     * @param allowNull            if true, then parameter may be bound to null, if false a null check will be added
     * @param defaultBindingPrefix the default binding prefix for this parameter @throws IllegalArgumentException if a
     *                             parameter with the given name has already been defined for this model
     * @see Parameter
     */
    void addParameter(String name, boolean required, boolean allowNull, String defaultBindingPrefix);

    /**
     * Defines a new embedded component.
     *
     * @param id                        the unique id for the embedded component, which must not already exist.
     * @param type                      the type of the component (posslibly blank)
     * @param componentClassName        the fully qualified class name (derived from the field), used if the type is
     *                                  blank
     * @param inheritInformalParameters if true, then the component will inherit informal parameters provided to its
     *                                  container
     * @param location                  where the component is defined @return a mutable model allowing parameters to be
     *                                  set
     */
    MutableEmbeddedComponentModel addEmbeddedComponent(String id, String type, String componentClassName,
                                                       boolean inheritInformalParameters, Location location);

    /**
     * Used to define the field persistence strategy for a particular field name. Returns a logical name for the field,
     * which is guaranteed to be unique (this is necessary for handling the case where a subclass has a persistent field
     * with the same name as a persistent field from a super-class).
     *
     * @param fieldName the name of the field which is to be made persistent
     * @param strategy  the strategy for persisting the field, from {@link Persist#value()}. This value may be blank, in
     *                  which case the stategy is inherited from the component, or the component's container.
     * @return a logical name for the field, to be used with {@link ComponentModel#getFieldPersistenceStrategy(String)},
     *         and with {@link InternalComponentResources#persistFieldChange(String, Object)}, etc.
     */
    String setFieldPersistenceStrategy(String fieldName, String strategy);

    /**
     * Adds a mixin to the component's implementation.
     */
    void addMixinClassName(String mixinClassName);

    /**
     * Sets the internal flag to indicate that this model (and all models that extend from it) support informal
     * parameters.
     */
    void enableSupportsInformalParameters();

    /**
     * Changes the value of the mixinAfter flag. The default value is false.
     */
    void setMixinAfter(boolean mixinAfter);

    /**
     * Stores a meta data value under the indicated key.
     */
    void setMeta(String key, String value);

    /**
     * Identifies that the component does handle the render phase.
     *
     * @param renderPhase annotation class corresponding to the render phase
     * @see ComponentModel#getHandledRenderPhases()
     * @since 5.0.19, 5.1.0.0
     */
    void addRenderPhase(Class renderPhase);

    /**
     * Identifies that the component includes an event handler for the indicated event type.
     *
     * @param eventType of handled event
     * @since 5.1.0.0
     */
    void addEventHandler(String eventType);
}
