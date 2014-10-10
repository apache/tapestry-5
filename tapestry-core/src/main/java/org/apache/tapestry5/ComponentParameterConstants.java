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

package org.apache.tapestry5;

/**
 * Constants for core component parameters defined by {@link org.apache.tapestry5.ioc.annotations.Symbol}
 *
 * @since 5.3
 */
public class ComponentParameterConstants
{
    /**
     * The default number for how many rows to display in a
     * {@link org.apache.tapestry5.corelib.components.Grid} page.
     */
    public static final String GRID_ROWS_PER_PAGE = "tapestry.components.grid_rows_per_page";

    /**
     * The default position of the {@link org.apache.tapestry5.corelib.components.Grid}
     * pager.
     */
    public static final String GRID_PAGER_POSITION = "tapestry.components.grid_pager_position";

    /**
     * The default name of the {@link org.apache.tapestry5.Block} used to display an empty
     * {@link org.apache.tapestry5.corelib.components.Grid}.
     */
    public static final String GRID_EMPTY_BLOCK = "tapestry.components.grid_empty_block";


    /**
     * The default name of the CSS class for the &lt;table&gt; element of the
     * {@link org.apache.tapestry5.corelib.components.Grid} component.
     */
    public static final String GRID_TABLE_CSS_CLASS = "tapestry.components.grid_table_css_class";

    /**
     * The default number of page range to display in the
     * {@link org.apache.tapestry5.corelib.components.GridPager} before and after the current page.
     */
    public static final String GRIDPAGER_PAGE_RANGE = "tapestry.components.gridpager_page_range";

    /**
     * The default {@link org.apache.tapestry5.Asset} to display in
     * {@link org.apache.tapestry5.corelib.components.GridColumns} header for ascending sort action.
     */
    public static final String GRIDCOLUMNS_ASCENDING_ASSET = "tapestry.components.gridcolumns_ascending_asset";

    /**
     * The default {@link org.apache.tapestry5.Asset} to display in
     * {@link org.apache.tapestry5.corelib.components.GridColumns} header for descending sort action.
     */
    public static final String GRIDCOLUMNS_DESCENDING_ASSET = "tapestry.components.gridcolumns_descending_asset";

    /**
     * The default {@link org.apache.tapestry5.Asset} to display in
     * {@link org.apache.tapestry5.corelib.components.GridColumns} header for identifying a sortable column.
     */
    public static final String GRIDCOLUMNS_SORTABLE_ASSET = "tapestry.components.gridcolumns_sortable_asset";

    /**
     * The default position where to insert content into {@link org.apache.tapestry5.corelib.components.Form}.
     * Defaults to "above".
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static final String FORMINJECTOR_INSERT_POSITION = "tapestry.components.forminjector_insert_position";

    /**
     * The default name for a JS function to use to show the injected content by the (removed in 5.4) FormInjector
     * component.
     *
     * Defaults to "highlight".
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static final String FORMINJECTOR_SHOW_FUNCTION = "tapestry.components.forminjector_show_function";

    /**
     * The default size of rows to display in a {@link org.apache.tapestry5.corelib.components.Palette}
     * component. Defaults to 10.
     */
    public static final String PALETTE_ROWS_SIZE = "tapestry.components.palette_rows_size";

    /**
     * The default for whether components that use a SelectModel (e.g.
     * {@link org.apache.tapestry5.corelib.components.Select}) enforce
     * that the submitted value is one of the values in the SelectModel
     * 
     * @since 5.4
     */
    public static final String VALIDATE_WITH_MODEL = "tapestry.components.validate_with_model";

    /**
     * The default name of a JS function attached to Tapestry.ElementEffect object to use for the initial
     * visualization of a {@link org.apache.tapestry5.corelib.components.Zone}.
     * Defaults to "show"
     *
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static final String ZONE_SHOW_METHOD = "tapestry.components.zone_show_method";

    /**
     * The default name of a JS function attached to Tapestry.ElementEffect object to point out an
     * update on a {@link org.apache.tapestry5.corelib.components.Zone}.
     * Defaults to "highlight"
     *
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static final String ZONE_UPDATE_METHOD = "tapestry.components.zone_update_method";
}
