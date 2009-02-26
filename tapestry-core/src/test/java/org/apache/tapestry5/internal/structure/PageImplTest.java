// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import static org.easymock.EasyMock.contains;
import static org.easymock.EasyMock.same;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.Locale;

public class PageImplTest extends InternalBaseTestCase
{
    private final Locale locale = Locale.ENGLISH;

    private static final String LOGICAL_PAGE_NAME = "MyPage";

    @Test
    public void accessor_methods()
    {
        ComponentPageElement root = mockComponentPageElement();

        replay();

        Page page = new PageImpl(LOGICAL_PAGE_NAME, locale, null);

        assertNull(page.getRootElement());

        page.setRootElement(root);

        assertSame(page.getLocale(), locale);
        assertSame(page.getRootElement(), root);
        assertSame(page.getName(), LOGICAL_PAGE_NAME);

        verify();
    }

    @Test
    public void detach_notification()
    {
        PageLifecycleListener listener1 = newPageLifecycle();
        PageLifecycleListener listener2 = newPageLifecycle();

        listener1.containingPageDidDetach();
        listener2.containingPageDidDetach();

        replay();

        Page page = new PageImpl(null, locale, null);

        page.addLifecycleListener(listener1);
        page.addLifecycleListener(listener2);

        assertFalse(page.detached());

        verify();
    }

    /**
     * Also checks that listeners are invoked, even if the page is dirty.
     */
    @Test
    public void detach_dirty_if_dirty_count_non_zero()
    {
        PageLifecycleListener listener = newPageLifecycle();

        listener.containingPageDidDetach();

        replay();

        Page page = new PageImpl(null, locale, null);

        page.addLifecycleListener(listener);

        page.incrementDirtyCount();

        assertTrue(page.detached());

        verify();
    }

    /**
     * Also checks that all listeners are invoked, even if one of them throws an exception.
     */
    @Test
    public void detach_dirty_if_listener_throws_exception()
    {
        ComponentPageElement element = mockComponentPageElement();
        Logger logger = mockLogger();
        PageLifecycleListener listener1 = newPageLifecycle();
        PageLifecycleListener listener2 = newPageLifecycle();
        RuntimeException t = new RuntimeException("Listener detach exception.");

        train_getLogger(element, logger);

        listener1.containingPageDidDetach();
        setThrowable(t);

        logger.error(contains("failed during page detach"), same(t));

        listener2.containingPageDidDetach();

        replay();

        Page page = new PageImpl(null, locale, null);
        page.setRootElement(element);

        page.addLifecycleListener(listener1);
        page.addLifecycleListener(listener2);

        assertTrue(page.detached());

        verify();
    }

    protected final void train_getLogger(ComponentPageElement element, Logger logger)
    {
        expect(element.getLogger()).andReturn(logger);
    }

    @Test
    public void attach_notification()
    {
        PageLifecycleListener listener1 = newPageLifecycle();
        PageLifecycleListener listener2 = newPageLifecycle();

        listener1.restoreStateBeforePageAttach();
        listener2.restoreStateBeforePageAttach();

        listener1.containingPageDidAttach();
        listener2.containingPageDidAttach();

        replay();

        Page page = new PageImpl(null, locale, null);

        page.addLifecycleListener(listener1);
        page.addLifecycleListener(listener2);

        page.attached();

        verify();
    }

    private PageLifecycleListener newPageLifecycle()
    {
        return newMock(PageLifecycleListener.class);
    }

    @Test
    public void load_notification()
    {
        PageLifecycleListener listener1 = newPageLifecycle();
        PageLifecycleListener listener2 = newPageLifecycle();

        listener1.containingPageDidLoad();
        listener2.containingPageDidLoad();

        replay();

        Page page = new PageImpl(LOGICAL_PAGE_NAME, locale, null);

        page.addLifecycleListener(listener1);
        page.addLifecycleListener(listener2);

        page.loaded();

        verify();
    }

    @Test
    public void get_by_nested_id_for_blank_value_returns_root_component()
    {
        ComponentPageElement root = mockComponentPageElement();

        replay();

        Page page = new PageImpl(LOGICAL_PAGE_NAME, locale, null);

        page.setRootElement(root);

        assertSame(page.getComponentElementByNestedId(""), root);

        verify();
    }
}
