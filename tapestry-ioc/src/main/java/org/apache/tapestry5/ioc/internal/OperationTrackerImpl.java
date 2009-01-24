//  Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.Stack;
import org.slf4j.Logger;

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

    public void run(String description, final Runnable operation)
    {
        Defense.notBlank(description, "description");
        Defense.notNull(operation, "operation");

        invoke(description, new Invokable<Void>()
        {
            public Void invoke()
            {
                operation.run();

                return null;
            }
        });
    }

    public <T> T invoke(String description, Invokable<T> operation)
    {
        Defense.notBlank(description, "description");
        Defense.notNull(operation, "operation");

        operations.push(description);

        try
        {
            return operation.invoke();
        }
        catch (RuntimeException ex)
        {
            if (!logged)
            {

                logger.error(InternalUtils.toMessage(ex));
                logger.error("Operations trace:");

                Object[] snapshot = operations.getSnapshot();
                String[] trace = new String[snapshot.length];

                for (int i = 0; i < snapshot.length; i++)
                {
                    trace[i] = snapshot[i].toString();

                    logger.error(String.format("[%2d] %s", i + 1, trace[i]));
                }

                logged = true;

                throw new OperationException(ex, trace);
            }

            throw ex;
        }
        finally
        {
            operations.pop();

            // We've finally backed out of the operation stack ... but there may be more to come!

            if (operations.isEmpty()) logged = false;
        }
    }

    boolean isEmpty()
    {
        return operations.isEmpty();
    }
}
