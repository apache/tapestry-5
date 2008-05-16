// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.ContentType;
import org.apache.tapestry.EventConstants;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.services.*;

import java.io.IOException;

/**
 * Similar to {@link ComponentEventRequestHandlerImpl}, but built around the Ajax request cycle, where the action
 * request sends back an immediate JSON response containing the new content.
 */
public class AjaxComponentEventRequestHandler implements ComponentEventRequestHandler
{
    private final RequestPageCache cache;

    private final Request request;

    private final PageRenderQueue queue;

    private final ComponentEventResultProcessor resultProcessor;

    private final PageContentTypeAnalyzer pageContentTypeAnalyzer;

    private final Environment environment;

    private final AjaxPartialResponseRenderer partialRenderer;

    public AjaxComponentEventRequestHandler(RequestPageCache cache, Request request, PageRenderQueue queue,
                                            @Ajax ComponentEventResultProcessor resultProcessor,
                                            PageContentTypeAnalyzer pageContentTypeAnalyzer, Environment environment,
                                            AjaxPartialResponseRenderer partialRenderer)
    {
        this.cache = cache;
        this.queue = queue;
        this.resultProcessor = resultProcessor;
        this.pageContentTypeAnalyzer = pageContentTypeAnalyzer;
        this.request = request;
        this.environment = environment;
        this.partialRenderer = partialRenderer;
    }

    public void handle(ComponentEventRequestParameters parameters) throws IOException
    {
        Page activePage = cache.get(parameters.getActivePageName());

        ComponentResultProcessorWrapper callback = new ComponentResultProcessorWrapper(resultProcessor);


        activePage.getRootElement().triggerContextEvent(EventConstants.ACTIVATE,
                                                        parameters.getPageActivationContext(), callback);


        if (callback.isAborted()) return;

        // If we end up doing a partial render, the page render queue service needs to know the
        // page that will be rendered (for logging purposes, if nothing else).

        queue.setRenderingPage(activePage);

        ContentType contentType = pageContentTypeAnalyzer.findContentType(activePage);

        request.setAttribute(InternalConstants.CONTENT_TYPE_ATTRIBUTE_NAME, contentType);

        Page containerPage = cache.get(parameters.getContainingPageName());

        ComponentPageElement element = containerPage.getComponentElementByNestedId(parameters.getNestedComponentId());

        // In many cases, the triggered element is a Form that needs to be able to
        // pass its event handler return values to the correct result processor.

        environment.push(ComponentEventResultProcessor.class, resultProcessor);

        element.triggerContextEvent(parameters.getEventType(), parameters.getEventContext(), callback);

        environment.pop(ComponentEventResultProcessor.class);

        if (queue.isPartialRenderInitialized())
        {
            partialRenderer.renderPartialPageMarkup();
            return;
        }

        if (callback.isAborted()) return;

        // Send an empty JSON reply if no value was returned from the component event handler method.

        JSONObject reply = new JSONObject();

        resultProcessor.processResultValue(reply);

    }
}
