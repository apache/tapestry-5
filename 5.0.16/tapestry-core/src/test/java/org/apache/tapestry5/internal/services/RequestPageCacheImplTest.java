// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.services.ComponentClassResolver;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class RequestPageCacheImplTest extends InternalBaseTestCase
{
    private static final String PAGE_NAME = "edit/EditFoo";

    private static final String CANON_PAGE_NAME = "edit/Foo";

    @Test
    public void get_is_cached()
    {
        PagePool pool = mockPagePool();
        Page page = mockPage();
        ComponentClassResolver resolver = mockComponentClassResolver();

        train_canonicalizePageName(resolver, PAGE_NAME, CANON_PAGE_NAME);

        expect(pool.checkout(CANON_PAGE_NAME)).andReturn(page);

        page.attached();

        replay();

        RequestPageCacheImpl cache = new RequestPageCacheImpl(pool, resolver);

        assertSame(cache.get(PAGE_NAME), page);

        verify();

        train_canonicalizePageName(resolver, CANON_PAGE_NAME, CANON_PAGE_NAME);

        replay();

        // Again, same object, but no PagePool this time.  Also checks that name is
        // properly resolved to canon name.
        assertSame(cache.get(CANON_PAGE_NAME), page);

        verify();

        pool.release(page);

        replay();

        // Now, trigger the release()

        cache.threadDidCleanup();

        verify();
    }

    @Test
    public void failure_in_attach_will_discard_page()
    {
        PagePool pool = mockPagePool();
        Page page = mockPage();
        RuntimeException t = new RuntimeException("Failure in attach.");
        ComponentClassResolver resolver = mockComponentClassResolver();

        train_canonicalizePageName(resolver, PAGE_NAME, CANON_PAGE_NAME);

        expect(pool.checkout(CANON_PAGE_NAME)).andReturn(page);

        page.attached();

        EasyMock.expectLastCall().andThrow(t);

        pool.discard(page);

        replay();

        RequestPageCacheImpl cache = new RequestPageCacheImpl(pool, resolver);

        try
        {
            cache.get(PAGE_NAME);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertSame(ex, t);
        }

        verify();
    }
}
