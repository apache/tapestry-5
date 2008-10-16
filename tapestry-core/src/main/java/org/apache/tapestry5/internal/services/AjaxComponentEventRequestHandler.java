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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.*;

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

        final Holder<Boolean> resultProcessorInvoked = Holder.create();
        resultProcessorInvoked.put(false);

        ComponentEventResultProcessor interceptor = new ComponentEventResultProcessor()
        {
            public void processResultValue(Object value) throws IOException
            {
                resultProcessorInvoked.put(true);

                resultProcessor.processResultValue(value);
            }
        };

        ComponentResultProcessorWrapper callback = new ComponentResultProcessorWrapper(interceptor);

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
        // This is certainly the case for forms.

        environment.push(ComponentEventResultProcessor.class, interceptor);

        boolean handled = element.triggerContextEvent(parameters.getEventType(), parameters.getEventContext(),
                                                      callback);

        if (!handled)
            throw new TapestryException(ServicesMessages.eventNotHandled(element, parameters.getEventType()), element,
                                        null);

        environment.pop(ComponentEventResultProcessor.class);

        if (queue.isPartialRenderInitialized())
        {
            partialRenderer.renderPartialPageMarkup();
            return;
        }

        // If  some other form of return value that's not a partial page render was send through to the
        // Ajax ComponentEventResultProcessor, then there's nothing more to do.

        if (resultProcessorInvoked.get()) return;

        // Send an empty JSON reply if no value was returned from the component event handler method.

        JSONObject reply = new JSONObject();

        resultProcessor.processResultValue(reply);
    }
}
