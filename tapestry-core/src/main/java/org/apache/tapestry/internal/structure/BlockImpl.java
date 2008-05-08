// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.Block;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ioc.BaseLocatable;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.runtime.RenderCommand;
import org.apache.tapestry.runtime.RenderQueue;

import java.util.List;

public class BlockImpl extends BaseLocatable implements Block, BodyPageElement, RenderCommand
{
    // We could lazily create this, but for <t:block> and <t:parameter>, the case
    // for an empty block is extremely rare.

    private final List<PageElement> elements = CollectionFactory.newList();

    public BlockImpl(Location location)
    {
        super(location);
    }

    public void addToBody(PageElement element)
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

}
