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
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ComponentRequestHandler;

import java.io.IOException;

/**
 * Processes component action events sent as requests from the client. Component events include an event type, identify
 * a page and a component, and may provide additional context strings.
 *
 * @see org.apache.tapestry5.services.ComponentEventLinkEncoder
 */
public class ComponentEventDispatcher implements Dispatcher
{
    private final ComponentRequestHandler componentRequestHandler;

    private final ComponentEventLinkEncoder linkEncoder;

    public ComponentEventDispatcher(ComponentRequestHandler componentRequestHandler,
                                    ComponentEventLinkEncoder linkEncoder)
    {
        this.componentRequestHandler = componentRequestHandler;
        this.linkEncoder = linkEncoder;
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        ComponentEventRequestParameters parameters = linkEncoder.decodeComponentEventRequest(request);

        if (parameters == null) return false;

        // Inside this pipeline, may find that the component id does not exist (this check only occurs in production
        // mode) ...

        componentRequestHandler.handleComponentEvent(parameters);

        // ... in which case, this attribute is set.
        if (request.getAttribute(InternalConstants.REFERENCED_COMPONENT_NOT_FOUND) != null) {
            return false;
        }

        return true;
    }
}
