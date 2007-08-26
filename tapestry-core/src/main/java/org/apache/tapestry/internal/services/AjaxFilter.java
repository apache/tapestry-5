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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.ComponentActionRequestFilter;
import org.apache.tapestry.services.ComponentActionRequestHandler;

/**
 * A filter that intercepts Ajax-oriented requests, thos that originate on the client-side using
 * XmlHttpRequest. In these cases, the action processing occurs normally, but the response is quite
 * different.
 */
public class AjaxFilter implements ComponentActionRequestFilter
{
    public ActionResponseGenerator handle(String logicalPageName, String nestedComponentId,
            String eventType, String[] context, String[] activationContext,
            ComponentActionRequestHandler handler)
    {
        return handler.handle(
                logicalPageName,
                nestedComponentId,
                eventType,
                context,
                activationContext);
    }

}
