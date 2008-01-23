// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.*;

import java.io.IOException;

public class ComponentEventRequestHandlerImpl implements ComponentEventRequestHandler
{
    private final ComponentEventResultProcessor _resultProcessor;

    private final RequestPageCache _cache;

    private final Response _response;

    private final ActionRenderResponseGenerator _generator;

    public ComponentEventRequestHandlerImpl(@Traditional ComponentEventResultProcessor resultProcessor,
                                            RequestPageCache cache, Response response,
                                            ActionRenderResponseGenerator generator)
    {
        _resultProcessor = resultProcessor;
        _cache = cache;
        _response = response;
        _generator = generator;
    }

    public void handle(ComponentEventRequestParameters parameters) throws IOException
    {
        Page activePage = _cache.get(parameters.getActivePageName());

        ComponentResultProcessorWrapper callback = new ComponentResultProcessorWrapper(_resultProcessor);

        // If activating the page returns a "navigational result", then don't trigger the action
        // on the component.

        activePage.getRootElement().triggerEvent(TapestryConstants.ACTIVATE_EVENT,
                                                 parameters.getPageActivationContext(), callback);

        if (callback.isAborted()) return;

        Page containerPage = _cache.get(parameters.getContainingPageName());

        ComponentPageElement element = containerPage.getComponentElementByNestedId(parameters.getNestedComponentId());

        element.triggerEvent(parameters.getEventType(), parameters.getEventContext(), callback);

        if (callback.isAborted()) return;

        if (!_response.isCommitted()) _generator.generateResponse(activePage);
    }
}
