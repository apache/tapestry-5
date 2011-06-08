// Copyright 2006, 2007, 2008, 2009, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResourcesCommon;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;

/**
 * Instantiates a fully loaded, configured instance of a Tapestry page. This is a recursive process, since part of
 * loading a page is to load the page elements for the page, many of which are components. Further, in some cases, the
 * full component tree is not identified until after each component's template is loaded. Pages are instantiated once
 * for each {@link ComponentResourceSelector} (prior to 5.3, once for each locale; prior to 5.1, for locale and pooled).
 * 
 * @see RequestPageCache
 */
public interface PageLoader
{
    /**
     * Loads the page for the indicated selector.
     * 
     * @param pageName
     *            the <em>canonicalized</em> logical name of the page, which will be made available via
     *            {@link Page#getName()} and {@link ComponentResourcesCommon#getPageName()} (for any component within
     *            the page).
     * @param selector
     *            Encapsulates the locale and other information used to select the component's
     *            template and message catalog resources.
     */
    Page loadPage(String pageName, ComponentResourceSelector selector);
}
