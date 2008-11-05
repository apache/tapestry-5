//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.Renderable;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;

/**
 * Allows any {@link org.apache.tapestry5.Renderable} object to act as a {@link org.apache.tapestry5.Block}. Basically,
 * dressed up the Renderable with the Block interface, and delegates the {@link org.apache.tapestry5.Renderable}
 * interface to the underlying renderable object.
 */
public class RenderableAsBlock implements Block, RenderCommand
{
    private final Renderable renderable;

    public RenderableAsBlock(Renderable renderable)
    {
        this.renderable = renderable;
    }

    /**
     * Invokes {@link Renderable#render(org.apache.tapestry5.MarkupWriter)}.
     */
    public void render(MarkupWriter writer, RenderQueue queue)
    {
        renderable.render(writer);
    }

    @Override
    public String toString()
    {
        return String.format("Block[%s]", renderable);
    }
}
