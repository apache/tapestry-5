// Copyright 2006-2014 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Locale;

import static org.easymock.EasyMock.contains;
import static org.easymock.EasyMock.same;

public class PageImplTest extends InternalBaseTestCase
{
    private final ComponentResourceSelector selector = new ComponentResourceSelector(Locale.ENGLISH);

    private static final String LOGICAL_PAGE_NAME = "MyPage";

    private PerthreadManager perThreadManager;

    @BeforeClass
    public void setup()
    {
        perThreadManager = getService(PerthreadManager.class);
    }

    @AfterMethod
    public void cleanup()
    {
        perThreadManager.cleanup();
    }

    private MetaDataLocator newMetaDataLocator(String pageName, boolean enabled)
    {

        MetaDataLocator locator = newMock(MetaDataLocator.class);

        expect(locator.findMeta(MetaDataConstants.UNKNOWN_ACTIVATION_CONTEXT_CHECK, pageName, Boolean.class)).andReturn(enabled);

        return locator;
    }

    @Test
    public void accessor_methods()
    {
        ComponentPageElement root = mockComponentPageElement();
        ComponentResourceSelector selector = new ComponentResourceSelector(Locale.ENGLISH);
        MetaDataLocator locator = newMetaDataLocator(LOGICAL_PAGE_NAME, true);

        replay();

        Page page = new PageImpl(LOGICAL_PAGE_NAME, selector, null, perThreadManager, locator);

        assertNull(page.getRootElement());

        page.setRootElement(root);

        assertSame(page.getSelector(), selector);
        assertSame(page.getRootElement(), root);
        assertSame(page.getName(), LOGICAL_PAGE_NAME);

        assertTrue(page.isExactParameterCountMatch());

        verify();
    }

    @Test
    public void detach_notification()
    {
        PageLifecycleListener listener1 = newPageLifecycle();
        PageLifecycleListener listener2 = newPageLifecycle();

        listener1.containingPageDidDetach();
        listener2.containingPageDidDetach();

        MetaDataLocator locator = newMetaDataLocator(LOGICAL_PAGE_NAME, true);

        replay();

        Page page = new PageImpl(LOGICAL_PAGE_NAME, selector, null, perThreadManager, locator);

        page.addLifecycleListener(listener1);
        page.addLifecycleListener(listener2);

        assertFalse(page.detached());

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
        MetaDataLocator locator = newMetaDataLocator(LOGICAL_PAGE_NAME, true);

        train_getLogger(element, logger);

        listener1.containingPageDidDetach();
        setThrowable(t);

        logger.error(contains("failed during page detach"), same(t));

        listener2.containingPageDidDetach();

        replay();

        Page page = new PageImpl(LOGICAL_PAGE_NAME, selector, null, perThreadManager, locator);
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
        MetaDataLocator locator = newMetaDataLocator(LOGICAL_PAGE_NAME, true);

        PageLifecycleListener listener1 = newPageLifecycle();
        PageLifecycleListener listener2 = newPageLifecycle();

        listener1.containingPageDidAttach();
        listener2.containingPageDidAttach();

        replay();

        Page page = new PageImpl(LOGICAL_PAGE_NAME, selector, null, perThreadManager, locator);

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
        MetaDataLocator locator = newMetaDataLocator(LOGICAL_PAGE_NAME, true);

        PageLifecycleListener listener1 = newPageLifecycle();
        PageLifecycleListener listener2 = newPageLifecycle();

        listener1.containingPageDidLoad();
        listener2.containingPageDidLoad();

        replay();

        Page page = new PageImpl(LOGICAL_PAGE_NAME, selector, null, perThreadManager, locator);

        page.addLifecycleListener(listener1);
        page.addLifecycleListener(listener2);

        page.loaded();

        verify();
    }

    @Test
    public void get_by_nested_id_for_blank_value_returns_root_component()
    {
        ComponentPageElement root = mockComponentPageElement();
        MetaDataLocator locator = newMetaDataLocator(LOGICAL_PAGE_NAME, true);

        replay();

        Page page = new PageImpl(LOGICAL_PAGE_NAME, selector, null, perThreadManager, locator);

        page.setRootElement(root);

        assertSame(page.getComponentElementByNestedId(""), root);

        verify();
    }
}
