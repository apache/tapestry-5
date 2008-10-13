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

/**
 * Provides access to pages, creating them as necessary, and pooling them between requests.
 */
public interface PagePool
{
    /**
     * Obtains a page instance from the pool via a logical page name. A page instance is created if no such page is
     * currently available.  The page pool enforces limits on the number of page instances (for any page name / locale
     * combination) and may wait for a page to become available rather than create a new instance. There's also a hard
     * limit, at which point an exception is raised.
     *
     * @param pageName the canonical page name
     * @return a page instance
     * @throws RuntimeException if the name is not valid, if the page cannot be loaded, or if an instance of the page
     *                          can't be created.
     */
    Page checkout(String pageName);

    /**
     * Releases a previously checked-out page.
     *
     * @param page a previously checked-out page
     */
    void release(Page page);

    /**
     * Discards a page, which occurs when there are errors invoking lifecycle methods on the page.
     *
     * @param page a previously checked-out page
     */
    void discard(Page page);
}
