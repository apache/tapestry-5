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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.internal.util.LocationImpl;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.testng.annotations.Test;

public class BlockImplTest extends InternalBaseTestCase
{
    @Test
    public void empty_block()
    {
        BlockImpl block = new BlockImpl(null, null);
        RenderQueue queue = mockRenderQueue();
        MarkupWriter writer = mockMarkupWriter();

        replay();

        block.render(writer, queue);

        verify();
    }

    @Test
    public void body_pushed_to_queue_backwards()
    {
        BlockImpl block = new BlockImpl(null, null);
        RenderQueue queue = mockRenderQueue();
        MarkupWriter writer = mockMarkupWriter();
        RenderCommand element1 = mockRenderCommand();
        RenderCommand element2 = mockRenderCommand();

        getMocksControl().checkOrder(true);

        queue.push(element2);
        queue.push(element1);

        replay();

        block.addToBody(element1);
        block.addToBody(element2);

        block.render(writer, queue);

        verify();
    }

    @Test
    public void to_string()
    {
        Resource r = new ClasspathResource("foo/pages/MyPage.tml");
        Location l = new LocationImpl(r, 23);

        BlockImpl block = new BlockImpl(l, "test block");

        assertEquals(block.toString(), "Block[test block, at classpath:foo/pages/MyPage.tml, line 23]");
    }
}
