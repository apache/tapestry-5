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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * It represents an invocation target for any kind of component event.
 */
public class ComponentEventTarget implements InvocationTarget
{
    private final String eventType;

    private final String pageName;

    private final String componentNestedId;

    public ComponentEventTarget(String eventType, String pageName, String componentNestedId)
    {
        this.eventType = eventType;
        this.pageName = pageName;
        this.componentNestedId = componentNestedId;
    }

    public String getPath()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(pageName.toLowerCase());

        boolean hasComponentId = InternalUtils.isNonBlank(componentNestedId);

        if (hasComponentId)
        {

            builder.append(".");
            // Already lower case by design.
            builder.append(componentNestedId);
        }

        // If no nested component id, then must append the action; the ':' and the action become the
        // delimiter between the page name and the event context.

        if (!hasComponentId || !eventType.equals(EventConstants.ACTION))
        {
            builder.append(":");
            builder.append(eventType);
        }

        return builder.toString();
    }

    public String getEventType()
    {
        return eventType;
    }

    public String getComponentNestedId()
    {
        return componentNestedId;
    }

    public String getPageName()
    {
        return pageName;
    }
}
