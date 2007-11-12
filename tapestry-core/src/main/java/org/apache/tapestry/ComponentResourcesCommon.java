// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry;

import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.internal.services.OnEventWorker;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.ioc.Locatable;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.services.ComponentSource;
import org.slf4j.Logger;

import java.util.Locale;

/**
 * Operations shared by the public {@link ComponentResources} interface and
 * {@link ComponentPageElement} interface (on the internal side).
 */
public interface ComponentResourcesCommon extends Locatable
{
    /**
     * Returns the id of the component. The id will be unique within the component's immediate
     * container. For a page's root component, the value null is returned.
     */
    String getId();

    /**
     * Return a string consisting the concatinated ids of all containing components, separated by
     * periods. In addition, nested ids are always all lower case. I.e., "foo.bar.baz". Returns null
     * for a page.
     */
    String getNestedId();

    /**
     * Creates a component action request link as a callback for this component.
     *
     * @param action  a name for the action associated with the link
     * @param forForm if true, the link will be used as the action for an HTML form submission, which
     *                may affect what information is encoded into the link
     * @param context additional objects to be encoded into the path portion of the link; each is
     *                converted to a string an URI encoded
     */
    Link createActionLink(String action, boolean forForm, Object... context);

    /**
     * Creates a render request link to a specific page.
     *
     * @param pageName the logical name of the page to link to
     * @param override if true, the context is used even if empty (normally, the target page is allowed
     *                 to passivate, providing a context, when the provided context is empty)
     * @param context  the activation context for the page. If omitted, the activation context is
     *                 obtained from the target paget
     */
    Link createPageLink(String pageName, boolean override, Object... context);

    /**
     * Returns a string consisting of the fully qualified class name of the containing page, and the
     * {@link #getNestedId() nested id} of this component, separated by a colon. I.e.,
     * "MyPage:foo.bar.baz". For a page, returns just the page's logical name.
     * <p/>
     * This value is often used to obtain an equivalent component instance in a later request.
     *
     * @see ComponentSource
     */

    String getCompleteId();

    /**
     * Triggers a component event. A search for an event handling method will occur, first in the
     * component, then its container, and so on. When a matching event handler method is located, it
     * is invoked. If the method returns a value, the value is passed to the handler (if handler is
     * null, then it is an error for a method to return a non-null vavlue).
     * <p/>
     * Resolution of event type to event handler methods is case insensitive.
     *
     * @param eventType event type (as determined from the request, or otherwise by design)
     * @param context   the context (as extracted from the request, or provided by the triggering
     *                  component); these values may be provided to event handler methods via their
     *                  parameters (may be null)
     * @param handler   the handler to be informed of the result, or null if the event is a notification
     *                  that does not support return values from event handler methods (the value true is
     *                  allowed even if the handler is null).
     * @return true if any event handler was invoked (even if no event handler method returns a
     *         non-null value)
     * @see OnEventWorker
     * @see OnEvent
     */
    boolean triggerEvent(String eventType, Object[] context, ComponentEventHandler handler);

    /**
     * Returns true if the component is currently rendering, false otherwise. This is most often
     * used to determine if parameter values should be cached.
     */
    boolean isRendering();

    /**
     * Returns the log instance associated with the component (which is based on the component or
     * mixin's class name).
     *
     * @see ComponentModel#getLogger()
     */
    Logger getLogger();

    /**
     * Returns the locale for the page containing this component.
     */
    Locale getLocale();

    /**
     * Returns the name of element that represents the component in its template, or null if the
     * element was a component type (in the Tapestry namespace).
     *
     * @return the element name
     */
    String getElementName();

    /**
     * Returns a block from the component's template, referenced by its id.
     *
     * @param blockId the id of the block (case insensitive)
     * @return the identified Block
     * @throws BlockNotFoundException if no block with the given id exists
     * @see #findBlock(String)
     */
    Block getBlock(String blockId);

    /**
     * As with {@link #getBlock(String)}, but returns null if the block is not found.
     *
     * @param blockId the id of the block (case insensitive)
     * @return the block, or null
     */
    Block findBlock(String blockId);

    /**
     * Returns the <em>logical</em> name of the page containing this component. This is the short
     * name (it often appears in URLs)
     *
     * @return the logical name of the page which contains this component
     */
    String getPageName();
}
