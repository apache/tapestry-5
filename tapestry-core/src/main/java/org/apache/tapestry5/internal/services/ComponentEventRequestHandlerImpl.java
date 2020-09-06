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

import org.apache.tapestry5.TrackableComponentEventCallback;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.ComponentEventRequestHandler;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.Traditional;

import java.io.IOException;

@SuppressWarnings("unchecked")
public class ComponentEventRequestHandlerImpl implements ComponentEventRequestHandler
{
    private final ComponentEventResultProcessor resultProcessor;

    private final RequestPageCache cache;

    private final Response response;

    private final PageActivator pageActivator;

    private final Environment environment;

    public ComponentEventRequestHandlerImpl(@Traditional
                                            @Primary
                                            ComponentEventResultProcessor resultProcessor,

                                            RequestPageCache cache, Response response,

                                            PageActivator pageActivator,

                                            Environment environment)
    {
        this.resultProcessor = resultProcessor;
        this.cache = cache;
        this.response = response;
        this.pageActivator = pageActivator;
        this.environment = environment;
    }

    public void handle(ComponentEventRequestParameters parameters) throws IOException
    {
        Page activePage = cache.get(parameters.getActivePageName());

        if (pageActivator.activatePage(activePage.getRootElement().getComponentResources(), parameters
                .getPageActivationContext(), resultProcessor))
        {
            return;
        }

        Page containerPage = cache.get(parameters.getContainingPageName());

        TrackableComponentEventCallback callback = new ComponentResultProcessorWrapper(resultProcessor);

        environment.push(ComponentEventResultProcessor.class, resultProcessor);
        environment.push(TrackableComponentEventCallback.class, callback);

        ComponentPageElement element = containerPage.getComponentElementByNestedId(parameters.getNestedComponentId());

        boolean handled = element.triggerContextEvent(parameters.getEventType(), parameters.getEventContext(), callback);

        if (!handled)
        {
            throw new TapestryException(String.format("Request event '%s' (on component %s) was not handled; you must provide a matching event handler method in the component or in one of its containers.", parameters.getEventType(), element.getCompleteId()), element,
                    null);
        }

        environment.pop(TrackableComponentEventCallback.class);
        environment.pop(ComponentEventResultProcessor.class);

        if (callback.isAborted())
        {
            callback.rethrow();
            return;
        }

        // If we get this far without generating a response, the default behavior is to
        // generate a redirect back to the active page; we can let the ComponentEventResultProcessor handle that.

        if (!response.isCommitted())
        {
            resultProcessor.processResultValue(activePage.getName());
        }
    }
}
