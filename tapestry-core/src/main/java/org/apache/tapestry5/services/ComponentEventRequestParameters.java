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
 * Encapsulates all the information that may be provided in a component event request URL.
 */
public final class ComponentEventRequestParameters
{
    private final String activePageName, containingPageName, nestedComponentId, eventType;
    private final EventContext pageActivationContext, eventContext;

    public ComponentEventRequestParameters(String activePageName, String containingPageName, String nestedComponentId,
                                           String eventType, EventContext pageActivationContext,
                                           EventContext eventContext)
    {
        Defense.notBlank(activePageName, "activePageName");
        Defense.notBlank(containingPageName, "containingPageName");
        Defense.notNull(nestedComponentId, "nestedComponentId");
        Defense.notBlank(eventType, "eventType");
        Defense.notNull(pageActivationContext, "pageActivationContext");
        Defense.notNull(eventContext, "eventContext");

        this.activePageName = activePageName;
        this.containingPageName = containingPageName;
        this.nestedComponentId = nestedComponentId;
        this.eventType = eventType;
        this.pageActivationContext = pageActivationContext;
        this.eventContext = eventContext;
    }

    @Override
    public String toString()
    {
        return String.format("ComponentEventParameters[page=%s component=%s:%s event=%s]",
                             activePageName,
                             containingPageName, nestedComponentId,
                             eventType);
    }

    // Implements equals() as a convienience for testing.

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentEventRequestParameters that = (ComponentEventRequestParameters) o;

        if (!activePageName.equals(that.activePageName)) return false;
        if (!containingPageName.equals(that.containingPageName)) return false;
        if (!eventType.equals(that.eventType)) return false;
        if (!nestedComponentId.equals(that.nestedComponentId)) return false;

        if (!TapestryInternalUtils.isEqual(eventContext, that.eventContext)) return false;

        return TapestryInternalUtils.isEqual(pageActivationContext, that.pageActivationContext);
    }


    /**
     * The name of the active page which rendered the link.  This is usually, but not always, the page which contains
     * the component.
     */
    public String getActivePageName()
    {
        return activePageName;
    }

    /**
     * The name of the page containing the component that was triggered. Usually this is the same as the active page,
     * but because of {@link org.apache.tapestry5.Block} and similar constructs, a component from other than the active
     * page may be rendered with the active page.
     */
    public String getContainingPageName()
    {
        return containingPageName;
    }

    /**
     * The path from the containing page down to the component in question. This may be the empty string if the action
     * request is routed directly to the page rather than a component.
     */
    public String getNestedComponentId()
    {
        return nestedComponentId;
    }

    /**
     * The type of event.  When not specified in the URL, a default type of "action" ({@link
     * org.apache.tapestry5.EventConstants#ACTION}) is provided.
     */
    public String getEventType()
    {
        return eventType;
    }

    /**
     * The activation context for the <em>active page</em>, possibly empty (but not null).
     *
     * @see org.apache.tapestry5.ComponentResourcesCommon#triggerContextEvent(String, org.apache.tapestry5.EventContext,
     *      org.apache.tapestry5.ComponentEventCallback)
     */
    public EventContext getPageActivationContext()
    {
        return pageActivationContext;
    }

    /**
     * The event context information passed in the URL.  Possibly empty (not not null).
     *
     * @see org.apache.tapestry5.ComponentResourcesCommon#triggerContextEvent(String, org.apache.tapestry5.EventContext,
     *      org.apache.tapestry5.ComponentEventCallback)
     */
    public EventContext getEventContext()
    {
        return eventContext;
    }
}
