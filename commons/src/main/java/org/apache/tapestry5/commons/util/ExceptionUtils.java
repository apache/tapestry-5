// Copyright 2008-2013 The Apache Software Foundation
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

package org.apache.tapestry5.commons.util;

import org.apache.tapestry5.commons.services.ClassPropertyAdapter;
import org.apache.tapestry5.commons.services.PropertyAccess;

/**
 * Contains static methods useful for manipulating exceptions.
 */
public class ExceptionUtils
{
    /**
     * Locates a particular type of exception, working its way via the cause property of each exception in the exception
     * stack.
     *
     * @param t    the outermost exception
     * @param type the type of exception to search for
     * @return the first exception of the given type, if found, or null
     */
    public static <T extends Throwable> T findCause(Throwable t, Class<T> type)
    {
        Throwable current = t;

        while (current != null)
        {
            if (type.isInstance(current))
            {
                return type.cast(current);
            }

            // Not a match, work down.

            current = current.getCause();
        }

        return null;
    }

    /**
     * Locates a particular type of exception, working its way down via any property that returns some type of Exception.
     * This is more expensive, but more accurate, than {@link #findCause(Throwable, Class)} as it works with older exceptions
     * that do not properly implement the (relatively new) {@linkplain Throwable#getCause() cause property}.
     *
     * @param t      the outermost exception
     * @param type   the type of exception to search for
     * @param access used to access properties
     * @return the first exception of the given type, if found, or null
     */
    public static <T extends Throwable> T findCause(Throwable t, Class<T> type, PropertyAccess access)
    {
        Throwable current = t;

        while (current != null)
        {
            if (type.isInstance(current))
            {
                return type.cast(current);
            }

            Throwable next = null;

            ClassPropertyAdapter adapter = access.getAdapter(current);

            for (String name : adapter.getPropertyNames())
            {

                Object value = adapter.getPropertyAdapter(name).get(current);

                if (value != null && value != current && value instanceof Throwable)
                {
                    next = (Throwable) value;
                    break;
                }
            }

            current = next;
        }


        return null;
    }

    /**
     * Extracts the message from an exception. If the exception's message is null, returns the exceptions class name.
     *
     * @param exception
     *         to extract message from
     * @return message or class name
     * @since 5.4
     */
    public static String toMessage(Throwable exception)
    {
        assert exception != null;

        String message = exception.getMessage();

        if (message != null)
            return message;

        return exception.getClass().getName();
    }
}
