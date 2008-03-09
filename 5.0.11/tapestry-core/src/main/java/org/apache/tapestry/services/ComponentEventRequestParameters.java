// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry.EventContext;
import org.apache.tapestry.ioc.internal.util.Defense;

/**
 * Encapsulates all the information that may be provided in a component event request URL.
 */
public final class ComponentEventRequestParameters
{
    private final String _activePageName;
    private final String _containingPageName;
    private final String _nestedComponentId;
    private final String _eventType;
    private final EventContext _pageActivationContext;
    private final EventContext _eventContext;

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

        _activePageName = activePageName;
        _containingPageName = containingPageName;
        _nestedComponentId = nestedComponentId;
        _eventType = eventType;
        _pageActivationContext = pageActivationContext;
        _eventContext = eventContext;
    }

    // Implements equals() as a convienience for testing.

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComponentEventRequestParameters that = (ComponentEventRequestParameters) o;

        if (!_activePageName.equals(that._activePageName)) return false;
        if (!_containingPageName.equals(that._containingPageName)) return false;
        if (!_eventType.equals(that._eventType)) return false;
        if (!_nestedComponentId.equals(that._nestedComponentId)) return false;

        if (!isEqual(_eventContext, that._eventContext)) return false;

        return isEqual(_pageActivationContext, that._pageActivationContext);
    }

    private boolean isEqual(EventContext left, EventContext right)
    {
        if (left == right) return true;

        int count = left.getCount();

        if (count != right.getCount()) return false;

        for (int i = 0; i < count; i++)
        {
            if (!left.get(Object.class, i).equals(right.get(Object.class, i)))
                return false;
        }

        return true;
    }


    /**
     * The name of the active page which rendered the link.  This is usually, but not always, the page which contains
     * the component.
     */
    public String getActivePageName()
    {
        return _activePageName;
    }

    /**
     * The name of the page containing the component that was triggered. Usually this is the same as the active page,
     * but because of {@link org.apache.tapestry.Block} and similar constructs, a component from other than the active
     * page may be rendered with the active page.
     */
    public String getContainingPageName()
    {
        return _containingPageName;
    }

    /**
     * The path from the containing page down to the component in question. This may be the empty string if the action
     * request is routed directly to the page rather than a component.
     */
    public String getNestedComponentId()
    {
        return _nestedComponentId;
    }

    /**
     * The type of event.  When not specified in the URL, a default type of "action" ({@link
     * org.apache.tapestry.TapestryConstants#ACTION_EVENT}) is provided.
     */
    public String getEventType()
    {
        return _eventType;
    }

    /**
     * The activation context for the <em>active page</em>, possibly empty (but not null).
     *
     * @see org.apache.tapestry.ComponentResourcesCommon#triggerContextEvent(String, org.apache.tapestry.EventContext,
     *      org.apache.tapestry.ComponentEventCallback)
     */
    public EventContext getPageActivationContext()
    {
        return _pageActivationContext;
    }

    /**
     * The event context information passed in the URL.  Possibly empty (not not null).
     *
     * @see org.apache.tapestry.ComponentResourcesCommon#triggerContextEvent(String, org.apache.tapestry.EventContext,
     *      org.apache.tapestry.ComponentEventCallback)
     */
    public EventContext getEventContext()
    {
        return _eventContext;
    }
}
