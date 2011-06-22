// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.Block;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ioc.BaseLocatable;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;

import java.util.List;

public class BlockImpl extends BaseLocatable implements Block, BodyPageElement, RenderCommand
{
    // We could lazily create this, but for (parameter) block elements the case
    // for an empty block is extremely rare.

    private final List<RenderCommand> elements = CollectionFactory.newList();

    private final String description;

    public BlockImpl(Location location, String description)
    {
        super(location);

        this.description = description;
    }

    public void addToBody(RenderCommand element)
    {
        elements.add(element);
    }

    /**
     * Pushes all the elements of the body of this block onto the queue in appropriate order.
     */
    public void render(MarkupWriter writer, RenderQueue queue)
    {
        int count = elements.size();
        for (int i = count - 1; i >= 0; i--)
            queue.push(elements.get(i));
    }

    @Override
    public String toString()
    {
        return String.format("Block[%s, at %s]", description, getLocation());
    }
}
