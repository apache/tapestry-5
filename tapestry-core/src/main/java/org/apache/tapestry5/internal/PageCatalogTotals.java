// Copyright 2011, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.beaneditor.ReorderProperties;

/**
 * @see org.apache.tapestry5.corelib.pages.PageCatalog
 */
@ReorderProperties("definedPages,loadedPages,uniquePageNames,selectors,components")
public class PageCatalogTotals
{
    /**
     * Total number of pages loaded.
     */
    public int loadedPages;

    /**
     * Number of total page names.
     */
    public int definedPages;
    /**
     * Number of unique page names (remember, same page may appear for multiple selectors).
     */
    public int uniquePageNames;
    /**
     * Total number of components.
     */
    public int components;

    /**
     * All selectors represented in the pool, often just 'en'.
     */
    public String selectors;
}
