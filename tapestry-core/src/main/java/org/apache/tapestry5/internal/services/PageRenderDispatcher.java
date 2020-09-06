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

import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.services.ComponentEventLinkEncoder;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;

import java.io.IOException;

/**
 * Dispatches incoming requests for render requests. Render requests consist of either just a logical page name (case
 * insensitive) or a logical page name plus additional context. Because of this structure, it take a little bit of work
 * to identify the split point between the page name and the context.
 */
public class PageRenderDispatcher implements Dispatcher
{
    private final ComponentRequestHandler componentRequestHandler;

    private final ComponentEventLinkEncoder linkEncoder;

    public PageRenderDispatcher(ComponentRequestHandler componentRequestHandler, ComponentEventLinkEncoder linkEncoder)
    {
        this.componentRequestHandler = componentRequestHandler;
        this.linkEncoder = linkEncoder;
    }

    public boolean dispatch(Request request, final Response response) throws IOException
    {
        // If a component event request arrives (in production)
        // with an invalid component id, then we want it to be a 404
        // See TAP5-1481 and TAP5-2388

        if (request.getAttribute(InternalConstants.REFERENCED_COMPONENT_NOT_FOUND) != null)
        {
            // This needs to be cleared out because the container may submit a request back into the filter
            // for the 404 page and some containers reuse the existing HttpServletRequest. See TAP5-2388.
            request.setAttribute(InternalConstants.REFERENCED_COMPONENT_NOT_FOUND, null);
            return false;
        }

        PageRenderRequestParameters parameters = linkEncoder.decodePageRenderRequest(request);

        if (parameters == null) return false;

        componentRequestHandler.handlePageRender(parameters);

        return true;
    }
}
