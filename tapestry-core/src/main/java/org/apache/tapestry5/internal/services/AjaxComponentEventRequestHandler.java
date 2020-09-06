// Copyright 2007-2013 The Apache Software Foundation
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

import java.io.IOException;

import org.apache.tapestry5.TrackableComponentEventCallback;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Ajax;
import org.apache.tapestry5.services.ComponentEventRequestHandler;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Environment;

/**
 * Similar to {@link ComponentEventRequestHandlerImpl}, but built around the Ajax request cycle, where the action
 * request sends back an immediate JSON response containing the new content.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class AjaxComponentEventRequestHandler implements ComponentEventRequestHandler
{
    private final RequestPageCache cache;

    private final Request request;

    private final PageRenderQueue queue;

    private final ComponentEventResultProcessor resultProcessor;

    private final Environment environment;

    private final AjaxPartialResponseRenderer partialRenderer;

    private final PageActivator pageActivator;

    public AjaxComponentEventRequestHandler(RequestPageCache cache, Request request, PageRenderQueue queue, @Ajax
    ComponentEventResultProcessor resultProcessor, PageActivator pageActivator,
                                            Environment environment,
                                            AjaxPartialResponseRenderer partialRenderer)
    {
        this.cache = cache;
        this.queue = queue;
        this.resultProcessor = resultProcessor;
        this.pageActivator = pageActivator;
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

        // If we end up doing a partial render, the page render queue service needs to know the
        // page that will be rendered (for logging purposes, if nothing else).

        queue.setRenderingPage(activePage);

        request.setAttribute(InternalConstants.PAGE_NAME_ATTRIBUTE_NAME, parameters.getActivePageName());

        if (pageActivator.activatePage(activePage.getRootElement().getComponentResources(), parameters
                .getPageActivationContext(), interceptor))
            return;

        Page containerPage = cache.get(parameters.getContainingPageName());

        ComponentPageElement element = containerPage.getComponentElementByNestedId(parameters.getNestedComponentId());

        // In many cases, the triggered element is a Form that needs to be able to
        // pass its event handler return values to the correct result processor.
        // This is certainly the case for forms.

        TrackableComponentEventCallback callback = new ComponentResultProcessorWrapper(interceptor);

        environment.push(ComponentEventResultProcessor.class, interceptor);
        environment.push(TrackableComponentEventCallback.class, callback);

        boolean handled = element
                .triggerContextEvent(parameters.getEventType(), parameters.getEventContext(), callback);

        if (!handled)
            throw new TapestryException(String.format("Request event '%s' (on component %s) was not handled; you must provide a matching event handler method in the component or in one of its containers.", parameters.getEventType(), element.getCompleteId()), element,
                    null);

        environment.pop(TrackableComponentEventCallback.class);
        environment.pop(ComponentEventResultProcessor.class);


        // If the result processor was passed a value, then it will already have rendered. Otherwise it was not passed a value,
        // but it's still possible that we still want to do a partial page render ... if filters were added to the render queue.
        // In that event, run the partial page render now and return.

        boolean wasInvoked = resultProcessorInvoked.get();

        if ((!wasInvoked) && queue.isPartialRenderInitialized())
        {
            partialRenderer.renderPartialPageMarkup();
            return;
        }

        // If the result processor was passed a value, then it will already have rendered, and there is nothing more to do.

        if (wasInvoked) { return; }

        // Send an empty JSON reply if no value was returned from the component event handler method.
        // This is the typical behavior when an Ajax component event handler returns null. It still
        // will go through a pipeline that will add information related to partial page rendering.

        resultProcessor.processResultValue(new JSONObject());
    }
}
