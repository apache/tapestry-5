// Copyright 2006 The Apache Software Foundation
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

import org.apache.tapestry.services.ActionResponseGenerator;

/**
 * Handler interface for action links. Action links <em>do things</em> such as process a form
 * submission or otherwise change state. In the majority of cases, after the action, a redirect
 * response is sent to the client which, in turn, causes a page render.
 */
public interface ActionLinkHandler
{
    /** Handle the event, and return a generator that is used to create a response for the client. */
    ActionResponseGenerator handle(ComponentInvocation invocation);

    /**
     * A convienience for handling a typical request, which returns a response generator used to
     * send the final response to the client.
     */
    ActionResponseGenerator handle(String logicalPageName, String nestedComponentId,
            String eventType, String[] context);
}
