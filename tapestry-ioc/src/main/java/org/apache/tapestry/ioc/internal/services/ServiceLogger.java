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

package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.services.ExceptionTracker;
import org.slf4j.Logger;

import static java.lang.String.format;
import java.util.Iterator;

/**
 * Used by {@link org.apache.tapestry.ioc.internal.services.LoggingDecoratorImpl} to delegate out
 * logging behavior to a seperate object (helps ensure no naming conflicts).
 */
public final class ServiceLogger
{
    private final Logger _logger;

    private final ExceptionTracker _exceptionTracker;

    private static final String ENTER = "ENTER";

    private static final String EXIT = " EXIT";

    private static final String FAIL = " FAIL";

    public ServiceLogger(Logger logger, ExceptionTracker exceptionTracker)
    {
        _logger = logger;
        _exceptionTracker = exceptionTracker;
    }

    /**
     * Returns true if the debugging is enabled for the underlying Log.
     */
    public boolean isDebugEnabled()
    {
        return _logger.isDebugEnabled();
    }

    /**
     * Invoked when a method is first entered
     *
     * @param name      of the method
     * @param arguments
     */
    public void entry(String name, Object[] arguments)
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append(format("[%s] %s(", ENTER, name));

        for (int i = 0; i < arguments.length; i++)
        {
            if (i > 0) buffer.append(", ");

            convert(buffer, arguments[i]);
        }

        buffer.append(")");

        _logger.debug(buffer.toString());
    }

    private void convert(StringBuilder buffer, Object object)
    {
        if (object == null)
        {
            buffer.append("null");
            return;
        }

        // Minimal, alas: Doesn't handle embedded quotes and other
        // characters. Really want to convert the string back to what it
        // would look like as source code.

        if (object instanceof String)
        {
            buffer.append("\"");
            buffer.append(object.toString());
            buffer.append("\"");
            return;
        }

        if (object instanceof Object[])
        {
            Object[] values = (Object[]) object;
            buffer.append('{');

            for (int i = 0; i < values.length; i++)
            {
                if (i > 0) buffer.append(", ");

                convert(buffer, values[i]);
            }

            buffer.append('}');
            return;
        }

        if (object instanceof Iterable)
        {
            Iterable itr = (Iterable) object;
            boolean first = true;

            buffer.append('[');
            Iterator i = itr.iterator();
            while (i.hasNext())
            {
                if (!first) buffer.append(", ");

                convert(buffer, i.next());
                first = false;
            }
            buffer.append(']');
            return;
        }

        // Might need to add a few more, for things like character values ...

        buffer.append(object.toString());
    }

    /**
     * Invoked when a method returns a value
     *
     * @param name   of the method
     * @param result the return value for the method invocation
     */
    public void exit(String name, Object result)
    {
        Defense.notNull(name, "name");

        StringBuilder buffer = new StringBuilder();

        buffer.append(format("[%s] %s [", EXIT, name));

        convert(buffer, result);

        buffer.append(']');

        _logger.debug(buffer.toString());
    }

    /**
     * Invoked when void method finishes succesfully.
     */
    public void voidExit(String name)
    {
        _logger.debug(format("[%s] %s", EXIT, name));
    }

    /**
     * Invoked when method invocation instead throws an exception.
     */
    public void fail(String name, Throwable t)
    {
        if (_logger.isDebugEnabled())
        {
            _logger.debug(
                    format("[%s] %s -- %s", FAIL, name, t.getClass().getName()),
                    _exceptionTracker.exceptionLogged(t) ? null : t);
        }
    }
}
