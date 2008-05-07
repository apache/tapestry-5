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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ComponentEventCallback;
import org.apache.tapestry.EventContext;
import org.apache.tapestry.internal.structure.PageResources;
import org.apache.tapestry.runtime.ComponentEvent;

public class ComponentEventImpl extends EventImpl implements ComponentEvent
{
    private final String eventType;

    private final String originatingComponentId;

    private final EventContext context;

    private final PageResources pageResources;

    /**
     * @param eventType              non blank string used to identify the type of event that was triggered
     * @param originatingComponentId the id of the component that triggered the event (this will likely need to change
     *                               somewhat)
     * @param context                provides access to parameter values
     * @param handler                invoked when a non-null return value is obtained from an event handler method
     * @param pageResources          provides access to common resources and services
     */
    public ComponentEventImpl(String eventType, String originatingComponentId, EventContext context,
                              ComponentEventCallback handler, PageResources pageResources)
    {
        super(handler);

        this.eventType = eventType;
        this.originatingComponentId = originatingComponentId;
        this.pageResources = pageResources;
        this.context = context;
    }

    public boolean matches(String eventType, String componentId, int parameterCount)
    {
        return this.eventType.equalsIgnoreCase(
                eventType) && context.getCount() >= parameterCount && (originatingComponentId.equalsIgnoreCase(
                componentId) || componentId.equals(""));
    }

    @SuppressWarnings("unchecked")
    public Object coerceContext(int index, String desiredTypeName)
    {
        if (index >= context.getCount()) throw new IllegalArgumentException(ServicesMessages
                .contextIndexOutOfRange(getMethodDescription()));
        try
        {
            Class desiredType = pageResources.toClass(desiredTypeName);

            return context.get(desiredType, index);

        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException(
                    ServicesMessages.exceptionInMethodParameter(getMethodDescription(), index, ex), ex);
        }
    }

    public Object[] getContext()
    {
        int count = context.getCount();

        Object[] result = new Object[count];

        for (int i = 0; i < count; i++)
            result[i] = context.get(Object.class, i);

        return result;
    }

    public EventContext getEventContext()
    {
        return context;
    }
}
