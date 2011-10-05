package org.apache.tapestry5;

/**
 * Constants for core component parameters defined by {@link org.apache.tapestry5.ioc.annotations.Symbol}
 *
 * @since 5.3
 */
public class ParameterConstants
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

}
