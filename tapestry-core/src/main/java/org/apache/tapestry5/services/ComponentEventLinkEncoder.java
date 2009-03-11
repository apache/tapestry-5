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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Link;

/**
 * Responsible for creating {@link org.apache.tapestry5.Link}s for page render requests and for component event
 * requests, and for parsing incoming paths to identify requests that are component event or page render requests. This
 * centralizes some logic that was scattered about in Tapestry 5.0.
 *
 * @since 5.1.0.1
 */
public interface ComponentEventLinkEncoder
{
    /**
     * Creates a Link that encapsulates a page render request, including activation context <em>and {@link
     * org.apache.tapestry5.services.PersistentLocale} (if set)</em>.
     *
     * @param parameters defining page to render and context
     * @return link for the page render
     */
    Link createPageRenderLink(PageRenderRequestParameters parameters);

    /**
     * Creates a link that encapsulates a component event request, including <em>{@link
     * org.apache.tapestry5.services.PersistentLocale} (if set)</em>.
     * <p/>
     * <p> Forms: <ul> <li>/context/pagename:eventname -- event on the page, no action context</li>
     * <li>/context/pagename:eventname/foo/bar -- event on the page with action context "foo", "bar"</li>
     * <li>/context/pagename.foo.bar -- event on component foo.bar within the page, default event, no action
     * context</li> <li>/context/pagename.foo.bar/baz.gnu -- event on component foo.bar within the page, default event,
     * with action context "baz", "gnu"</li> <li>/context/pagename.bar.baz:eventname/foo/gnu -- event on component
     * bar.baz within the page with action context "foo" , "gnu"</li> </ul>
     * <p/>
     * The persistent locale may be placed in between the context name and the page name, i.e., "/context/fr/SomePage".
     * <p/>
     * In many cases the context name is blank, so the path begins with a "/" and then the locale name or page name.
     * <p/>
     * The page name portion may itself consist of a series of folder names, i.e., "admin/user/create".  The context
     * portion isn't the concern of this code, since {@link org.apache.tapestry5.services.Request#getPath()} will
     * already have stripped that off.  We can act as if the context is always "/" (the path always starts with a
     * slash).
     * <p/>
     *
     * @param parameters defining page, component, activation context and other details
     * @param forForm    true if the event link will trigger a form submission
     * @return link for the component event
     */
    Link createComponentEventLink(ComponentEventRequestParameters parameters, boolean forForm);

    /**
     * Checks the request, primarily the {@linkplain org.apache.tapestry5.services.Request#getPath() path}, to determine
     * the if the request is a component event request. As a side-effect (necessary for historical reasons), responsible
     * for setting the locale for the thread, including the {@link org.apache.tapestry5.services.PersistentLocale} ...
     * but only if the reuqest is a component event.
     *
     * @param request incoming request
     * @return component event request details, if a component event request
     */
    ComponentEventRequestParameters decodeComponentEventRequest(Request request);

    /**
     * Checks the request, primarily the {@linkplain org.apache.tapestry5.services.Request#getPath() path}, to determine
     * the if the request is a page render request. As a side-effect (necessary for historical reasons), responsible for
     * setting the locale for the thread, including the {@link org.apache.tapestry5.services.PersistentLocale} ... but
     * only if the reuqest is a component event.
     *
     * @param request incoming request
     * @return page render request details, if a page render request
     */
    PageRenderRequestParameters decodePageRenderRequest(Request request);
}
