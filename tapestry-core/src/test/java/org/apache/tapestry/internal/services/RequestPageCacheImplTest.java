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

import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.ComponentClassResolver;
import org.testng.annotations.Test;

/**
 * 
 */
public class RequestPageCacheImplTest extends InternalBaseTestCase
{
    private static final String PAGE_NAME = "MyPage";

    private static final String PAGE_CLASS_NAME = "com.foo.pages.MyPage";

    @Test
    public void get_is_cached()
    {
        ComponentClassResolver resolver = newComponentClassResolver();
        PagePool pool = newPagePool();
        Page page = newPage();

        train_resolvePageNameToClassName(resolver, PAGE_NAME, PAGE_CLASS_NAME);

        expect(pool.checkout(PAGE_CLASS_NAME)).andReturn(page);

        page.attached();

        replay();

        RequestPageCacheImpl cache = new RequestPageCacheImpl(resolver, pool);

        assertSame(cache.get(PAGE_NAME), page);

        verify();

        // Asking for a page always resolves the name to a class (fortunately,
        // this is cached by resolver).

        train_resolvePageNameToClassName(resolver, PAGE_NAME, PAGE_CLASS_NAME);

        replay();

        // Again, same object, but no PagePool this time.
        assertSame(cache.get(PAGE_NAME), page);

        verify();

        pool.release(page);

        replay();

        // Now, trigger the release()

        cache.threadDidCleanup();

        verify();
    }

    @Test
    public void page_does_not_exist()
    {
        ComponentClassResolver resolver = newComponentClassResolver();
        PagePool pool = newPagePool();

        train_resolvePageNameToClassName(resolver, PAGE_NAME, null);

        replay();

        try
        {
            RequestPageCacheImpl cache = new RequestPageCacheImpl(resolver, pool);

            cache.get(PAGE_NAME);

            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Page 'MyPage' is not defined by this application.");
        }

        verify();
    }

    protected final void train_resolvePageNameToClassName(ComponentClassResolver resolver,
            String pageName, String className)
    {
        expect(resolver.resolvePageNameToClassName(pageName)).andReturn(className);
    }
}
