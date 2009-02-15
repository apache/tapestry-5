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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import static org.easymock.EasyMock.contains;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.Locale;

public class PagePoolImplTest extends InternalBaseTestCase
{

    private static final String LOGICAL_PAGE_NAME = "MyPage";

    // This will change once we start supporting application localization.

    private final Locale locale = Locale.getDefault();

    @Test
    public void checkout_when_page_list_is_null()
    {
        PageLoader loader = mockPageLoader();
        Page page = mockPage();
        ThreadLocale tl = mockThreadLocale();
        Logger logger = mockLogger();

        train_getLocale(tl, locale);

        train_loadPage(loader, LOGICAL_PAGE_NAME, locale, page);

        replay();

        PagePool pool = new PagePoolImpl(logger, loader, tl, 5, 0, 20, 600000);

        assertSame(page, pool.checkout(LOGICAL_PAGE_NAME));

        verify();
    }

    @Test
    public void checkout_when_page_list_is_empty()
    {
        Page page1 = mockPage();
        Page page2 = mockPage();
        PageLoader loader = mockPageLoader();
        Logger logger = mockLogger();
        ThreadLocale tl = mockThreadLocale();

        train_getLocale(tl, locale);

        train_loadPage(loader, LOGICAL_PAGE_NAME, locale, page1);

        replay();

        PagePool pool = new PagePoolImpl(logger, loader, tl, 5, 0, 20, 600000);

        assertSame(pool.checkout(LOGICAL_PAGE_NAME), page1);

        verify();

        train_detached(page1, false);
        train_getName(page1, LOGICAL_PAGE_NAME);
        train_getLocale(page1, locale);

        replay();

        pool.release(page1);

        verify();

        train_getLocale(tl, locale);

        train_getLocale(tl, locale);

        train_loadPage(loader, LOGICAL_PAGE_NAME, locale, page2);

        replay();

        assertSame(pool.checkout(LOGICAL_PAGE_NAME), page1);
        assertSame(pool.checkout(LOGICAL_PAGE_NAME), page2);

        verify();
    }

    @Test
    public void dirty_pages_are_not_pooled()
    {
        PageLoader loader = mockPageLoader();
        Page page = mockPage();
        Logger logger = mockLogger();

        train_detached(page, true);
        train_getName(page, "dirty");
        train_getLocale(page, Locale.ENGLISH);

        logger.error(contains("is dirty, and will be discarded"));

        // The fact that we don't ask
        // the page for its name is our clue that it is not being cached.

        replay();

        PagePool pool = new PagePoolImpl(logger, loader, null, 5, 0, 20, 600000);

        pool.release(page);

        verify();
    }
}
