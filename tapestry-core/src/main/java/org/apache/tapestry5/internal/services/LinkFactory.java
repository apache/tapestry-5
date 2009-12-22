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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.structure.Page;

/**
 * A source for {@link Link} objects.
 *
 * @see LinkFactoryListener
 */
public interface LinkFactory
{
    /**
     * Creates a stateful action link. Action links are built for components. Action links are encoded by the current
     * request (that is, bound to the current request's session, if any).
     *
     * @param page
     * @param nestedId
     * @param eventType the type of event to trigger
     * @param forForm   true if the link is for a form, false otherwise
     * @param context   Additional path data, each value will be converted to a string and appended to the URI @return a
     *                  link
     * @see org.apache.tapestry5.ComponentResources#createActionLink(String, boolean, Object[])
     */
    Link createComponentEventLink(Page page, String nestedId, String eventType, boolean forForm, Object... context);

    /**
     * Creates a render link for the page. If an activation context is supplied then that context is built into the URI.
     * If no activation context is supplied, then the activation context is obtained from the page itself, by triggering
     * a passivate event on its root component.
     * <p/>
     * When the activationContext is an empty array, the targetted page is checked to see if it can provide an
     * activation context. This is accomplished by triggering a "passivate" event on the targetted page. If the override
     * parameter is true, this will not occur (even when the activation context is empty).
     *
     * @param page              the page to which a link should be created
     * @param override          if true, then the provided activation context is always used even if empty
     * @param activationContext the activation context for the page
     * @return a link
     * @see org.apache.tapestry5.ComponentResources#createPageLink(String, boolean, Object[])
     */
    Link createPageRenderLink(Page page, boolean override, Object... activationContext);

    /**
     * As with {@link #createPageRenderLink(Page, boolean, Object[])}, but the page is specified by logical name, rather
     * than as an instance.
     *
     * @param logicalPageName the logical name of the page to generate a link to
     * @param override        if true, then the provided activation context is always used even if empty
     * @param context         activation context for the page
     * @return
     */
    Link createPageRenderLink(String logicalPageName, boolean override, Object... context);

    /**
     * Adds a listener, to be notified any time an action or render link is created; this allows the listener to modify
     * the link (by adding additional query parameters to the link).
     *
     * @param listener to add
     */
    void addListener(LinkFactoryListener listener);
}
