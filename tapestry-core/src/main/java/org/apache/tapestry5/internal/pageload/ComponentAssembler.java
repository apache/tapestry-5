// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;

import java.util.Locale;
import java.util.Map;

/**
 * Encapsulates a series of actions that are used to assemble a new page instance (or a comoponent within the page).
 */
interface ComponentAssembler
{
    /**
     * The model defining the component assembled by this assembler.
     */
    ComponentModel getModel();

    /**
     * Assembles and returns page's root component
     *
     * @param page to assemble
     */
    ComponentPageElement assembleRootComponent(Page page);

    /**
     * Assembles a component embedded within another component, leaving the new component on the {@link
     * org.apache.tapestry5.internal.pageload.PageAssembly#createdElement} stack.
     *
     * @param pageAssembly      holds dynamic state while assembling the comopnent
     * @param embeddedAssembler
     * @param embeddedId        the unique id for the component within its container
     * @param elementName       element name in the template for the component (or null if defined via a Tapestry
     *                          namespaced element)
     * @param location          location of the embedded component in its container's template
     */
    void assembleEmbeddedComponent(PageAssembly pageAssembly, EmbeddedComponentAssembler embeddedAssembler,
                                   String embeddedId, String elementName,
                                   Location location);

    /**
     * Adds a page assembly action for this component
     *
     * @param action to be performed when assembling a page
     */
    void add(PageAssemblyAction action);

    /**
     * Validates that all component ids defined by the model are accounted for in the template. In addition, takes care
     * of id pre-allocation.
     */
    void validateEmbeddedIds(Map<String, Location> componentIds, Resource templateResource);

    /**
     * Generates an id for an otherwise anonymous component, based on the component's type.
     *
     * @param componentType
     * @return unique id based on the type
     */
    String generateEmbeddedId(String componentType);

    /**
     * Creates an assembler for an embedded component within this component. Does some additional tracking of published
     * parameters.
     *
     * @param embeddedId         unique id for the embedded component
     * @param componentClassName class name to instantiate
     * @param embeddedModel      model defining how the component is used (may be null)
     * @param mixins             mixins for the component (as defined in the template)
     * @param location           location of the component (i.e., it's location in the container template)
     * @return assembler for the component
     */
    EmbeddedComponentAssembler createEmbeddedAssembler(String embeddedId, String componentClassName,
                                                       EmbeddedComponentModel embeddedModel,
                                                       String mixins,
                                                       Location location);

    /**
     * Finds a binder for a published parameter, or returns null. That is, if the parameter name matches the name of a
     * parameter of an emebdded components of this component, returns a parameter binder for the parameter. The caller
     * will pass the {@link org.apache.tapestry5.internal.structure.ComponentPageElement} that corresponds to this
     * component to the binder and the binder will, internally, redirect to the correct embedded ComponentPageElement.
     *
     * @param parameterName simple (unqualified) name of parameter
     * @return binder, or null if the parameter name does not correspond to a published parameter of an embedded
     *         component
     */
    ParameterBinder getBinder(String parameterName);

    /**
     * Returns the locale for which the component is being assembled.
     */
    Locale getLocale();
}
