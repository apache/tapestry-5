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

package org.apache.tapestry5;

import org.apache.tapestry5.ioc.Locatable;
import org.slf4j.Logger;

import java.util.Locale;

/**
 * Operations shared by the public {@link org.apache.tapestry5.ComponentResources} interface and {@link
 * org.apache.tapestry5.internal.structure.ComponentPageElement} interface (on the internal side).
 */
@SuppressWarnings({ "JavaDoc" })
public interface ComponentResourcesCommon extends Locatable
{
    /**
     * Returns the simple (or local) id of the component. The id will be unique within the component's immediate
     * container. For a page's root component, the value null is returned.
     */
    String getId();

    /**
     * Return a string consisting the concatinated ids of all containing components, separated by periods. In addition,
     * nested ids are always all lower case. I.e., "foo.bar.baz". Returns null for the root component of a page.
     */
    String getNestedId();


    /**
     * Returns a string consisting of the logical name of the containing page, and the {@link #getNestedId() nested id}
     * of this component, separated by a colon. I.e., "MyPage:foo.bar.baz". For a page, returns just the page's name.
     * <p/>
     * This value is often used to obtain an equivalent component instance in a later request.
     *
     * @see org.apache.tapestry5.services.ComponentSource#getComponent(String)
     */
    String getCompleteId();

    /**
     * A convienience for invoking {@link #triggerContextEvent(String, EventContext , ComponentEventCallback)}. Wraps
     * the context values into an {@link org.apache.tapestry5.EventContext}.
     *
     * @param eventType     event type (as determined from the request, or otherwise by design)
     * @param contextValues Values that may be provided to the event handler method as method parameters, or null if no
     *                      context values are available
     * @param callback      the handler to be informed of the result, or null if the event is a notification that does
     *                      not support return values from event handler methods (the value true is allowed even if the
     *                      handler is null).
     * @return true if any event handler was invoked (even if no event handler method returns a non-null value)
     * @throws org.apache.tapestry5.runtime.ComponentEventException
     *          if an event handler method throws a checked or unchecked exception
     * @see org.apache.tapestry5.internal.transform.OnEventWorker
     * @see org.apache.tapestry5.annotations.OnEvent
     */
    boolean triggerEvent(String eventType, Object[] contextValues, ComponentEventCallback callback);

    /**
     * Triggers a component event. A search for an event handling method will occur, first in the component, then its
     * container, and so on. When a matching event handler method is located, it is invoked. If the method returns a
     * value, the value is passed to the callback (if callback is null, then it is an error for a method to return a
     * non-null value).
     * <p/>
     * Resolution of event type to event handler methods is case insensitive.
     *
     * @param eventType event type (as determined from the request, or otherwise by design)
     * @param context   the context (as extracted from the request, or provided by the triggering component); these
     *                  values may be provided to event handler methods via their parameters (may not be null)
     * @param callback  the handler to be informed of the result, or null if the event is a notification that does not
     *                  support return values from event handler methods (the value true is allowed even if the handler
     *                  is null).
     * @return true if any event handler was invoked (even if no event handler method returns a non-null value)
     * @throws org.apache.tapestry5.runtime.ComponentEventException
     *          if an event handler method throws a checked or unchecked exception
     * @see org.apache.tapestry5.internal.transform.OnEventWorker
     * @see org.apache.tapestry5.annotations.OnEvent
     */
    boolean triggerContextEvent(String eventType, EventContext context, ComponentEventCallback callback);

    /**
     * Returns true if the component is currently rendering, false otherwise. This is most often used to determine if
     * parameter values should be cached.
     */
    boolean isRendering();

    /**
     * Returns the log instance associated with the component (which is based on the component or mixin's class name).
     *
     * @see org.apache.tapestry5.model.ComponentModel#getLogger()
     */
    Logger getLogger();

    /**
     * Returns the locale for the page containing this component.
     */
    Locale getLocale();

    /**
     * Returns the name of element that represents the component in its template, or the provided default element name
     * if the element was a component type (in the Tapestry namespace).
     *
     * @param defaultElementName element name to return if the element name is not known (may be null)
     * @return the element name
     */
    String getElementName(String defaultElementName);

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
     * Returns the <em>logical</em> name of the page containing this component. This is the short name (it often appears
     * in URLs)
     *
     * @return the logical name of the page which contains this component
     */
    String getPageName();


