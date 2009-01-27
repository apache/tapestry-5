// Copyright 2009 Apache Software Foundation
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

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

import java.io.IOException;

/**
 * A facade around {@link org.apache.tapestry5.services.ComponentEventRequestHandler} and {@link
 * org.apache.tapestry5.services.PageRenderRequestHandler} that allows for simplified filters that cover both types of
 * requests.
 *
 * @since 5.1.0.0
 */
@UsesOrderedConfiguration(ComponentRequestFilter.class)
public interface ComponentRequestHandler
{
    /**
     * Handler for a component action request which will trigger an event on a component and use the return value to
     * send a response to the client (typically, a redirect to a page render URL).
     *
     * @param parameters defining the requst
     */
    void handleComponentEvent(ComponentEventRequestParameters parameters) throws IOException;

    /**
     * Invoked to activate and render a page. In certain cases, based on values returned when activating the page, a
     * {@link org.apache.tapestry5.services.ComponentEventResultProcessor} may be used to send an alternate response
     * (typically, a redirect).
     *
     * @param parameters defines the page name and activation context
     */
    void handlePageRender(PageRenderRequestParameters parameters) throws IOException;
}
