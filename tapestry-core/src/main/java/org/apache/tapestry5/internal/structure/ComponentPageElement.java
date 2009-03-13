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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ComponentResourcesCommon;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.InternalComponentResourcesCommon;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.runtime.*;
import org.slf4j.Logger;

/**
 * Defines an element of a page that is a component elements that are, in fact, components (rather than just static
 * markup).
 */
public interface ComponentPageElement extends ComponentResourcesCommon, InternalComponentResourcesCommon, RenderCommand, BodyPageElement, PageLifecycleListener
{
    /**
     * Returns the core component associated with this page element (as opposed to any mixins attached to the
     * component).
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
     * Used during the construction of a page. Adds a page element as part of the template for this page element. A page
     * element will eventually render by sequentially rendering these elements. A page elements template is really just
     * the outermost portions of the component's template ... where a template contains elements that are all
     * components, those components will receive portions of the template as their body.
     */
    void addToTemplate(RenderCommand element);

    /**
     * Used during the contruction of a page to add a non-anonymous Block to the component.
     *
     * @see ComponentResourcesCommon#getBlock(String)
     */
    void addBlock(String blockId, Block block);

    /**
     * Adds a mixin.
     *
     * @param mixinId      a unique id for the mixin, the last term of the mixin's class name
     * @param instantiator used to instantiate an instance of the mixin
     */
    void addMixin(String mixinId, Instantiator instantiator);

    /**
     * @param mixinId       id of previously added mixin
     * @param parameterName simple (unqualified) name of parameter
     * @param binding       binding for parameter
     * @since 5.1.0.0
     */
    void bindMixinParameter(String mixinId, String parameterName, Binding binding);

    /**
     * Retrieves a component page element by its id. The search is caseless.
     *
     * @param id used to locate the element
     * @return the page element
     * @throws IllegalArgumentException if no component exists with the given id
     */
    ComponentPageElement getEmbeddedElement(String id);

    /**
     * Returns the {@link org.apache.tapestry5.ComponentResources} for a mixin attached to this component element. Mixin
     * ids are the simple names of the mixin class.
     *
     * @param mixinId the mixin id (case insensitive)
     * @return the resources for the component
     * @throws IllegalArgumentException if no mixin with the given id exists
     */
    ComponentResources getMixinResources(String mixinId);

    /**
     * Invoked when the component should render its body.
     */
    void enqueueBeforeRenderBody(RenderQueue queue);

    /**
     * Asks each mixin and component to {@link Component#dispatchComponentEvent(ComponentEvent)}, returning true if any
     * handler was found.
     *
     * @param event to be handled
     * @return true if a handler was found
     */
    boolean dispatchEvent(ComponentEvent event);

    /**
     * Creates a new child component of the invoked component.  The new element will be added as an embedded element of
     * its container.
     *
     * @param id           simple id of the new component
     * @param nestedId
     * @param completeId
     * @param elementName  name of the component's element in its container's template
     * @param instantiator used to create a component instance, and access the component's model
     * @param location     location of the element within its container's template @return the new component
     */
    ComponentPageElement newChild(String id, String nestedId, String completeId, String elementName,
                                  Instantiator instantiator,
                                  Location location);

    /**
     * Returns a logger used to for logging event dispatch and event method invocation.
     */
    Logger getEventLogger();
}
