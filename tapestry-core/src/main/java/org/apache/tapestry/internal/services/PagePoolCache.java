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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Used by  {@link org.apache.tapestry.internal.services.PagePoolImpl} to maintain a cache of available and in-use page
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
    private final String _pageName;

    private final Locale _locale;

    private final int _softLimit;

    private final long _softWait;

    private final int _hardLimit;

    private final long _activeWindow;

    private final PageLoader _pageLoader;

    /**
     * Pages that are available for use.
     */
    private LinkedList<CachedPage> _available = CollectionFactory.newLinkedList();

    /**
     * Pages that are currently in use.
     */
    private LinkedList<CachedPage> _inUse = CollectionFactory.newLinkedList();

    /**
     * Guards access to the available and in use lists.
     */
    private final Lock _lock = new ReentrantLock();

    /**
     * Condition signalled whenever an in-use page is returned to the cache, which is useful if some other thread may be
     * waiting for a page to be available.
     */
    private final Condition _pageAvailable = _lock.newCondition();

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
        _pageName = pageName;
        _locale = locale;
        _pageLoader = pageLoader;
        _softLimit = softLimit;
        _softWait = softWait;
        _hardLimit = hardLimit;
        _activeWindow = activeWindow;
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
        // itself, and compute the wait period from that ... an dangerous and expensive option.

        long start = System.currentTimeMillis();

        // We don't set a wait on acquiring the lock; it is assumed that any given thread will
        // only have the lock for an instant whether it is checking for an available page, or
        // releasing pages from the in use list back into the active list. We go to some trouble to
        // ensure that the PageLoader is invoked OUTSIDE of the lock.

        _lock.lock();

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

                if (_inUse.size() < _softLimit) break;

                // We'll wait for pages to be available, but careful that the
                // total wait period is less than the soft wait limit.

                long waitMillis = (start + _softWait) - System.currentTimeMillis();

                // We've run out of time to wait.

                if (waitMillis < 1) break;

                try
                {
                    // Note: await() will release the lock, but will re-acquire it
                    // before returning. 
                    _pageAvailable.await(waitMillis, TimeUnit.MILLISECONDS);
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

            if (_inUse.size() >= _hardLimit)
                throw new RuntimeException(ServicesMessages.pagePoolExausted(_pageName, _locale, _hardLimit));
        }
        finally
        {
            _lock.unlock();
        }

        // This may take a moment, so we're careful to do it outside of a write lock.
        // That does mean that we may slip over a hard or soft limit momentarily, if
        // just the right race condition occurs.

        Page page = _pageLoader.loadPage(_pageName, _locale);

        _lock.lock();

        try
        {
            _inUse.addFirst(new CachedPage(page));
        }
        finally
        {
            _lock.unlock();
        }

        return page;
    }

    /**
     * Finds and returns the first available page.
     * <p/>
     * Side effect: removes the {@link org.apache.tapestry.internal.services.CachedPage} from the available list and
     * moves it to the in use list.
     *
     * @return the page, if any found, or null if no page is available
     */
    private Page findAvailablePage()
    {

        if (_available.isEmpty()) return null;

        CachedPage cachedPage = _available.removeFirst();

        _inUse.addFirst(cachedPage);

        return cachedPage.get();
    }

    /**
     * Invoked to release an active page back into the pool.
     */
    void release(Page page)
    {
        _lock.lock();

        try
        {
            CachedPage cached = null;

            ListIterator<CachedPage> i = _inUse.listIterator();

            while (i.hasNext())
            {
                cached = i.next();

                if (cached.get() == page)
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

            cached.setLastAccess(System.currentTimeMillis());

            _available.addFirst(cached);

            _pageAvailable.signal();

        }
        finally
        {
            _lock.unlock();
        }
    }

    /**
     * Called for dirty pages, pages that are in an unknown state after being used for the request. Such pages are
     * removed from the in use list and NOT added back to the active list.
     */
    void remove(Page page)
    {
        _lock.lock();

        try
        {
            ListIterator<CachedPage> i = _inUse.listIterator();

            while (i.hasNext())
            {
                CachedPage cached = i.next();

                if (cached.get() == page)
                {
                    i.remove();

                    break;
                }
            }
        }
        finally
        {
            _lock.unlock();
        }
    }

    /**
     * Finds any cached pages whose last modified time is beyond the active window, meaning they haven't been used in
     * some amount of time., and releases them to the garbage collector.
     */
    void cleanup()
    {
        long cutoff = System.currentTimeMillis() - _activeWindow;

        _lock.lock();

        try
        {

            ListIterator<CachedPage> i = _available.listIterator();

            while (i.hasNext())
            {
                CachedPage cached = i.next();

                if (cached.getLastAccess() < cutoff) i.remove();
            }
        }
        finally
        {
            _lock.unlock();
        }
    }

}
