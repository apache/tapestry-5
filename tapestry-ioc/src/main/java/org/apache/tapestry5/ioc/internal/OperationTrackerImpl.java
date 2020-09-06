// Copyright 2008-2013 The Apache Software Foundation
//
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

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.commons.util.Stack;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Core implementation that manages a logger and catches and reports exception.
 *
 * @see org.apache.tapestry5.ioc.internal.PerThreadOperationTracker
 */
public class OperationTrackerImpl implements OperationTracker
{
    private final Logger logger;

    private final Stack<String> operations = CollectionFactory.newStack();

    private boolean logged;

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

        } catch (RuntimeException ex)
        {
            logAndRethrow(ex);
        } catch (Error ex)
        {
            handleError(ex);
        } finally
        {
            handleFinally();
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

        } catch (RuntimeException ex)
        {
            return logAndRethrow(ex);
        } catch (Error ex)
        {
            return handleError(ex);
        } finally
        {
            handleFinally();
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

        } catch (RuntimeException ex)
        {
            return logAndRethrow(ex);
        } catch (Error ex)
        {
            return handleError(ex);
        } catch (IOException ex)
        {
            return logAndRethrow(ex);
        } finally
        {
            handleFinally();
        }
    }

    private void handleFinally()
    {
        operations.pop();
        // We've finally backed out of the operation stack ... but there may be more to come!

        if (operations.isEmpty())
        {
            logged = false;
        }
    }

    private <T> T handleError(Error error)
    {
        if (!logged)
        {
            log(error);
            logged = true;
        }

        throw error;
    }

    private void finish(String description, long startNanos)
    {
        if (logger.isDebugEnabled())
        {
            long elapsedNanos = System.nanoTime() - startNanos;
            double elapsedMillis = ((double) elapsedNanos) / 1000000.d;

            logger.debug(String.format("[%3d] <-- %s [%,.2f ms]", operations.getDepth(), description, elapsedMillis));
        }
    }

    private long start(String description)
    {
        long startNanos = -1l;

        if (logger.isDebugEnabled())
        {
            startNanos = System.nanoTime();
            logger.debug(String.format("[%3d] --> %s", operations.getDepth() + 1, description));
        }

        operations.push(description);
        return startNanos;
    }

    private <T> T logAndRethrow(RuntimeException ex)
    {
        if (!logged)
        {
            String[] trace = log(ex);

            logged = true;

            throw new OperationException(ex, trace);
        }

        throw ex;
    }

    private <T> T logAndRethrow(IOException ex) throws IOException
    {
        if (!logged)
        {
            String[] trace = log(ex);

            logged = true;

            throw new OperationException(ex, trace);
        }

        throw ex;
    }

    private String[] log(Throwable ex)
    {
        logger.error(ExceptionUtils.toMessage(ex));
        logger.error("Operations trace:");

        Object[] snapshot = operations.getSnapshot();
        String[] trace = new String[snapshot.length];

        for (int i = 0; i < snapshot.length; i++)
        {
            trace[i] = snapshot[i].toString();

            logger.error(String.format("[%2d] %s", i + 1, trace[i]));
        }

        return trace;
    }

    boolean isEmpty()
    {
        return operations.isEmpty();
    }
}
