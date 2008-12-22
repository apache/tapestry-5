// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.Renderable;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;

import java.util.List;

/**
 * Used by {@link org.apache.tapestry5.internal.structure.ComponentPageElementImpl} to track the results of invoking the
 * component methods for a render phase event.
 *
 * @since 5.0.19
 */
class RenderPhaseEventHandler implements ComponentEventCallback
{
    private boolean result = true;

    private List<RenderCommand> commands;

    boolean getResult()
    {
        return result;
    }

    void reset()
    {
        result = true;

        commands = null;
    }

    public boolean handleResult(Object result)
    {
        if (result instanceof Boolean)
        {
            this.result = (Boolean) result;
            return true; // abort other handler methods
        }

        if (result instanceof RenderCommand)
        {
            RenderCommand command = (RenderCommand) result;

            add(command);

            return false; // do not abort!
        }

        if (result instanceof Renderable)
        {
            final Renderable renderable = (Renderable) result;

            RenderCommand wrapper = new RenderCommand()
            {
                public void render(MarkupWriter writer, RenderQueue queue)
                {
                    renderable.render(writer);
                }
            };

            add(wrapper);

            return false;
        }

        throw new RuntimeException(StructureMessages.wrongPhaseResultType(Boolean.class));
    }

    private void add(RenderCommand command)
    {
        if (commands == null) commands = CollectionFactory.newList();

        commands.add(command);
    }

    public void queueCommands(RenderQueue queue)
    {
        if (commands == null) return;

        for (RenderCommand command : commands)
            queue.push(command);
    }
}
