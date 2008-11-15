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
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Used by  {@link org.apache.tapestry5.internal.services.PagePoolImpl} to maintain a cache of available and in-use page
 * instances.
 * <p/>
 * This code is designed to handle high volume sites and deal with request fluctuations.
 * <p/>
 * Page instances, once created, are tracked with <strong>soft</strong> references.
 * <p/>
 * A <em>soft limit</em> on the number of page instances is enforced. Requesting a page instance when the soft limit has
 * been reached (or exceeded) will result in a delay until a page instance (released from another thread) is available.
 * The delay time is configurable.
 * <p/>
 * A <em>hard limit</em> on the number of page instances is enforced. This number may not be exceeded. Requesting a page
 * instance when at the hard limit will result in a runtime exception.
 */
final class PagePoolCache
{
    private final String pageName;

    private final Locale locale;

    private final int softLimit;

    private final long softWait;

    private final int hardLimit;

    private final long activeWindow;

    private final PageLoader pageLoader;

    /**
     * Pages that are available for use.
     */
    private final LinkedList<CachedPage> available = CollectionFactory.newLinkedList();

    /**
     * Pages that are currently in use.
     */
    private final LinkedList<CachedPage> inUse = CollectionFactory.newLinkedList();

    /**
     * Guards access to the available and in use lists.
     */
    private final Lock lock = new ReentrantLock();

    /**
     * Condition signalled whenever an in-use page is returned to the cache, which is useful if some other thread may be
     * waiting for a page to be available.
     */
    private final Condition pageAvailable = lock.newCondition();

    /**
     * Tracks the usage of a page instance, allowing a last access property to be associated with the page. CachedPage
     * instances are only accessed from within a {@link org.apache.tapestry5.internal.services.PagePoolCache}, which
     * handles synchronization concerns.
     * <p/>
     * An earlier version of this code used <em>soft references</em>, but those seem to be problematic (the test suite
     * started behaving erratically and response time suffered).  Perhaps that could be addressed via tuning of the VM,
     * but for the meantime, we use hard references and rely more on the soft and hard limits and the culling of unused
     * pages periodically.
     */
    static class CachedPage
    {
        private final Page page;

        private long lastAccess;

        CachedPage(Page page)
        {
            this.page = page;
        }
    }

    /**
     * @param pageName     logical name of page, needed when creating a fresh instance
     * @param locale       locale of the page, needed when creating a fresh instance
     * @param pageLoader   used to create a fresh page instance, if necessary
     * @param softLimit    soft limit on pages, point at which the cache will wait for an existing page to be made
     *                     available
     * @param softWait     interval, in milliseconds, to wait for a page to become available
     * @param hardLimit    maximum number of page instances that will ever be created
     * @param activeWindow interval, in milliseconds, beyond which an available page is simply discarded
     */
    public PagePoolCache(String pageName, Locale locale, PageLoader pageLoader, int softLimit, long softWait,
                         int hardLimit, long activeWindow)
    {
        this.pageName = pageName;
        this.locale = locale;
        this.pageLoader = pageLoader;
        this.softLimit = softLimit;
        this.softWait = softWait;
        this.hardLimit = hardLimit;
        this.activeWindow = activeWindow;
    }

