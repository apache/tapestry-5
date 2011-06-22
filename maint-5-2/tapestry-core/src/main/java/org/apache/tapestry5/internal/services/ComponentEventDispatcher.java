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

import org.apache.tapestry5.services.*;

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

        componentRequestHandler.handleComponentEvent(parameters);

        return true;
    }
}
