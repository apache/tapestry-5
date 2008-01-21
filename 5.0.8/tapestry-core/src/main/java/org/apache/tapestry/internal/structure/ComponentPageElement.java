// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.Block;
import org.apache.tapestry.ComponentResourcesCommon;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.InternalComponentResourcesCommon;
import org.apache.tapestry.internal.services.Instantiator;
import org.apache.tapestry.model.ParameterModel;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.ComponentEvent;
import org.apache.tapestry.runtime.RenderQueue;

/**
 * Extended version of {@link org.apache.tapestry.internal.structure.PageElement} for elements that
 * are, in fact, components (rather than just static markup).
 */
public interface ComponentPageElement extends ComponentResourcesCommon, InternalComponentResourcesCommon, PageElement, BodyPageElement
{
    /**
     * Returns the core component associated with this page element (as opposed to any mixins
     * attached to the component).
     */
    Component getComponent();

    /**
     * Returns the resources associated with the core component.
     */
    InternalComponentResources getComponentResources();

    /**
     * Returns the page which contains this component.
     */
    Page getContainingPage();

    /**
     * Containing component (or null for the root component of a page).
     */
    ComponentPageElement getContainerElement();

    /**
     * Used during the construction of a page. Adds a page element as part of the template for this
     * page element. A page element will eventually render by sequentially rendering these elements.
     * A page elements template is really just the outermost portions of the component's template
     * ... where a template contains elements that are all components, those components will receive
     * portions of the template as their body.
     */
    void addToTemplate(PageElement element);

    /**
     * Used during the contruction of a page to add a non-anonymous Block to the component.
     *
     * @see ComponentResourcesCommon#getBlock(String)
     */
    void addBlock(String blockId, Block block);

    /**
     * Adds a component to its container. The embedded component's id must be unique within the
     * container (after the id is converted to lower case).
     */
    void addEmbeddedElement(ComponentPageElement child);

    /**
     * Adds a mixin.
     *
     * @param instantiator used to instantiate an instance of the mixin
     */
    void addMixin(Instantiator instantiator);

    /**
     * Retrieves a component page element by its id. The search is caseless.
     *
     * @param id used to locate the element
     * @return the page element
     * @throws IllegalArgumentException if no component exists with the given id
     */
    ComponentPageElement getEmbeddedElement(String id);

    /**
     * Invoked when the component should render its body.
     */
    void enqueueBeforeRenderBody(RenderQueue queue);

    /**
     * Asks each mixin and component to {@link Component#handleComponentEvent(ComponentEvent)},
     * returning true if any handler was found.
     *
     * @param event to be handled
     * @return true if a handler was found
     */
    boolean handleEvent(ComponentEvent event);

    /**
     * Searches the component (and its mixins) for a formal parameter matching the given name. If
     * found, the {@link ParameterModel#getDefaultBindingPrefix() default binding prefix} is
     * returned. Otherwise the parameter is an informal parameter, and null is returned.
     *
     * @param parameterName the name of the parameter, possibly qualified with the mixin class name
     * @return the default binding prefix, or null
     */
    String getDefaultBindingPrefix(String parameterName);
}
