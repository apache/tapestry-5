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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ComponentResourcesCommon;
import org.apache.tapestry.internal.event.InvalidationEventHub;
import org.apache.tapestry.internal.structure.Page;

import java.util.Locale;

/**
 * Instantiates a fully loaded, configured instance of a Tapestry page. This is a recursive process,
 * since part of loading a page is to load the page elements for the page, many of which are
 * components. Further, in some cases, the full component tree is not identified until after each
 * component's template is loaded. Because this is an expensive process, loaded pages will be used
 * for many requests (on behalf of many different users) and will be pooled between requests.
 *
 * @see PagePool
 * @see RequestPageCache
 */
public interface PageLoader extends InvalidationEventHub
{
    /**
     * Loads the page in the given locale.
     *
     * @param logicalPageName the <em>canonicalized</em> logical name of the page, which will be made
     *                        available via {@link Page#getLogicalName()} and
     *                        {@link ComponentResourcesCommon#getPageName()} (for any component within the
     *                        page).
     * @param locale          the locale to load the page and its components , which will be made available via
     *                        {@link Page#getLocale()} and {@link ComponentResourcesCommon#getLocale()} (for any
     *                        component within the page)
     * @see Page#getLocale()
     */
    Page loadPage(String logicalPageName, Locale locale);
}
