// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
 * Per-thread service that caches page instances for the duration of the request, and is also responsible for tracking
 * the active page (the page which will ultimately render the response).
 * <p/>
 * Since {@link org.apache.tapestry5.internal.structure.Page} is internal, most user-code should use the {@link
 * org.apache.tapestry5.services.ComponentSource} service instead.
 */
public interface RequestPageCache
{
    /**
     * Gets the page via its page name, in the current locale. The logical page name is resolved to a class name, which
     * is used to obtain the page (from the page pool).  Note that under certain circumstances, a page may have multiple
     * names (even beyond simple case-insensitivity), and RequestPageCache caches correctly.
     *
     * @param pageName the name of the page to retrieve (this is the logical page name, not the fully qualified class
     *                 name)
     * @return a page instance reserved for this request
     * @throws IllegalArgumentException if the name can not be resolved to a page instance
     */
    Page get(String pageName);
}
