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

import org.apache.tapestry.ioc.internal.util.Defense;

import java.util.Arrays;

/**
 * Encapsulates all the information that may be provided in a component action request URL.
 */
public final class ComponentActionRequestParameters
{
    private final String _activePageName;
    private final String _containingPageName;
    private final String _nestedComponentId;
    private final String _eventType;
    private final String[] _pageActivationContext;
    private final String[] _eventContext;

    public ComponentActionRequestParameters(String activePageName, String containingPageName, String nestedComponentId,
                                            String eventType, String[] pageActivationContext, String[] eventContext)
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

        ComponentActionRequestParameters that = (ComponentActionRequestParameters) o;

        if (!_activePageName.equals(that._activePageName)) return false;
        if (!_containingPageName.equals(that._containingPageName)) return false;
        if (!Arrays.equals(_eventContext, that._eventContext)) return false;
        if (!_eventType.equals(that._eventType)) return false;
        if (!_nestedComponentId.equals(that._nestedComponentId)) return false;

        return Arrays.equals(_pageActivationContext, that._pageActivationContext);
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
     */
    public String[] getPageActivationContext()
    {
        return _pageActivationContext;
    }

    /**
     * The event context information passed in the URL.  Possibly empty (not not null).
     *
     * @see org.apache.tapestry.ComponentResourcesCommon#triggerEvent(String, Object[],
     *      org.apache.tapestry.ComponentEventCallback)
     */
    public String[] getEventContext()
    {
        return _eventContext;
    }
}
