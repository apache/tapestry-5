// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.pageload;

import org.apache.tapestry5.internal.structure.BodyPageElement;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.util.Stack;
import org.apache.tapestry5.runtime.RenderCommand;

import java.util.List;
import java.util.Set;

/**
 * Mutable data used when {@link org.apache.tapestry5.internal.pageload.ComponentAssembler}s are assembling a page
 * instance.
 */
class PageAssembly
{
    public final Page page;

    public final Stack<ComponentPageElement> activeElement = CollectionFactory.newStack();

    public final Stack<BodyPageElement> bodyElement = CollectionFactory.newStack();

    public final Stack<ComponentPageElement> createdElement = CollectionFactory.newStack();

    public final Stack<EmbeddedComponentAssembler> embeddedAssembler = CollectionFactory.newStack();

    public final List<PageAssemblyAction> deferred = CollectionFactory.newList();

    private final List<RenderCommand> composableRenderCommands = CollectionFactory.newList();

    private final Set<String> flags = CollectionFactory.newSet();

    PageAssembly(Page page)
    {
        this.page = page;
    }

    /**
     * Adds the command to the top element of the {@link #bodyElement} stack. {@linkplain
     * #flushComposableRenderCommands() Flushes} composable render commands first.
     *
     * @param command
     */
    public void addRenderCommand(RenderCommand command)
    {
        flushComposableRenderCommands();

        bodyElement.peek().addToBody(command);
    }

    /**
     * Adds the command to the list of composable commands. Composable commands are added to the top element of the body
     * element stack when {@linkplain #flushComposableRenderCommands() flushed}.
     *
     * @param command
     */
    public void addComposableRenderCommand(RenderCommand command)
    {
        composableRenderCommands.add(command);
    }

    /**
     * Adds any composed render commands to the top element of the bodyElement stack. Render commands may be combined as
     * a {@link org.apache.tapestry5.internal.pageload.CompositeRenderCommand}.
     */
    public void flushComposableRenderCommands()
    {
        int count = composableRenderCommands.size();

        switch (count)
        {
            case 0:
                break;

            case 1:
                bodyElement.peek().addToBody(composableRenderCommands.get(0));
                break;

            default:
                RenderCommand[] commands = composableRenderCommands.toArray(new RenderCommand[count]);

                bodyElement.peek().addToBody(new CompositeRenderCommand(commands));
                break;
        }

        composableRenderCommands.clear();
    }

    public boolean checkAndSetFlag(String flagName)
    {
        boolean result = flags.contains(flagName);

        if (!result)
            flags.add(flagName);

        return result;
    }
}


