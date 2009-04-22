// Copyright 2006, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.runtime;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.OnEvent;

/**
 * Interface that defines the lifecycle of a component, within a page, allowing for callbacks into the component for
 * many different events. This interface is part of the public API for Tapestry, but is <em>not</em> expected to be
 * directly implemented by component classes; it should only be implemented as part of the component class
 * transformation process.
 * <p/>
 * Most of the methods are related to render phases; see the corresponding annotations and component rendering
 * documentation to see how they relate to each other.
 */
public interface Component extends ComponentResourcesAware, PageLifecycleListener
{

    /**
     * Lifecycle method invoked at the end of the {@link org.apache.tapestry5.annotations.CleanupRender} render phase.
     * There is no annotation for this method, it is part of CleanupRender, but is always invoked. Its specific use is
     * to allow components to clean up cached parameter values.
     */
    void postRenderCleanup();

    /**
     * Invoked before rendering a component (or its template).
     */
    void setupRender(MarkupWriter writer, Event event);

    /**
     * Invoked to allow a component to render its tag (start tag and attributes).
     */
    void beginRender(MarkupWriter writer, Event event);

    /**
     * This phase is only invoked for components with templates.
     */
    void beforeRenderTemplate(MarkupWriter writer, Event event);

    /**
     * Invoked after rendering the template for a component (only for components with a template).
     */
    void afterRenderTemplate(MarkupWriter writer, Event event);

    /**
     * Invoked just before rendering the body of component.
     */
    void beforeRenderBody(MarkupWriter writer, Event event);

    /**
     * Invoked just after rendering the body of the component.
     */
    void afterRenderBody(MarkupWriter writer, Event event);

    /**
     * Generally used to write the close tag matching any open tag written by {@link
     * #beginRender(org.apache.tapestry5.MarkupWriter, Event)}.
     */
    void afterRender(MarkupWriter writer, Event event);

    /**
     * Generally used to perform final cleanup of the component after rendering.
     */
    void cleanupRender(MarkupWriter writer, Event event);

    /**
     * Invoked to handle a component event. Methods with the {@link OnEvent} annotation (or the matching naming
     * convention) will be invoked until one returns a non-null value.
     *
     * @param event
     * @return true if any handler was found (and invoked), false otherwise
     * @throws RuntimeException wrapping any checked exceptions that are thrown by individual event handler methods
     */
    boolean dispatchComponentEvent(ComponentEvent event);
}
