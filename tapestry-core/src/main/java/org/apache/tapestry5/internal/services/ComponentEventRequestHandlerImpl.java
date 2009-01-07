// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.services.*;

import java.io.IOException;

public class ComponentEventRequestHandlerImpl implements ComponentEventRequestHandler
{
    private final ComponentEventResultProcessor resultProcessor;

    private final RequestPageCache cache;

    private final Response response;

    private final ActionRenderResponseGenerator generator;

    private final Environment environment;

    public ComponentEventRequestHandlerImpl(
            @Traditional @Primary
            ComponentEventResultProcessor resultProcessor,

            RequestPageCache cache, Response response,

            ActionRenderResponseGenerator generator,

            Environment environment)
    {
        this.resultProcessor = resultProcessor;
        this.cache = cache;
        this.response = response;
        this.generator = generator;
        this.environment = environment;
    }

    public void handle(ComponentEventRequestParameters parameters) throws IOException
    {
        Page activePage = cache.get(parameters.getActivePageName());

        ComponentResultProcessorWrapper callback = new ComponentResultProcessorWrapper(resultProcessor);

        // If activating the page returns a "navigational result", then don't trigger the action
        // on the component.

        activePage.getRootElement().triggerContextEvent(EventConstants.ACTIVATE,
                                                        parameters.getPageActivationContext(), callback);

        if (callback.isAborted()) return;

        Page containerPage = cache.get(parameters.getContainingPageName());

        ComponentPageElement element = containerPage.getComponentElementByNestedId(parameters.getNestedComponentId());

        environment.push(ComponentEventResultProcessor.class, resultProcessor);

        boolean handled = element.triggerContextEvent(parameters.getEventType(), parameters.getEventContext(),
                                                      callback);

        if (!handled)
            throw new TapestryException(ServicesMessages.eventNotHandled(element, parameters.getEventType()), element,
                                        null);

        environment.pop(ComponentEventResultProcessor.class);

        if (callback.isAborted()) return;

        if (!response.isCommitted()) generator.generateResponse(activePage);
    }
}
