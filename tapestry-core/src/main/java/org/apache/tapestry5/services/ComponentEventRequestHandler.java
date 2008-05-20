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

package org.apache.tapestry.services;

import org.apache.tapestry.corelib.components.ActionLink;
import org.apache.tapestry.corelib.components.Form;

import java.io.IOException;

/**
 * Handler interface for action requests. Action requests <em>do things</em> such as process a form submission or
 * otherwise change state. In the majority of cases, after the action, a redirect response is sent to the client which,
 * in turn, causes a page render.
 *
 * @see ActionLink
 * @see Form
 * @see ComponentEventRequestFilter
 */
public interface ComponentEventRequestHandler
{
    /**
     * Handler for a component action request which will trigger an event on a component and use the return value to
     * send a response to the client (typically, a redirect to a page render URL).
     *
     * @param parameters defining the requst
     */
    void handle(ComponentEventRequestParameters parameters) throws IOException;
}
