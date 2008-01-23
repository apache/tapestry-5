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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Block;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry.services.BeanBlockContribution;
import org.apache.tapestry.services.BeanBlockSource;
import org.testng.annotations.Test;

import java.util.Collection;

public class BeanBlockSourceImplTest extends InternalBaseTestCase
{
    @Test
    public void found_display_block()
    {
        Block block = mockBlock();
        RequestPageCache cache = mockRequestPageCache();
        Page page = mockPage();
        BeanBlockContribution contribution = new BeanBlockContribution("mydata", "MyPage",
                                                                       "mydisplay", false);
        Collection<BeanBlockContribution> configuration = newList(contribution);

        train_get(cache, "MyPage", page);
        train_getBlock(page, "mydisplay", block);

        replay();

        BeanBlockSource source = new BeanBlockSourceImpl(cache, configuration);

        // Check case insensitivity while we are at it.
        assertTrue(source.hasDisplayBlock("MyData"));
        Block actual = source.getDisplayBlock("MyData");

        assertSame(actual, block);

        verify();
    }

    @Test
    public void display_block_not_found()
    {
        RequestPageCache cache = mockRequestPageCache();
        Collection<BeanBlockContribution> configuration = newList();

        replay();

        BeanBlockSource source = new BeanBlockSourceImpl(cache, configuration);

        try
        {
            assertFalse(source.hasDisplayBlock("MyData"));
            source.getDisplayBlock("MyData");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "There is no defined way to display data of type \'MyData\'. Make a contribution to the BeanBlockSource service for this type.");
        }

        verify();
    }

    @Test
    public void edit_block_not_found()
    {
        RequestPageCache cache = mockRequestPageCache();
        Collection<BeanBlockContribution> configuration = newList();

        replay();

        BeanBlockSource source = new BeanBlockSourceImpl(cache, configuration);

        try
        {
            source.getEditBlock("MyData");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "There is no defined way to edit data of type \'MyData\'.  Make a contribution to the BeanBlockSource service for this type.");
        }

        verify();
    }

    @Test
    public void found_edit_block()
    {
        Block block = mockBlock();
        RequestPageCache cache = mockRequestPageCache();
        Page page = mockPage();
        BeanBlockContribution contribution = new BeanBlockContribution("mydata", "MyPage",
                                                                       "mydisplay", true);
        Collection<BeanBlockContribution> configuration = newList(contribution);

        train_get(cache, "MyPage", page);
        train_getBlock(page, "mydisplay", block);

        replay();

        BeanBlockSource source = new BeanBlockSourceImpl(cache, configuration);

        // Check case insensitivity while we are at it.
        Block actual = source.getEditBlock("MyData");

        assertSame(actual, block);

        verify();
    }

    protected final void train_getBlock(Page page, String blockId, Block block)
    {
        ComponentPageElement element = mockComponentPageElement();
        train_getRootElement(page, element);

        expect(element.getBlock(blockId)).andReturn(block);
    }
}
