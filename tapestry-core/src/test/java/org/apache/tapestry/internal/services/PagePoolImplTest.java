// Copyright 2006, 2007 The Apache Software Foundation
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

import static org.easymock.EasyMock.contains;

import java.util.Locale;

import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.apache.tapestry.services.ComponentClassResolver;
import org.slf4j.Logger;
import org.testng.annotations.Test;

public class PagePoolImplTest extends InternalBaseTestCase
{
    private static final String INPUT_PAGE_NAME = "mypage";

    private static final String LOGICAL_PAGE_NAME = "MyPage";

    // This will change once we start supporting application localization.

    private final Locale _locale = Locale.getDefault();

    @Test
    public void checkout_when_page_list_is_null()
    {
        PageLoader loader = mockPageLoader();
        Page page = mockPage();
        ThreadLocale tl = mockThreadLocale();
        ComponentClassResolver resolver = mockComponentClassResolver();
        Logger logger = mockLogger();

        train_canonicalizePageName(resolver, INPUT_PAGE_NAME, LOGICAL_PAGE_NAME);

        train_getLocale(tl, _locale);

        train_loadPage(loader, LOGICAL_PAGE_NAME, _locale, page);

        replay();

        PagePool pool = new PagePoolImpl(logger, loader, tl, resolver);

        assertSame(page, pool.checkout(INPUT_PAGE_NAME));

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
        ComponentClassResolver resolver = mockComponentClassResolver();

        train_canonicalizePageName(resolver, INPUT_PAGE_NAME, LOGICAL_PAGE_NAME);

        train_getLocale(tl, _locale);

        train_loadPage(loader, LOGICAL_PAGE_NAME, _locale, page1);

        replay();

        PagePool pool = new PagePoolImpl(logger, loader, tl, resolver);

        assertSame(pool.checkout(INPUT_PAGE_NAME), page1);

        verify();

        train_detached(page1, false);
        train_getLogicalName(page1, LOGICAL_PAGE_NAME);
        train_getLocale(page1, _locale);

        replay();

        pool.release(page1);

        verify();

        train_canonicalizePageName(resolver, INPUT_PAGE_NAME, LOGICAL_PAGE_NAME);
        train_getLocale(tl, _locale);

        train_canonicalizePageName(resolver, INPUT_PAGE_NAME, LOGICAL_PAGE_NAME);
        train_getLocale(tl, _locale);

        train_loadPage(loader, LOGICAL_PAGE_NAME, _locale, page2);

        replay();

        assertSame(pool.checkout(INPUT_PAGE_NAME), page1);
        assertSame(pool.checkout(INPUT_PAGE_NAME), page2);

        verify();

    }

    @Test
    public void dirty_pages_are_not_pooled()
    {
        PageLoader loader = mockPageLoader();
        Page page = mockPage();
        Logger logger = mockLogger();

        train_detached(page, true);

        logger.error(contains("is dirty, and will be discarded"));

        // The fact that we don't ask
        // the page for its name is our clue that it is not being cached.

        replay();

        PagePool pool = new PagePoolImpl(logger, loader, null, null);

        pool.release(page);

        verify();
    }
}
