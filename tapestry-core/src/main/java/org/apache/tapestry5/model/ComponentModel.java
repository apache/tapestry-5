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

import org.apache.tapestry5.annotations.MixinAfter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.ioc.Resource;
import org.slf4j.Logger;

import java.util.List;
import java.util.Set;

/**
 * Defines a component in terms of its capabilities, parameters, sub-components, etc. During <em>runtime</em>, the
 * component model is immutable. During <em>construction</em> time, when the class is being transformed and loaded, the
 * model is mutable.
 *
 * @see org.apache.tapestry5.model.MutableComponentModel
 */
public interface ComponentModel
{
    /**
     * Returns the resource corresponding to the class file for this component. This is used to find related resources,
     * such as the component's template and message catalog.
     */
    Resource getBaseResource();

    /**
     * The FQCN of the component.
     */
    String getComponentClassName();

    /**
     * Returns the ids of all embedded components defined within the component class (via the {@link
     * org.apache.tapestry5.annotations.Component} annotation), including those defined by any super-class.
     */
    List<String> getEmbeddedComponentIds();

    /**
     * Returns an embedded component defined by this component or by a super-class.
     *
     * @param componentId the id of the embedded component
     * @return the embedded component model, or null if no component exists with that id
     */
    EmbeddedComponentModel getEmbeddedComponentModel(String componentId);

    /**
     * Returns the persistent strategy associated with the field.
     *
     * @param fieldName
     * @return the corresponding strategy, or the empty string
     * @throws IllegalArgumentException if the named field is not marked as persistent
     */
    String getFieldPersistenceStrategy(String fieldName);

    /**
     * Returns object that will be used to log warnings and errors related to this component.
     *
     * @see org.apache.tapestry5.annotations.Log
     */
    Logger getLogger();

    /**
     * Returns a list of the class names of mixins that are part of the component's implementation.
     */
    List<String> getMixinClassNames();

    /**
     * Return a single parameter model by parameter name, or null if the parameter is not defined.
     *
     * @param parameterName the name of the parameter (case is ignored)
     */
    ParameterModel getParameterModel(String parameterName);

    /**
     * Returns an alphabetically sorted list of the names of all formal parameters. This includes parameters defined by
     * a base class.
     */

    List<String> getParameterNames();

    /**
     * Returns an alphabetically sorted list of the names of all formal parameters defined by this specific class
     * (parameters inherited from base classes are not identified).
     */
    List<String> getDeclaredParameterNames();

    /**
     * Returns a list of the names of all persistent fields (within this class, or any super-class). The names are
     * sorted alphabetically.
     *
     * @see Persist
     */
    List<String> getPersistentFieldNames();

    /**
     * Returns true if the modeled component is a root class, a component class whose parent class is not a component
     * class.  We may in the future require that components only extend from Object.
     *
     * @return true if a root class, false if a subclass
     */
    boolean isRootClass();

    /**
     * Returns true if the model indicates that informal parameters, additional parameters beyond the formal parameter
     * defined for the component, are supported. This is false in most cases, but may be set to true for specific
     * classes (when the {@link SupportsInformalParameters} annotation is present, or inherited from a super-class).
     *
     * @return true if this component model supports informal parameters
     */
    boolean getSupportsInformalParameters();

    /**
     * Returns the component model for this component's super-class, if it exists. Remember that only classes in the
     * correct packages, are considered component classes.
     *
     * @return the parent class model, or null if this component's super class is not itself a component class
     */
    ComponentModel getParentModel();

    /**
     * Relevant for component mixins only. Indicates that the mixin behavior should occur <em>after</em> (not before)
     * the component. Normally, this flag is set by the presence of the {@link MixinAfter} annotation.
     *
     * @return true if the mixin should operate after, not before, the component
     */
    boolean isMixinAfter();

    /**
     * Gets a meta value identified by the given key. If the current model does not provide a value for the key, then
     * the parent component model (if any) is searched.
     *
     * @param key identifies the value to be accessed
     * @return the value for the key (possibly inherited from a parent model), or null
     */
    String getMeta(String key);

    /**
     * Returns a set of all the render phases that this model (including parent models) that are handled. Render phases
     * are represented by the corresponding annotation ({@link org.apache.tapestry5.annotations.BeginRender}, {@link
     * org.apache.tapestry5.annotations.AfterRender}, etc.).
     *
     * @return set of classes
     * @since 5.0.19, 5.1.0.0
     */
    Set<Class> getHandledRenderPhases();

    /**
     * Determines if the component has an event handler for the indicated event name (case insensitive). This includes
     * handles in the component class itself, or its super-classes, but does not include event handles supplied by
     * implementation or instance mixins.
     *
     * @param eventType name of event to check (case insensitive)
     * @return true if event handler present
     */
    boolean handlesEvent(String eventType);
}