    /**
     * Finds an available page instance and returns it.  If no page instance is available, will wait up to the soft wait
     * for one to become available. After that time, it will either create a new instance, or give up (the hard instance
     * limit has been reached) and throw an exception.
     *
     * @return page instance
     * @throws RuntimeException if the hard limit is reached, or if there is an error loading a new page instance
     */
    Page checkout()
    {
        // The only problem here is that *each* new page attached to the request
        // may wait the soft limit.  The alternative would be to timestamp the request
        // itself, and compute the wait period from that ... a dangerous and expensive option.

        long start = System.currentTimeMillis();

        // We don't set a wait on acquiring the lock; it is assumed that any given thread will
        // only have the lock for an instant whether it is checking for an available page, or
        // releasing pages from the in use list back into the active list. We go to some trouble to
        // ensure that the PageLoader is invoked OUTSIDE of the lock.

        lock.lock();

        try
        {


            while (true)
            {
                // We have the write lock, see if there is an available cached page we can use.

                Page page = findAvailablePage();

                if (page != null) return page;

                // Now it starts to get tricky.  Have we hit the soft limit yet?
                // We assume that none of the in use pages' soft references are cleared,
                // which is largely accurate as long as there haven't been a lot
                // of request exceptions.  We'll take the count at face value.

                if (inUse.size() < softLimit) break;

                // We'll wait for pages to be available, but careful that the
                // total wait period is less than the soft wait limit.

                long waitMillis = (start + softWait) - System.currentTimeMillis();

                // We've run out of time to wait.

                if (waitMillis < 1) break;

                try
                {
                    // Note: await() will release the lock, but will re-acquire it
                    // before returning. 
                    pageAvailable.await(waitMillis, TimeUnit.MILLISECONDS);
                }
                catch (InterruptedException ex)
                {
                    // Not sure who is interrupting us (the servlet container)? But returning null
                    // is the fastest way to bounce out of the thread.

                    return null;
                }

                // Loop back up and see if an active page is available.  It won't always be,
                // because of race conditions, so we may wait again.
            }

            // We get here if we exhausted the softWait interval without actually
            // acquiring a page.            

            // If past the hard limit, we don't try to create the page fresh.

            if (inUse.size() >= hardLimit)
                throw new RuntimeException(ServicesMessages.pagePoolExausted(pageName, locale, hardLimit));
        }
        finally
        {
            lock.unlock();
        }

        // This may take a moment, so we're careful to do it outside of a write lock.
        // That does mean that we may slip over a hard or soft limit momentarily, if
        // just the right race condition occurs.

        Page page = pageLoader.loadPage(pageName, locale);

        lock.lock();

        try
        {
            inUse.addFirst(new CachedPage(page));
        }
        finally
        {
            lock.unlock();
        }

        return page;
    }

    /**
     * Finds and returns the first available page.
     * <p/>
     * Side effect: removes the {@link org.apache.tapestry5.internal.services.PagePoolCache.CachedPage} from the
     * available list and moves it to the in use list.
     *
     * @return the page, if any found, or null if no page is available
     */
    private Page findAvailablePage()
    {
        if (available.isEmpty()) return null;

        CachedPage cachedPage = available.removeFirst();

        inUse.addFirst(cachedPage);

        return cachedPage.page;
    }

    /**
     * Invoked to release an active page back into the pool.
     */
    void release(Page page)
    {
        lock.lock();

        try
        {
            CachedPage cached = null;

            ListIterator<CachedPage> i = inUse.listIterator();

            while (i.hasNext())
            {
                cached = i.next();

                if (cached.page == page)
                {
                    i.remove();
                    break;
                }
            }

            // This should not ever happen. The only scenario I can think of is if a page instance
            // was in use before the page pool was cleared (due to a file check invalidation notification).
            // That's not supposed to happen, CheckForUpdatesFilter ensures that all threads but one
            // or blocked on the outside when a file check occurs.

            // So, cached is null means that the page instance was not created by this
            // PagePoolCache, so we're not interested in keeping it.

            if (cached == null) return;

            cached.lastAccess = System.currentTimeMillis();

            available.addFirst(cached);

            pageAvailable.signal();
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Called for dirty pages, pages that are in an unknown state after being used for the request. Such pages are
     * removed from the in use list and NOT added back to the active list.
     */
    void remove(Page page)
    {
        lock.lock();

        try
        {
            ListIterator<CachedPage> i = inUse.listIterator();

            while (i.hasNext())
            {
                CachedPage cached = i.next();

                if (cached.page == page)
                {
                    i.remove();

                    break;
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Finds any cached pages whose last modified time is beyond the active window, meaning they haven't been used in
     * some amount of time., and releases them to the garbage collector.
     */
    void cleanup()
    {
        long cutoff = System.currentTimeMillis() - activeWindow;

        lock.lock();

        try
        {

            ListIterator<CachedPage> i = available.listIterator();

            while (i.hasNext())
            {
                CachedPage cached = i.next();

                if (cached.lastAccess < cutoff) i.remove();
            }
        }
        finally
        {
            lock.unlock();
        }
    }
}
