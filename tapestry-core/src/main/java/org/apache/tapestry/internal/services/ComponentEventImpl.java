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

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.runtime.ComponentEvent;

public class ComponentEventImpl extends EventImpl implements ComponentEvent
{
    private final String _eventType;

    private final String _originatingComponentId;

    private final Object[] _context;

    private final TypeCoercer _typeCoercer;

    /**
     * @param eventType
     *            non blank string used to identify the type of event that was triggered
     * @param originatingComponentId
     *            the id of the component that triggered the event (this will likely need to change
     *            somewhat)
     * @param context
     *            an array of values that can be made available to handler methods via method
     *            parameters
     * @param handler
     *            invoked when a non-null return value is obtained from an event handler method
     * @param typeCoercer
     *            used when coercing context values to parameter types
     */
    public ComponentEventImpl(String eventType, String originatingComponentId, Object[] context,
            ComponentEventHandler handler, TypeCoercer typeCoercer)
    {
        super(handler);

        _eventType = notBlank(eventType, "eventType");
        _originatingComponentId = originatingComponentId;
        _context = context != null ? context : new Object[0];
        _typeCoercer = notNull(typeCoercer, "typeCoercer");

    }

    /**
     * TODO: This implementation is broken, but will get the job done for simple cases. It just
     * doesn't do quite the right think when an event bubbles up past its originating component's
     * container (can lead to false matches).
     */
    public boolean matchesByComponentId(ComponentResources resources, String[] componentId)
    {
        for (String id : componentId)
        {
            if (id.equalsIgnoreCase(_originatingComponentId))
                return true;
        }

        return false;
    }

    public boolean matchesByEventType(String[] eventTypes)
    {
        for (String type : eventTypes)
        {
            if (type.equalsIgnoreCase(_eventType))
                return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public Object coerceContext(int index, String desiredTypeName)
    {
        if (index >= _context.length)
            throw new IllegalArgumentException(ServicesMessages
                    .contextIndexOutOfRange(getMethodDescription()));

        try
        {
            Class desiredType = Class.forName(desiredTypeName);

            return _typeCoercer.coerce(_context[index], desiredType);
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException(ServicesMessages.exceptionInMethodParameter(
                    getMethodDescription(),
                    index,
                    ex), ex);
        }
    }

    public Object[] getContext()
    {
        return _context;
    }
}
