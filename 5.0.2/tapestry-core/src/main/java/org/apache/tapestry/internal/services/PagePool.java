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

/**
 * Provides access to pages, creating them as necessary, and pooling them between requests.
 * 
 * 
 */
public interface PagePool
{
    /**
     * Obtains a page instance from the pool via a page name. A page instance is created if no such
     * page is currently available.
     * 
     * @param pageName
     *            identifies the page name, as a fully qualified class name
     * @return a page instance
     */
    Page checkout(String pageName);

    /**
     * @param page
     *            a previously checked-out page
     */
    void release(Page page);
}
