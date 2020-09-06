// Copyright 2007, 2008, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.BeanBlockContribution;
import org.apache.tapestry5.services.BeanBlockOverrideSource;
import org.apache.tapestry5.services.BeanBlockSource;
import org.apache.tapestry5.services.DisplayBlockContribution;
import org.apache.tapestry5.services.EditBlockContribution;
import org.testng.annotations.Test;

import static org.apache.tapestry5.commons.util.CollectionFactory.newList;

import java.util.Collection;
import java.util.Collections;

public class BeanBlockSourceImplTest extends InternalBaseTestCase
{
    private static final Collection<BeanBlockContribution> EMPTY_CONFIGURATION = Collections.emptyList();

    @Test
    public void found_display_block()
    {
        Block block = mockBlock();
        RequestPageCache cache = mockRequestPageCache();
        Page page = mockPage();
        BeanBlockContribution contribution = new DisplayBlockContribution("mydata", "MyPage", "mydisplay");
        Collection<BeanBlockContribution> configuration = newList(contribution);

        train_get(cache, "MyPage", page);
        train_getBlock(page, "mydisplay", block);

        replay();

        BeanBlockSource source = new BeanBlockSourceImpl(cache, createBeanBlockOverrideSource(cache), configuration);

        // Check case insensitivity while we are at it.
        assertTrue(source.hasDisplayBlock("MyData"));
        Block actual = source.getDisplayBlock("MyData");

        assertSame(actual, block);

        verify();
    }

    @Test
    public void found_display_block_in_override()
    {
        Block block = mockBlock();
        RequestPageCache cache = mockRequestPageCache();
        BeanBlockOverrideSource overrideSource = mockBeanBlockOverrideSource();
        String datatype = "MyData";

        expect(overrideSource.hasDisplayBlock(datatype)).andReturn(true);
        expect(overrideSource.getDisplayBlock(datatype)).andReturn(block);

        replay();

        BeanBlockSource source = new BeanBlockSourceImpl(cache, overrideSource, EMPTY_CONFIGURATION);

        // Check case insensitivity while we are at it.
        assertTrue(source.hasDisplayBlock(datatype));
        Block actual = source.getDisplayBlock(datatype);

        assertSame(actual, block);

        verify();
    }

    protected final BeanBlockOverrideSource mockBeanBlockOverrideSource()
    {
        return newMock(BeanBlockOverrideSource.class);
    }

    private BeanBlockOverrideSource createBeanBlockOverrideSource(RequestPageCache cache)
    {
        return new BeanBlockOverrideSourceImpl(cache, EMPTY_CONFIGURATION);
    }

    @Test
    public void display_block_not_found()
    {
        RequestPageCache cache = mockRequestPageCache();
        Collection<BeanBlockContribution> configuration = newList();

        replay();

        BeanBlockSource source = new BeanBlockSourceImpl(cache, createBeanBlockOverrideSource(cache), configuration);

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

        BeanBlockSource source = new BeanBlockSourceImpl(cache, createBeanBlockOverrideSource(cache), configuration);

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
        BeanBlockContribution contribution = new EditBlockContribution("mydata", "MyPage", "mydisplay");
        Collection<BeanBlockContribution> configuration = newList(contribution);

        train_get(cache, "MyPage", page);
        train_getBlock(page, "mydisplay", block);

        replay();

        BeanBlockSource source = new BeanBlockSourceImpl(cache, createBeanBlockOverrideSource(cache), configuration);

        // Check case insensitivity while we are at it.
        Block actual = source.getEditBlock("MyData");

        assertSame(actual, block);

        verify();
    }

    @Test
    public void found_edit_block_in_override()
    {
        Block block = mockBlock();
        RequestPageCache cache = mockRequestPageCache();
        BeanBlockOverrideSource overrideSource = mockBeanBlockOverrideSource();
        String datatype = "MyData";

        expect(overrideSource.getEditBlock(datatype)).andReturn(block);

        replay();

        BeanBlockSource source = new BeanBlockSourceImpl(cache, overrideSource, EMPTY_CONFIGURATION);

        Block actual = source.getEditBlock(datatype);

        assertSame(actual, block);

        verify();
    }
    
    @Test
    // TAP5-2506
    public void conflicting_bean_block_overrides()
    {
        RequestPageCache cache = mockRequestPageCache();
        Collection<BeanBlockContribution> configuration = CollectionFactory.newList();
        configuration.add(new DisplayBlockContribution("foo", "page1", "bar"));
        configuration.add(new DisplayBlockContribution("foo", "page2", "baz"));
        try {
            new BeanBlockOverrideSourceImpl(cache, configuration);
            unreachable();
        } catch(IllegalArgumentException ex)
        {
            assertEquals(
              ex.getMessage(),
              "The BeanBlockOverrideSource configuration contains multiple display block overrides for data type 'foo'.");

        }
    }

    protected final void train_getBlock(Page page, String blockId, Block block)
    {
        ComponentPageElement element = mockComponentPageElement();
        train_getRootElement(page, element);

        expect(element.getBlock(blockId)).andReturn(block);
    }
}
