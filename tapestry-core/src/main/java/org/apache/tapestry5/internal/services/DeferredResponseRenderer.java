// Copyright 2014 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;

import java.io.IOException;

/**
 * After processing the component event request (including Ajax requests), or the page render request,
 * checks for the {@link org.apache.tapestry5.TapestryConstants#RESPONSE_RENDERER} request attribute,
 * and invokes it to render the deferred response.
 *
 * @since 5.4
 */
public class DeferredResponseRenderer implements ComponentRequestFilter
{
    private final Request request;

    private final OperationTracker tracker;

    public DeferredResponseRenderer(Request request, OperationTracker tracker)
    {
        this.request = request;
        this.tracker = tracker;
    }

    public void handleComponentEvent(ComponentEventRequestParameters parameters, ComponentRequestHandler handler) throws IOException
    {
        handler.handleComponentEvent(parameters);

        invokeQueuedRenderer();
    }

    public void handlePageRender(PageRenderRequestParameters parameters, ComponentRequestHandler handler) throws IOException
    {
        handler.handlePageRender(parameters);

        invokeQueuedRenderer();
    }

    private void invokeQueuedRenderer() throws IOException
    {
        while (true)
        {

            IOOperation responseRenderer = (IOOperation) request.getAttribute(TapestryConstants.RESPONSE_RENDERER);

            if (responseRenderer == null)
            {
                break;
            }

            // There's a particular case where an operation puts a different operation into the attribute;
            // we'll handle that on the next pass.
            request.setAttribute(TapestryConstants.RESPONSE_RENDERER, null);

            tracker.perform("Executing deferred response renderer.", responseRenderer);
        }
    }
}
