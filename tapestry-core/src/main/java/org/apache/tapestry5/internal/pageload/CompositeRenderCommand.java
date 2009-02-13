// Copyright 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;

/**
 * A RenderCommand wrapper that renders internally a series of render commands. This is intended for static content
 * (commands that may write content, but won't affect the render queue itself.
 */
class CompositeRenderCommand implements RenderCommand
{
    /**
     * Composite commands are intended for static elements; elements that won't invoke methods on the RenderQueue. To
     * enforce this, we have a NO-OP version of RenderQueue that is passed to the composed render commands.
     */
    private static final RenderQueue NOOP = new RenderQueue()
    {
        public void push(RenderCommand command)
        {
            nyi("push");
        }

        public void startComponent(ComponentResources resources)
        {
            nyi("startComponent");
        }

        public void endComponent()
        {
            nyi("endComponent");
        }

        private void nyi(String methodName)
        {
            throw new IllegalStateException(
                    String.format("RenderQueue method %s() is not implemented for composited render commands.",
                                  methodName));
        }
    };

    private final RenderCommand[] commands;

    public CompositeRenderCommand(RenderCommand[] commands)
    {
        this.commands = commands;
    }

    public void render(MarkupWriter writer, RenderQueue queue)
    {
        for (RenderCommand c : commands)
        {
            c.render(writer, NOOP);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("CompositeRenderCommand[");

        boolean comma = false;

        for (RenderCommand c : commands)
        {
            if (comma) builder.append(", ");

            builder.append(c);

            comma = true;
        }

        return builder.append("]").toString();
    }
}
