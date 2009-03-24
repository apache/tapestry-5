// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Link;

/**
 * A service that allows other services to create page render links (which are otherwise created by components, via
 * {@link org.apache.tapestry5.ComponentResources#createPageLink(String, boolean, Object[])}).
 *
 * @since 5.1.0.2
 */
public interface PageRenderLinkSource
{
    /**
     * Creates a page render link using the page's normal passivation context (if it has one).
     *
     * @param pageName name of page to create link to
     * @return render link for the page
     */
    Link createPageRenderLink(String pageName);

    /**
     * Creates a page render link using an override of the page's passivation context (possibly an empty one).
     *
     * @param pageName name of page to create link to
     * @param context  zero or more values to encode as the passiviation context
     * @return render link for the page
     */
    Link createPageRenderLinkWithContext(String pageName, Object... context);

    /**
     * Creates a page render link using the page's class to identify the target page, and using the pages normal
     * passivation context (if it has one).
     *
     * @param pageClass
     * @return render link for the page
     */
    Link createPageRenderLink(Class pageClass);

    /**
     * Creates a page render link using the page's class to identify the target page, and using an override of the
     * page's passivation context (possibly an empty one).
     *
     * @param pageClass
     * @param context   zero or more values to encode as the passiviation context
     * @return render link for the page
     */
    Link createPageRenderLinkWithContext(Class pageClass, Object... context);

}
