// Copyright 2007 The Apache Software Foundation
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

import java.io.IOException;

/**
 * Filter interface for {@link ComponentActionRequestHandler}.
 */
public interface ComponentActionRequestFilter
{
    /**
     * Filter for a component action request.
     *
     * @param logicalPageName   the page name containing the component, and the default component to render the
     *                          response
     * @param nestedComponentId the id of the component within the page
     * @param eventType         the type of event to trigger on the component
     * @param context           context information to provide to the event handler
     * @param activationContext activation context for the page
     * @param handler           to delegate to
     * @return true if the request has been handled (and a response sent to the client), false otherwise
     */
    boolean handle(String logicalPageName, String nestedComponentId, String eventType, String[] context,
                   String[] activationContext, ComponentActionRequestHandler handler) throws IOException;
}
