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

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.runtime.RenderQueue;
import org.testng.annotations.Test;

public class BlockImplTest extends InternalBaseTestCase
{
    @Test
    public void empty_block()
    {
        BlockImpl block = new BlockImpl(null);
        RenderQueue queue = newRenderQueue();
        MarkupWriter writer = newMarkupWriter();

        replay();

        block.render(writer, queue);

        verify();
    }

    @Test
    public void body_pushed_to_queue_backwards()
    {
        BlockImpl block = new BlockImpl(null);
        RenderQueue queue = newRenderQueue();
        MarkupWriter writer = newMarkupWriter();
        PageElement element1 = newPageElement();
        PageElement element2 = newPageElement();

        getMocksControl().checkOrder(true);

        queue.push(element2);
        queue.push(element1);

        replay();

        block.addToBody(element1);
        block.addToBody(element2);

        block.render(writer, queue);

        verify();
    }
}
