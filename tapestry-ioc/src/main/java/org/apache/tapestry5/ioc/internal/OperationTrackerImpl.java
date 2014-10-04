// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.ExceptionUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Core implementation that manages a logger and catches and reports exception.
 */
public class OperationTrackerImpl implements OperationTracker
{
    private static final String CLASS_NAME = OperationTrackerImpl.class.getName();

    private static final List<String> OPERATIONS = Arrays.asList("run", "invoke", "perform");

    private final Logger logger;

    public OperationTrackerImpl(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void run(String description, final Runnable operation)
    {
        assert InternalUtils.isNonBlank(description);
        assert operation != null;

        long startNanos = start(description);

        try
        {
            operation.run();

            finish(description, startNanos);

        } catch (OperationException oe)
        {
            captureDescription(oe, description);
        } catch (Throwable ex)
        {
            throwNewOperationException(ex, description);
        }
    }

    @Override
    public <T> T invoke(String description, Invokable<T> operation)
    {
        assert InternalUtils.isNonBlank(description);
        assert operation != null;

        long startNanos = start(description);

        try
        {
            T result = operation.invoke();

            finish(description, startNanos);

            return result;

        } catch (OperationException oe)
        {
            return captureDescription(oe, description);
        } catch (Throwable ex)
        {
            return throwNewOperationException(ex, description);
        }
    }

    @Override
    public <T> T perform(String description, IOOperation<T> operation) throws IOException
    {
        InternalUtils.isNonBlank(description);
        assert operation != null;

        long startNanos = start(description);

        try
        {
            T result = operation.perform();

            finish(description, startNanos);

            return result;

        } catch (OperationException oe)
        {
            return captureDescription(oe, description);
        } catch (Throwable ex)
        {
            return throwNewOperationException(ex, description);
        }
    }

    private void finish(String description, long startNanos)
    {
        if (logger.isDebugEnabled())
        {
            long elapsedNanos = System.nanoTime() - startNanos;
            double elapsedMillis = ((double) elapsedNanos) / 1000000.d;

            logger.debug(String.format("[%3d] <-- %s [%,.2f ms]", getDepth(), description, elapsedMillis));
        }
    }

    private long start(String description)
    {
        long startNanos = -1l;

        if (logger.isDebugEnabled())
        {
            logger.debug(String.format("[%3d] --> %s", getDepth(), description));
            startNanos = System.nanoTime();
        }

        return startNanos;
    }

    private <T> T captureDescription(OperationException oe, String description)
    {
        oe.push(description);

        return logOrRethrow(oe);
    }

    private <T> T throwNewOperationException(Throwable ex, String description)
    {
        OperationException oe = new OperationException(ex, description);

        return logOrRethrow(oe);
    }

    /**
     * So, when an exception occurs, at the deepest level, an OperationException is thrown via
     * {@link #throwNewOperationException(Throwable, String)}. Each perform/run/invoke call
     * catches the OperationException, invokes {@link #captureDescription(OperationException, String)} to
     * add a message to it, and rethrows it. This method checks to see if it is the first invocation
     * of perform/run/invoke on the stack and, if so, it logs the operation trace (this is a difference
     * from 5.3, which logged the trace much earlier). After logging, the OperationException, or
     * the cause of the OE, is rethrown.
     *
     * @param oe
     * @param <T>
     * @return
     */
    private <T> T logOrRethrow(OperationException oe)
    {
        if (getDepth() == 1)
        {
            logger.error(ExceptionUtils.toMessage(oe.getCause()));
            logger.error("Operations trace:");

            String[] trace = oe.getTrace();

            for (int i = 0; i < trace.length; i++)
            {
                logger.error(String.format("[%2d] %s", i + 1, trace[i]));
            }

            if (oe.getCause() instanceof Error)
            {
                throw (Error) oe.getCause();
            }
        }

        throw oe;
    }

    private int getDepth()
    {
        int result = 0;

        for (StackTraceElement ste : new Throwable().getStackTrace())
        {
            if (ste.getClassName().equals(CLASS_NAME) && OPERATIONS.contains(ste.getMethodName()))
            {
                ++result;
            }
        }

        return result;
    }

}
