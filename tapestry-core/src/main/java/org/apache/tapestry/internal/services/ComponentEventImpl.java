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

import org.apache.tapestry.ComponentEventHandler;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.runtime.ComponentEvent;

public class ComponentEventImpl extends EventImpl implements ComponentEvent
{
    private final String _eventType;

    private final String _originatingComponentId;

    private final Object[] _context;

    private final TypeCoercer _typeCoercer;

    private final ClassLoader _classLoader;

    /**
     * @param eventType              non blank string used to identify the type of event that was triggered
     * @param originatingComponentId the id of the component that triggered the event (this will likely need to change
     *                               somewhat)
     * @param context                an array of values that can be made available to handler methods via method
     *                               parameters
     * @param handler                invoked when a non-null return value is obtained from an event handler method
     * @param typeCoercer            used when coercing context values to parameter types
     * @param classLoader            loader used when resolving a class name to a class  (ultimately, this
     *                               is the class loader used to create the component class; that loader's parent is the Thread's context class loader).
     */
    public ComponentEventImpl(String eventType, String originatingComponentId, Object[] context,
                              ComponentEventHandler handler, TypeCoercer typeCoercer, ClassLoader classLoader)
    {
        super(handler);

        _eventType = notBlank(eventType, "eventType");
        _originatingComponentId = originatingComponentId;
        _context = context != null ? context : new Object[0];
        _typeCoercer = notNull(typeCoercer, "typeCoercer");
        _classLoader = classLoader;
    }

    public boolean matches(String eventType, String componentId, int parameterCount)
    {
        return _eventType.equalsIgnoreCase(
                eventType) && _context.length >= parameterCount && (_originatingComponentId.equalsIgnoreCase(
                componentId) || componentId.equals(""));
    }

    @SuppressWarnings("unchecked")
    public Object coerceContext(int index, String desiredTypeName)
    {
        if (index >= _context.length) throw new IllegalArgumentException(ServicesMessages
                .contextIndexOutOfRange(getMethodDescription()));
        try
        {
            Class desiredType = Class.forName(desiredTypeName, true, _classLoader);

            return _typeCoercer.coerce(_context[index], desiredType);
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException(
                    ServicesMessages.exceptionInMethodParameter(getMethodDescription(), index, ex), ex);
        }
    }

    public Object[] getContext()
    {
        return _context;
    }
}
