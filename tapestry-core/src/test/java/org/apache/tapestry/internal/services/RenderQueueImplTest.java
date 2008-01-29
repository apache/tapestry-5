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

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.runtime.RenderCommand;
import org.apache.tapestry.runtime.RenderQueue;
import org.slf4j.Logger;
import org.testng.annotations.Test;

public class RenderQueueImplTest extends InternalBaseTestCase
{
    @Test
    public void run_commands()
    {
        final RenderCommand command2 = newMock(RenderCommand.class);
        RenderCommand command1 = new RenderCommand()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                queue.push(command2);
            }
        };

        Logger logger = mockLogger();
        MarkupWriter writer = mockMarkupWriter();
        RenderQueueImpl queue = new RenderQueueImpl(logger);

        // There's only one check for trace enabled now.

        train_isTraceEnabled(logger, false);

        command2.render(writer, queue);

        replay();

        queue.push(command1);
        queue.run(writer);

        verify();
    }

    @Test
    public void command_failed()
    {
        final RuntimeException t = new RuntimeException("Oops.");

        RenderCommand rc = new RenderCommand()
        {

            public void render(MarkupWriter writer, RenderQueue queue)
            {
                throw t;
            }

            @Override
            public String toString()
            {
                return "FailedCommand";
            }
        };

        Logger logger = mockLogger();
        MarkupWriter writer = mockMarkupWriter();

        train_isTraceEnabled(logger, false);

        logger.error("Render queue error in FailedCommand: Oops.", t);

        replay();

        RenderQueueImpl queue = new RenderQueueImpl(logger);

        queue.startComponent("foo");
        queue.startComponent("bar");
        queue.endComponent();
        queue.startComponent("baz");

        queue.push(rc);

        try
        {
            queue.run(writer);
            unreachable();
        }
        catch (RenderQueueException ex)
        {
            assertSame(ex.getCause(), t);

            assertArraysEqual(ex.getActiveComponentIds(), new String[]{"foo", "baz"});
        }

        verify();
    }

}
