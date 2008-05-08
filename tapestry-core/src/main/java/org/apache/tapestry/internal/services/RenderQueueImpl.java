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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.util.Stack;
import org.apache.tapestry.runtime.RenderCommand;
import org.apache.tapestry.runtime.RenderQueue;
import org.slf4j.Logger;

public class RenderQueueImpl implements RenderQueue
{
    private static final int INITIAL_QUEUE_DEPTH = 100;

    private final Stack<RenderCommand> queue = CollectionFactory.newStack(INITIAL_QUEUE_DEPTH);

    private final Stack<ComponentResources> renderingComponents = CollectionFactory.newStack();

    private final Logger _logger;

    public RenderQueueImpl(Logger logger)
    {
        _logger = logger;
    }

    public void push(RenderCommand command)
    {
        queue.push(command);
    }

    public void run(MarkupWriter writer)
    {
        RenderCommand command = null;

        boolean traceEnabled = _logger.isTraceEnabled();

        long startNanos = System.nanoTime();
        int commandCount = 0;

        // Seems to make sense to use one try/finally around the whole process, rather than
        // around each call to render() since the end result (in a failure scenario) is the same.

        try
        {
            while (!queue.isEmpty())
            {
                command = queue.pop();

                commandCount++;

                if (traceEnabled) _logger.trace(String.format("Executing: %s", command));

                command.render(writer, this);
            }
        }
        catch (RuntimeException ex)
        {
            // This will likely leave the page in a dirty state, and it will not go back into the
            // page pool.

            String message = ServicesMessages.renderQueueError(command, ex);

            _logger.error(message, ex);

            throw new RenderQueueException(message, renderingComponents.getSnapshot(), ex);
        }

        long endNanos = System.nanoTime();

        if (_logger.isDebugEnabled())
        {

            long elapsedNanos = endNanos - startNanos;
            double elapsedSeconds = ((float) elapsedNanos) / 1000000000F;

            _logger.debug(String.format("Executed %,d rendering commands in %.3f seconds",
                                        commandCount,
                                        elapsedSeconds));
        }

    }

    public void startComponent(ComponentResources resources)
    {
        Defense.notNull(resources, "resources");

        renderingComponents.push(resources);
    }

    public void endComponent()
    {
        renderingComponents.pop();
    }
}
