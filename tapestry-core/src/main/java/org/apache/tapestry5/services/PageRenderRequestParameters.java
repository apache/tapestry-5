// Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.internal.util.Defense;

/**
 * Used with {@link org.apache.tapestry5.services.PageRenderRequestHandler} and {@link
 * org.apache.tapestry5.services.PageRenderRequestFilter} to define the logical page name and activation context for the
 * request.
 */
public class PageRenderRequestParameters
{
    private final String logicalPageName;

    private final EventContext activationContext;

    public PageRenderRequestParameters(String logicalPageName, EventContext activationContext)
    {
        Defense.notNull(logicalPageName, "logicalPageName");
        Defense.notNull(activationContext, "activationContext");

        this.logicalPageName = logicalPageName;
        this.activationContext = activationContext;
    }

    public String getLogicalPageName()
    {
        return logicalPageName;
    }

    public EventContext getActivationContext()
    {
        return activationContext;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        PageRenderRequestParameters other = (PageRenderRequestParameters) obj;

        return logicalPageName.equals(other.logicalPageName) &&
                TapestryInternalUtils.isEqual(activationContext, other.activationContext);
    }


    @Override
    public String toString()
    {
        return String.format("PageRenderRequestParameters[%s]", logicalPageName);
    }
}
