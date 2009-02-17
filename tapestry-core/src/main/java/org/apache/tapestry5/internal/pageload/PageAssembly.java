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
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
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
    final Page page;

    final Stack<ComponentPageElement> activeElement = CollectionFactory.newStack();

    final Stack<BodyPageElement> bodyElement = CollectionFactory.newStack();

    final Stack<ComponentPageElement> createdElement = CollectionFactory.newStack();

    final Stack<ComponentName> componentName = CollectionFactory.newStack();

    final Stack<EmbeddedComponentAssembler> embeddedAssembler = CollectionFactory.newStack();

    final List<PageAssemblyAction> deferred = CollectionFactory.newList();

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
    void addRenderCommand(RenderCommand command)
    {
        bodyElement.peek().addToBody(command);
    }

    boolean checkAndSetFlag(String flagName)
    {
        boolean result = flags.contains(flagName);

        if (!result)
            flags.add(flagName);

        return result;
    }

    void checkForRecursion(String componentClassName, Location location)
    {
        for (Object o : activeElement.getSnapshot())
        {
            ComponentPageElement e = (ComponentPageElement) o;

            if (e.getComponentResources().getComponentModel().getComponentClassName().equals(componentClassName))
            {
                String message = String.format(
                        "The template for component %s is recursive (contains another direct or indirect reference to component %<s). This is not supported (components may not contain themselves).",
                        componentClassName);

                throw new TapestryException(message, location, null);
            }
        }
    }
}


