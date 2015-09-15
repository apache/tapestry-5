// Copyright 2008, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.grid;

public class GridConstants
{
    /**
     * CSS class for the first column or the first row. May be applied to a &lt;th&gt; (in the &lt;thead&gt;) or a
     * &lt;tr&gt; (in the &lt;tbody&gt;).
     * 
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static final String FIRST_CLASS = "t-first";

    /**
     * CSS class for the last column or the last row. May be applied to a &lt;th&gt; (in the &lt;thead&gt;) or a
     * &lt;tr&gt; (in the &lt;tbody&gt;).
     * 
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static final String LAST_CLASS = "t-last";

    /**
     * Marks the column that is currently sorted for sort ascending.  May be applied to a &lt;th&gt; (in the
     * &lt;thead&gt; or a &lt;td&gt; in the &lt;tbody&gt;).
     * 
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static final String SORT_ASCENDING_CLASS = "t-sort-column-ascending";

    /**
     * Marks the column that is currently sorted for sort descending.  May be applied to a &lt;th&gt; (in the
     * &lt;thead&gt; or a &lt;td&gt; in the &lt;tbody&gt;).
     * 
     * @deprecated Deprecated in 5.4 with no replacement.
     */

    public static final String SORT_DESCENDING_CLASS = "t-sort-column-descending";

    /**
     * Number of rows to diplay within each {@link org.apache.tapestry5.corelib.components.Grid} page.
     */
    public static final int ROWS_PER_PAGE = 25;

    /**
     * CSS class for the &lt;table&gt; element.   Change in 5.4 from "t-data-grid" due to the introduction
     * of Twitter Bootstrap.
     */
    public static final String TABLE_CLASS = "table table-hover table-bordered table-striped";

    /**
     * Default pager position
     */
    public static final String PAGER_POSITION = "top";

    /**
     * Block name to display in place of an empty {@link org.apache.tapestry5.corelib.components.Grid}
     */
    public static final String EMPTY_BLOCK = "block:empty";

    /**
     * Default number of page to display before and after the current page in the
     * {@link org.apache.tapestry5.corelib.components.GridPager}
     */
    public static final int PAGER_PAGE_RANGE = 5;

    /**
     * Default {@link org.apache.tapestry5.Asset} for ascending columns sort
     * 
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static final String COLUMNS_ASCENDING = "sort-asc.png";

    /**
     * Default {@link org.apache.tapestry5.Asset} for ascending columns sort
     * 
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static final String COLUMNS_DESCENDING = "sort-desc.png";

    /**
     * Default {@link org.apache.tapestry5.Asset} for sortable columns
     * 
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static final String COLUMNS_SORTABLE = "sortable.png";
}
