// Copyright 2006 The Apache Software Foundation
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

import org.apache.commons.logging.Log;
import org.apache.tapestry.events.InvalidationListener;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.internal.services.ThreadLocaleImpl;
import org.apache.tapestry.ioc.services.ThreadLocale;
import org.testng.annotations.Test;

/**
 * 
 */
public class PagePoolImplTest extends InternalBaseTestCase
{
    private static final String PAGE_NAME = "com.foo.pages.MyPage";

    // This will change once we start supporting application localization.

    private final Locale _locale = Locale.getDefault();

    @Test
    public void checkout_when_page_list_is_null()
    {
        PageLoader loader = newPageLoader();
        Page page = newPage();
        ThreadLocale tl = newThreadLocale();

        train_getLocale(tl, _locale);

        train_loadPage(loader, PAGE_NAME, _locale, page);

        replay();

        PagePool pool = new PagePoolImpl(null, loader, tl);

        assertSame(page, pool.checkout(PAGE_NAME));

        verify();
    }

    @Test
    public void checkout_when_page_list_is_empty()
    {
        final Page page1 = new NoOpPage(PAGE_NAME, _locale);
        final Page page2 = new NoOpPage(PAGE_NAME, _locale);

        PageLoader loader = new PageLoader()
        {

            public void addInvalidationListener(InvalidationListener listener)
            {

            }

            public Page loadPage(String pageClassName, Locale locale)
            {
                if (pageClassName.equals(PAGE_NAME))
                {
                    return page2;
                }
                return null;
            }

        };
        ThreadLocale threadLocale = new ThreadLocaleImpl();
        threadLocale.setLocale(_locale);
        PagePool pool = new PagePoolImpl(null, loader, threadLocale);
        pool.release(page1);
        assertSame(page1, pool.checkout(PAGE_NAME));
        // Now the list is empty, but not null.
        assertSame(page2, pool.checkout(PAGE_NAME));
    }

    // This should move up to IOCTestCase

    protected final void train_detached(Page page, boolean dirty)
    {
        expect(page.detached()).andReturn(dirty);
    }

    @Test
    public void release_last_in_first_out()
    {
        final Page page1 = new NoOpPage(PAGE_NAME, _locale);
        final Page page2 = new NoOpPage(PAGE_NAME, _locale);

        PageLoader loader = new PageLoader()
        {

            public void addInvalidationListener(InvalidationListener listener)
            {

            }

            public Page loadPage(String pageClassName, Locale locale)
            {
                fail();
                return null;
            }

        };
        ThreadLocale threadLocale = new ThreadLocaleImpl();
        threadLocale.setLocale(_locale);
        PagePool pool = new PagePoolImpl(null, loader, threadLocale);

        pool.release(page1);
        pool.release(page2);

        assertSame(page2, pool.checkout(PAGE_NAME));
        assertSame(page1, pool.checkout(PAGE_NAME));
    }

    @Test
    public void dirty_pages_are_not_pooled()
    {
        PageLoader loader = newPageLoader();
        Page page = newPage();
        Log log = newLog();

        train_detached(page, true);

        log.error(contains("is dirty, and will be discarded"));

        // The fact that we don't ask the page for its name is our clue that it is not being cached.

        replay();

        PagePool pool = new PagePoolImpl(log, loader, null);

        pool.release(page);

        verify();
    }

    @Test
    public void diff_locales()
    {
        final Page germanPage = new NoOpPage("p1", Locale.GERMAN);
        final Page frenchPage = new NoOpPage("p1", Locale.FRENCH);

        PageLoader loader = new PageLoader()
        {

            public void addInvalidationListener(InvalidationListener listener)
            {

            }

            public Page loadPage(String pageClassName, Locale locale)
            {
                if (pageClassName.equals("p1"))
                {
                    return locale.equals(Locale.GERMAN) ? germanPage
                            : locale.equals(Locale.FRENCH) ? frenchPage : null;
                }
                return null;
            }

        };
        ThreadLocale threadLocale = new ThreadLocaleImpl();
        PagePool pool = new PagePoolImpl(null, loader, threadLocale);
        threadLocale.setLocale(Locale.GERMAN);
        Page page = pool.checkout("p1");
        assertSame(page, germanPage);
        pool.release(page);
        threadLocale.setLocale(Locale.FRENCH);
        page = pool.checkout("p1");
        assertSame(page, frenchPage);
        pool.release(page);
    }
}