    /**
     * Returns true if the element has a body and false otherwise.  Only components may have a body; pages and mixins
     * will return false.
     */
    boolean hasBody();

    /**
     * Returns the body of this component as a (possibly empty) block.  When invoked on a mixin, returns the containing
     * component's body.
     */
    Block getBody();

    /**
     * Creates a component event request link as a callback for this component. The event type and context (as well as
     * the page name and nested component id) will be encoded into a URL. A request for the URL will {@linkplain
     * #triggerEvent(String, Object[], org.apache.tapestry5.ComponentEventCallback)}  trigger} the named event on the
     * component.
     *
     * @param eventType the type of event to be triggered.  Event types should be Java identifiers (contain only
     *                  letters, numbers and the underscore).
     * @param context   additional objects to be encoded into the path portion of the link; each is converted to a
     *                  string and URI encoded
     * @return link object for the callback
     */
    Link createEventLink(String eventType, Object... context);

    /**
     * Creates a component event request link as a callback for this component. The event type and context (as well as
     * the page name and nested component id) will be encoded into a URL. A request for the URL will {@linkplain
     * #triggerEvent(String, Object[], org.apache.tapestry5.ComponentEventCallback)}  trigger} the named event on the
     * component.
     *
     * @param eventType the type of event to be triggered.  Event types should be Java identifiers (contain only
     *                  letters, numbers and the underscore).
     * @param forForm   if true, the link will be used as the eventType for an HTML form submission, which may affect
     *                  what information is encoded into the link
     * @param context   additional objects to be encoded into the path portion of the link; each is converted to a
     *                  string and URI encoded
     * @return link object for the callback
     * @deprecated Use {@link #createEventLink(String, Object[])} instead
     */
    Link createActionLink(String eventType, boolean forForm, Object... context);

    /**
     * Creates a component event request link as a callback for this component. The event type and context (as well as
     * the page name and nested component id) will be encoded into a URL. A request for the URL will {@linkplain
     * #triggerEvent(String, Object[], org.apache.tapestry5.ComponentEventCallback)}  trigger} the named event on the
     * component. This is only used for form submission events, as extra data may be encoded in the form as hidden
     * fields.
     *
     * @param eventType the type of event to be triggered.  Event types should be Java identifiers (contain only
     *                  letters, numbers and the underscore).
     * @param context   additional objects to be encoded into the path portion of the link; each is converted to a
     *                  string and URI encoded
     * @return link object for the callback
     */
    Link createFormEventLink(String eventType, Object... context);

    /**
     * Creates a page render request link to render a specific page.
     *
     * @param pageName the logical name of the page to link to
     * @param override if true, the context is used even if empty (normally, the target page is allowed to passivate,
     *                 providing a context, when the provided context is empty)
     * @param context  the activation context for the page. If omitted, the activation context is obtained from the
     *                 target page
     * @return link for a render request to the targetted page
     * @deprecated Use {@link org.apache.tapestry5.services.PageRenderLinkSource#createPageRenderLink(String)} or {@link
     *             org.apache.tapestry5.services.PageRenderLinkSource#createPageRenderLinkWithContext(String, Object[])}
     *             instead
     */
    Link createPageLink(String pageName, boolean override, Object... context);

    /**
     * Creates a page render request link to render a specific page. Using a page class, rather than a page name, is
     * more refactoring safe (in the even the page is renamed or moved).
     *
     * @param pageClass identifies the page to link to
     * @param override  if true, the context is used even if empty (normally, the target page is allowed to passivate,
     *                  providing a context, when the provided context is empty)
     * @param context   the activation context for the page. If omitted, the activation context is obtained from the
     *                  target page
     * @return link for a render request to the targetted page
     * @deprecated Use {@link org.apache.tapestry5.services.PageRenderLinkSource#createPageRenderLink(Class)} or {@link
     *             org.apache.tapestry5.services.PageRenderLinkSource#createPageRenderLinkWithContext(Class, Object[])}
     *             instead
     */
    Link createPageLink(Class pageClass, boolean override, Object... context);
}
