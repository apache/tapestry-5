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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

public class RequestPageCacheImplTest extends InternalBaseTestCase
{
    private static final String PAGE_NAME = "MyPage";

    @Test
    public void get_is_cached()
    {
        PagePool pool = mockPagePool();
        Page page = mockPage();

        expect(pool.checkout(PAGE_NAME)).andReturn(page);

        page.attached();

        replay();

        RequestPageCacheImpl cache = new RequestPageCacheImpl(pool);

        assertSame(cache.get(PAGE_NAME), page);

        verify();

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
}
