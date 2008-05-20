// Copyright 2008 The Apache Software Foundation
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
import org.testng.annotations.Test;

import java.util.Locale;

public class PagePoolCacheTest extends InternalBaseTestCase
{
    private static final String PAGE_NAME = "mypage";
    private static final Locale LOCALE = Locale.ENGLISH;

    @Test
    public void inside_of_soft_limit()
    {
        PageLoader loader = mockPageLoader();
        Page page1 = mockPage();
        Page page2 = mockPage();

        train_loadPage(loader, PAGE_NAME, LOCALE, page1);
        train_loadPage(loader, PAGE_NAME, LOCALE, page2);

        replay();

        PagePoolCache cache = new PagePoolCache(PAGE_NAME, LOCALE, loader, 5, 100, 20, 1000);

        assertSame(cache.checkout(), page1);
        assertSame(cache.checkout(), page2);

        verify();
    }

    @Test
    public void reuse_existing_page()
    {
        PageLoader loader = mockPageLoader();
        Page page1 = mockPage();
        Page page2 = mockPage();

        train_loadPage(loader, PAGE_NAME, LOCALE, page1);
        train_loadPage(loader, PAGE_NAME, LOCALE, page2);

        replay();

        PagePoolCache cache = new PagePoolCache(PAGE_NAME, LOCALE, loader, 5, 100, 20, 1000);

        assertSame(cache.checkout(), page1);
        assertSame(cache.checkout(), page2);

        cache.release(page1);

        assertSame(cache.checkout(), page1);

        verify();
    }

    @Test
    public void remove_does_not_reuse_page()
    {
        PageLoader loader = mockPageLoader();
        Page page1 = mockPage();
        Page page2 = mockPage();
        Page page3 = mockPage();

        train_loadPage(loader, PAGE_NAME, LOCALE, page1);
        train_loadPage(loader, PAGE_NAME, LOCALE, page2);
        train_loadPage(loader, PAGE_NAME, LOCALE, page3);

        replay();

        PagePoolCache cache = new PagePoolCache(PAGE_NAME, LOCALE, loader, 5, 100, 20, 1000);

        assertSame(cache.checkout(), page1);
        assertSame(cache.checkout(), page2);

        cache.remove(page1);

        assertSame(cache.checkout(), page3);

        verify();
    }

    @Test
    public void new_instance_after_soft_wait()
    {
        PageLoader loader = mockPageLoader();
        Page page1 = mockPage();
        Page page2 = mockPage();

        train_loadPage(loader, PAGE_NAME, LOCALE, page1);
        train_loadPage(loader, PAGE_NAME, LOCALE, page2);

        replay();

        PagePoolCache cache = new PagePoolCache(PAGE_NAME, LOCALE, loader, 1, 500, 20, 1000);

        assertSame(cache.checkout(), page1);

        long start = System.currentTimeMillis();

        assertSame(cache.checkout(), page2);

        long elapsed = System.currentTimeMillis() - start;

        // Fudging a bit because Java clocks are notoriously innaccurate

        assertTrue(elapsed > 490, "A delay should have occured.");

        verify();
    }

    @Test
    public void hard_limit_failure()
    {
        PageLoader loader = mockPageLoader();
        Page page1 = mockPage();

        train_loadPage(loader, PAGE_NAME, LOCALE, page1);

        replay();

        PagePoolCache cache = new PagePoolCache(PAGE_NAME, LOCALE, loader, 1, 10, 1, 1000);

        assertSame(cache.checkout(), page1);

        try
        {
            cache.checkout();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "The page pool for page 'mypage' (in locale en) has been exausted: there are 1 instances currently being used and no more can be created");
        }

        verify();
    }

    @Test
    public void page_released_by_other_thread() throws Exception
    {
        PageLoader loader = mockPageLoader();
        Page page1 = mockPage();
        final Page page2 = mockPage();

        train_loadPage(loader, PAGE_NAME, LOCALE, page1);
        train_loadPage(loader, PAGE_NAME, LOCALE, page2);

        replay();

        final PagePoolCache cache = new PagePoolCache(PAGE_NAME, LOCALE, loader, 1, 1000, 20, 1000);

        assertSame(cache.checkout(), page1);
        assertSame(cache.checkout(), page2);

        Runnable r = new Runnable()
        {
            public void run()
            {
                sleep(20);

                cache.release(page2);
            }
        };

        new Thread(r).start();

        assertSame(cache.checkout(), page2);

        verify();
    }

    @Test
    public void page_removed_by_other_thread_is_not_used()
    {
        PageLoader loader = mockPageLoader();
        Page page1 = mockPage();
        final Page page2 = mockPage();
        Page page3 = mockPage();

        train_loadPage(loader, PAGE_NAME, LOCALE, page1);
        train_loadPage(loader, PAGE_NAME, LOCALE, page2);
        train_loadPage(loader, PAGE_NAME, LOCALE, page3);

        replay();

        final PagePoolCache cache = new PagePoolCache(PAGE_NAME, LOCALE, loader, 1, 100, 20, 1000);

        assertSame(cache.checkout(), page1);
        assertSame(cache.checkout(), page2);

        Runnable r = new Runnable()
        {
            public void run()
            {
                sleep(20);

                cache.remove(page2);
            }
        };

        new Thread(r).start();

        assertSame(cache.checkout(), page3);

        verify();
    }

    @Test
    public void cleanup()
    {
        PageLoader loader = mockPageLoader();
        Page page1 = mockPage();
        Page page2 = mockPage();
        Page page3 = mockPage();


        train_loadPage(loader, PAGE_NAME, LOCALE, page1);
        train_loadPage(loader, PAGE_NAME, LOCALE, page2);
        train_loadPage(loader, PAGE_NAME, LOCALE, page3);

        replay();

        PagePoolCache cache = new PagePoolCache(PAGE_NAME, LOCALE, loader, 5, 100, 20, 50);

        assertSame(cache.checkout(), page1);
        assertSame(cache.checkout(), page2);

        cache.release(page1);

        // Sleep longer than the active window (10)

        sleep(75);

        cache.release(page2);

        cache.cleanup();

        assertSame(cache.checkout(), page2);

        // Page3 is created because page1 was culled as too old.

        assertSame(cache.checkout(), page3);

        verify();
    }

    private static void sleep(long time)
    {
        try
        {
            Thread.sleep(time);
        }
        catch (Exception ex)
        {
            // Ignore.
        }
    }

}
