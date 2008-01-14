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

/**
 * Tracks the usage of a page instance, allowing a last access property to be associated with the page. CachedPage
 * instances are only accessed from within a {@link PagePoolCache}, which handles synchronization concerns.
 * <p/>
 * An earlier version of this code used <soft references>, but those seem to be problematic (the test suite started
 * behaving erratically and response time suffered).  Perhaps that could be addressed via tuning of the VM, but for the
 * meantime, we use hard references and rely more on the soft and hard limits and the culling of unused pages
 * periodically.
 */
class CachedPage
{
    private final Page _page;

    private long _lastAccess;

    CachedPage(Page page)
    {
        _page = page;
    }

    /**
     * Returns the page, or null if the reference has been reclaimed by the garbage collector.
     *
     * @return the page or null
     */
    Page get()
    {
        return _page;
    }

    /**
     * Time when the page was last returned to the available list.
     *
     * @return last access time (in milliseconds from the epoch)
     */
    long getLastAccess()
    {
        return _lastAccess;
    }

    void setLastAccess(long lastAccess)
    {
        _lastAccess = lastAccess;
    }
}
