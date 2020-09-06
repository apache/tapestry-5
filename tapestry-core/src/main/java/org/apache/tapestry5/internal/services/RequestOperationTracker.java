// Copyright 2013 Apache Software Foundation
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

import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.PageRenderRequestParameters;

import java.io.IOException;

/**
 * Uses {@link OperationTracker} to add an operation generally describing each request.
 *
 * @since 5.4
 */
public class RequestOperationTracker implements ComponentRequestFilter
{
    private final OperationTracker tracker;

    private final Request request;

    public RequestOperationTracker(OperationTracker tracker, Request request)
    {
        this.tracker = tracker;
        this.request = request;
    }

    public void handleComponentEvent(final ComponentEventRequestParameters parameters, final ComponentRequestHandler handler) throws IOException
    {
        String componentId = parameters.getNestedComponentId().equals("")
                ? parameters.getContainingPageName()
                : parameters.getContainingPageName() + ":" + parameters.getNestedComponentId();

        tracker.perform(String.format("Handling %s '%s' component event request for %s.",
                request.isXHR() ? "Ajax" : "traditional",
                parameters.getEventType(),
                componentId),
                new IOOperation<Void>()
                {
                    public Void perform() throws IOException
                    {
                        handler.handleComponentEvent(parameters);

                        return null;
                    }
                });
    }

    public void handlePageRender(final PageRenderRequestParameters parameters, final ComponentRequestHandler handler) throws IOException
    {
        final Holder<IOException> holder = Holder.create();

        tracker.run("Handling page render request for page " + parameters.getLogicalPageName(),
                new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            handler.handlePageRender(parameters);
                        } catch (IOException e)
                        {
                            holder.put(e);
                        }
                    }
                }
        );

        if (holder.hasValue())
        {
            throw holder.get();
        }
    }
}
