// Copyright 2006, 2007 The Apache Software Foundation
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
import org.apache.tapestry.ioc.internal.util.InternalUtils;

/**
 * It represents an invocation target for an action link.
 */
public class ActionLinkTarget implements InvocationTarget
{
    private final String _eventType;

    private final String _pageName;

    private final String _componentNestedId;

    public ActionLinkTarget(String eventType, String pageName, String componentNestedId)
    {
        _eventType = eventType;
        _pageName = pageName;
        _componentNestedId = componentNestedId;

    }

    public String getPath()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(_pageName.toLowerCase());

        boolean hasComponentId = InternalUtils.isNonBlank(_componentNestedId);

        if (hasComponentId)
        {

            builder.append(".");
            // Already lower case by design.
            builder.append(_componentNestedId);
        }

        // If no nested component id, then must append the action; the ':' and the action become the
        // delimiter between the page name and the event context.

        if (!hasComponentId || !_eventType.equals(TapestryConstants.ACTION_EVENT))
        {
            builder.append(":");
            builder.append(_eventType);
        }

        return builder.toString();
    }

    public String getEventType()
    {
        return _eventType;
    }

    public String getComponentNestedId()
    {
        return _componentNestedId;
    }

    public String getPageName()
    {
        return _pageName;
    }

}
