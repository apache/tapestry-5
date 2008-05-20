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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.services.ExceptionTracker;
import org.slf4j.Logger;

import static java.lang.String.format;
import java.util.Iterator;

/**
 * Used by {@link org.apache.tapestry5.ioc.internal.services.LoggingDecoratorImpl} to delegate out logging behavior to a
 * seperate object.
 */
public final class MethodLogger
{
    private static final int BUFFER_SIZE = 200;

    private static final String ENTER = "ENTER";

    private static final String EXIT = " EXIT";

    private static final String FAIL = " FAIL";

    private final Logger logger;

    private final ExceptionTracker exceptionTracker;

    public MethodLogger(Logger logger, ExceptionTracker exceptionTracker)
    {
        this.logger = logger;
        this.exceptionTracker = exceptionTracker;
    }

    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    /**
     * Invoked when a method is first entered
     *
     * @param invocation identifies method invoked as well as parameters passed to method
     */
    public void entry(Invocation invocation)
    {
        StringBuilder buffer = new StringBuilder(BUFFER_SIZE);

        buffer.append(format("[%s] %s(", ENTER, invocation.getMethodName()));

        for (int i = 0; i < invocation.getParameterCount(); i++)
        {
            if (i > 0) buffer.append(", ");

            convert(buffer, invocation.getParameter(i));
        }

        buffer.append(")");

        logger.debug(buffer.toString());
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
     * Invoked when a method exits (possibly returning a value).
     *
     * @param invocation identifies method invocation and  result value
     */
    public void exit(Invocation invocation)
    {
        StringBuilder buffer = new StringBuilder(BUFFER_SIZE);

        buffer.append(format("[%s] %s", EXIT, invocation.getMethodName()));

        if (invocation.getResultType() != void.class)
        {
            buffer.append(" [");
            convert(buffer, invocation.getResult());
            buffer.append(']');
        }

        logger.debug(buffer.toString());
    }

    /**
     * Invoked when method invocation instead throws an exception.
     *
     * @param invocation identifies method invocation which failed
     * @param t          exception throws by method invocation
     */
    public void fail(Invocation invocation, Throwable t)
    {
        logger.debug(
                format("[%s] %s -- %s", FAIL,
                       invocation.getMethodName(),
                       t.getClass().getName()),
                exceptionTracker.exceptionLogged(t) ? null : t);
    }
}
