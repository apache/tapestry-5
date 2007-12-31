// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.util.ContentType;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.*;

import java.io.IOException;

/**
 * Similar to {@link ComponentActionRequestHandlerImpl}, but built around the Ajax request cycle, where the action request
 * sends back an immediate JSON response containing the new content.
 */
public class AjaxComponentActionRequestHandler implements ComponentActionRequestHandler
{
    private final RequestPageCache _cache;

    private final MarkupWriterFactory _factory;

    private final AjaxPartialResponseRenderer _renderer;

    private final Request _request;

    private final Response _response;

    private final PageRenderQueue _queue;

    private final ComponentEventResultProcessor _resultProcessor;

    private final PageContentTypeAnalyzer _pageContentTypeAnalyzer;

    public AjaxComponentActionRequestHandler(RequestPageCache cache, MarkupWriterFactory factory,
                                             AjaxPartialResponseRenderer renderer, Request request, Response response,
                                             PageRenderQueue queue, @Ajax ComponentEventResultProcessor resultProcessor,
                                             PageContentTypeAnalyzer pageContentTypeAnalyzer)
    {
        _cache = cache;
        _factory = factory;
        _renderer = renderer;
        _response = response;
        _queue = queue;
        _resultProcessor = resultProcessor;
        _pageContentTypeAnalyzer = pageContentTypeAnalyzer;
        _request = request;
    }

    public void handle(String logicalPageName, String nestedComponentId, String eventType, String[] context,
                       String[] activationContext) throws IOException
    {
        Page page = _cache.get(logicalPageName);

        // If we end up doing a partial render, the page render queue service needs to know the
        // page that will be rendered (for logging purposes, if nothing else).

        _queue.initializeForCompletePage(page);

        ContentType contentType = _pageContentTypeAnalyzer.findContentType(page);

        _request.setAttribute(InternalConstants.CONTENT_TYPE_ATTRIBUTE_NAME, contentType);

        ComponentPageElement element = page.getComponentElementByNestedId(nestedComponentId);

        final Holder<Boolean> holder = Holder.create();
        final Holder<IOException> exceptionHolder = Holder.create();

        ComponentEventHandler handler = new ComponentEventHandler()
        {
            @SuppressWarnings("unchecked")
            public boolean handleResult(Object result, Component component, String methodDescription)
            {
                try
                {
                    _resultProcessor.processComponentEvent(result, component, methodDescription);
                }
                catch (IOException ex)
                {
                    // Jump through some hoops to escape this block, which doesn't
                    // declare IOException
                    exceptionHolder.put(ex);
                }

                holder.put(true);

                return true;
            }
        };

        page.getRootElement().triggerEvent(TapestryConstants.ACTIVATE_EVENT, activationContext, handler);

        if (exceptionHolder.hasValue()) throw exceptionHolder.get();

        if (holder.hasValue()) return;

        element.triggerEvent(eventType, context, handler);

        if (exceptionHolder.hasValue()) throw exceptionHolder.get();

        if (holder.hasValue()) return;

        JSONObject reply = new JSONObject();

        _resultProcessor.processComponentEvent(reply, null, null);

    }
}
