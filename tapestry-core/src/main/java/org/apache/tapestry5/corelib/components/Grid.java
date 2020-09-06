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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beanmodel.BeanModel;
import org.apache.tapestry5.beanmodel.BeanModelUtils;
import org.apache.tapestry5.beanmodel.PropertyModel;
import org.apache.tapestry5.beanmodel.services.BeanModelSource;
import org.apache.tapestry5.corelib.data.GridPagerPosition;
import org.apache.tapestry5.grid.*;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.bindings.AbstractBinding;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * A grid presents tabular data. It is a composite component, created in terms of several sub-components. The
 * sub-components are statically wired to the Grid, as it provides access to the data and other models that they need.
 *
 * A Grid may operate inside a {@link org.apache.tapestry5.corelib.components.Form}. By overriding the cell renderers of
 * properties, the default output-only behavior can be changed to produce a complex form with individual control for
 * editing properties of each row. There is a big caveat here: if the order of rows provided by
 * the {@link org.apache.tapestry5.grid.GridDataSource} changes between render and form submission, then there's the
 * possibility that data will be applied to the wrong server-side objects.
 *
 * For this reason, when using Grid and Form together, you should generally
 * provide the Grid with a {@link org.apache.tapestry5.ValueEncoder} (via the
 * encoder parameter), or use an entity type for the "row" parameter for which
 * Tapestry can provide a ValueEncoder automatically. This will allow Tapestry
 * to use a unique ID for each row that doesn't change when rows are reordered.
 *
 * @tapestrydoc
 * @see org.apache.tapestry5.beanmodel.BeanModel
 * @see org.apache.tapestry5.beanmodel.services.BeanModelSource
 * @see org.apache.tapestry5.grid.GridDataSource
 * @see BeanEditForm
 * @see BeanDisplay
 * @see Loop
 */
@SupportsInformalParameters
public class Grid implements GridModel, ClientElement
{
    /**
     * The source of data for the Grid to display. This will usually be a List or array but can also be an explicit
     * {@link GridDataSource}. For Lists and object arrays, a GridDataSource is created automatically as a wrapper
     * around the underlying List.
     */
    @Parameter(required = true, autoconnect = true)
    private GridDataSource source;

    /**
     * A wrapper around the provided GridDataSource that caches access to the availableRows property. This is the source
     * provided to sub-components.
     */
    private GridDataSource cachingSource;

    /**
     * The number of rows of data displayed on each page. If there are more rows than will fit, the Grid will divide up
     * the rows into "pages" and (normally) provide a pager to allow the user to navigate within the overall result
     * set.
     */
    @Parameter(BindingConstants.SYMBOL + ":" + ComponentParameterConstants.GRID_ROWS_PER_PAGE)
    private int rowsPerPage;

    /**
     * Defines where the pager (used to navigate within the "pages" of results) should be displayed: "top", "bottom",
     * "both" or "none".
     */
    @Parameter(value = BindingConstants.SYMBOL + ":" + ComponentParameterConstants.GRID_PAGER_POSITION,
            defaultPrefix = BindingConstants.LITERAL)
    private GridPagerPosition pagerPosition;

    /**
     * Used to store the current object being rendered (for the current row). This is used when parameter blocks are
     * provided to override the default cell renderer for a particular column ... the components within the block can
     * use the property bound to the row parameter to know what they should render.
     */
    @Parameter(principal = true)
    private Object row;

    /**
     * Optional output parameter used to identify the index of the column being rendered.
     */
    @Parameter
    private int columnIndex;

    /**
     * The model used to identify the properties to be presented and the order of presentation. The model may be
     * omitted, in which case a default model is generated from the first object in the data source (this implies that
     * the objects provided by the source are uniform). The model may be explicitly specified to override the default
     * behavior, say to reorder or rename columns or add additional columns. The add, include,
     * exclude and reorder
     * parameters are <em>only</em> applied to a default model, not an explicitly provided one.
     */
    @Parameter
    private BeanModel model;

    /**
     * The model parameter after modification due to the add, include, exclude and reorder parameters.
     */
    private BeanModel dataModel;

    /**
     * The model used to handle sorting of the Grid. This is generally not specified, and the built-in model supports
     * only single column sorting. The sort constraints (the column that is sorted, and ascending vs. descending) is
     * stored as persistent fields of the Grid component.
     */
    @Parameter
    private GridSortModel sortModel;

    /**
     * A comma-separated list of property names to be added to the {@link org.apache.tapestry5.beanmodel.BeanModel}.
     * Cells for added columns will be blank unless a cell override is provided. This parameter is only used
     * when a default model is created automatically.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String add;

    /**
     * A comma-separated list of property names to be retained from the
     * {@link org.apache.tapestry5.beanmodel.BeanModel}.
     * Only these properties will be retained, and the properties will also be reordered. The names are
     * case-insensitive. This parameter is only used
     * when a default model is created automatically.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String include;

    /**
     * A comma-separated list of property names to be removed from the {@link org.apache.tapestry5.beanmodel.BeanModel}
     * .
     * The names are case-insensitive. This parameter is only used
     * when a default model is created automatically.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String exclude;

    /**
     * A comma-separated list of property names indicating the order in which the properties should be presented. The
     * names are case insensitive. Any properties not indicated in the list will be appended to the end of the display
     * order. This parameter is only used
     * when a default model is created automatically.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String reorder;

    /**
     * A Block to render instead of the table (and pager, etc.) when the source is empty. The default is simply the text
     * "There is no data to display". This parameter is used to customize that message, possibly including components to
     * allow the user to create new objects.
     */
    //@Parameter(value = BindingConstants.SYMBOL + ":" + ComponentParameterConstants.GRID_EMPTY_BLOCK,
    @Parameter(value = "block:empty",
            defaultPrefix = BindingConstants.LITERAL)
    @Property(write = false)
    private Block empty;

    /**
     * CSS class for the &lt;table&gt; element. In addition, informal parameters to the Grid are rendered in the table
     * element.
     */
    @Parameter(name = "class", defaultPrefix = BindingConstants.LITERAL,
            value = BindingConstants.SYMBOL + ":" + ComponentParameterConstants.GRID_TABLE_CSS_CLASS)
    @Property(write = false)
    private String tableClass;

    /**
     * If true, then the Grid will be wrapped in an element that acts like a
     * {@link org.apache.tapestry5.corelib.components.Zone}; all the paging and sorting links will refresh the zone,
     * repainting the entire grid within it, but leaving the rest of the page (outside the zone) unchanged.
     */
    @Parameter
    private boolean inPlace;

    /**
     * If true, then the Grid will also render a table element complete with headers if the data source is empty.
     * If set to true, a model parameter will have to be specified. A default model for a specific class can be
     * created using {@link BeanModelSource#createDisplayModel(Class, org.apache.tapestry5.commons.Messages)}.
     */
    @Parameter
    private boolean renderTableIfEmpty = false;


    /**
     * The name of the pseudo-zone that encloses the Grid. Starting in 5.4, this is always either
     * null or "^" and is not really used the way it was in 5.3; instead it triggers the addition
     * of a {@code data-inplace-grid-links} attribute in a div surrounding any links related to
     * sorting or pagination. The rest is sorted out on the client. See module {@code t5/core/zone}.
     */
    @Property(write = false)
    private String zone;

    private boolean didRenderZoneDiv;


    /**
     * The pagination model for the Grid, which encapsulates current page, sort column id,
     * and sort ascending/descending. If not bound, a persistent property of the Grid is used.
     * When rendering the Grid in a loop, this should be bound in some way to keep successive instances
     * of the Grid configured individually.
     *
     * @since 5.4
     */
    @Parameter(value = "defaultPaginationModel")
    private GridPaginationModel paginationModel;

    @Persist
    private GridPaginationModel defaultPaginationModel;

    @Inject
    private ComponentResources resources;

    @Inject
    private BeanModelSource modelSource;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    @Component(parameters =
            {"index=inherit:columnIndex", "lean=inherit:lean", "overrides=overrides", "zone=zone"})
    private GridColumns columns;

    @Component(parameters =
            {"columnIndex=inherit:columnIndex", "rowsPerPage=rowsPerPage", "currentPage=currentPage", "row=row",
                    "overrides=overrides"}, publishParameters = "rowIndex,rowClass,volatile,encoder,lean")
    private GridRows rows;

    @Component(parameters =
            {"source=dataSource", "rowsPerPage=rowsPerPage", "currentPage=currentPage", "zone=zone"})
    private GridPager pager;

    @Component(parameters = "to=pagerTop")
    private Delegate pagerTop;

    @Component(parameters = "to=pagerBottom")
    private Delegate pagerBottom;

    @Component(parameters = "class=tableClass", inheritInformalParameters = true)
    private Any table;

    @Environmental(false)
    private FormSupport formSupport;

    /**
     * Defines where block and label overrides are obtained from. By default, the Grid component provides block
     * overrides (from its block parameters).
     */
    @Parameter(value = "this", allowNull = false)
    @Property(write = false)
    private PropertyOverrides overrides;

    /**
     * Set up via the traditional or Ajax component event request handler
     */
    @Environmental
    private ComponentEventResultProcessor componentEventResultProcessor;

    @Inject
    private ComponentDefaultProvider defaultsProvider;

    ValueEncoder defaultEncoder()
    {
        return defaultsProvider.defaultValueEncoder("row", resources);
    }

    /**
     * A version of GridDataSource that caches the availableRows and empty properties. This addresses TAPESTRY-2245.
     */
    static class CachingDataSource implements GridDataSource
    {
        private final GridDataSource delegate;

        private boolean availableRowsCached;

        private int availableRows;

        private boolean emptyCached;

        private boolean empty;

        CachingDataSource(GridDataSource delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public boolean isEmpty()
        {
            if (!emptyCached)
            {
                empty = delegate.isEmpty();
                emptyCached = true;
                if (empty)
                {
                    availableRows = 0;
                    availableRowsCached = true;
                }
            }

            return empty;
        }

        @Override
        public int getAvailableRows(int limit)
        {
            if (!availableRowsCached)
            {
                int result = delegate.getAvailableRows(limit);
                if (result == 0)
                {
                    empty = true;
                    emptyCached = true;
                } else {
                    empty = false;
                    emptyCached = true;
                }
                if (result < limit) {
                    availableRows = result;
                    availableRowsCached = true;
                }
                return result;
            } else {
              return Math.min(availableRows, limit);
            }
        }

        public int getAvailableRows()
        {
            if (!availableRowsCached)
            {
                availableRows = delegate.getAvailableRows();
                availableRowsCached = true;
                if (availableRows == 0)
                {
                    empty = true;
                    emptyCached = true;
                } else {
                  empty = false;
                  emptyCached = true;
              }
            }

            return availableRows;
        }

        public void prepare(int startIndex, int endIndex, List<SortConstraint> sortConstraints)
        {
            delegate.prepare(startIndex, endIndex, sortConstraints);
        }

        public Object getRowValue(int index)
        {
            return delegate.getRowValue(index);
        }

        public Class getRowType()
        {
            return delegate.getRowType();
        }
    }

    /**
     * Default implementation that only allows a single column to be the sort column, and stores the sort information as
     * persistent fields of the Grid component.
     */
    class DefaultGridSortModel implements GridSortModel
    {
        public ColumnSort getColumnSort(String columnId)
        {
            if (paginationModel == null || !TapestryInternalUtils.isEqual(columnId, paginationModel.getSortColumnId()))
            {
                return ColumnSort.UNSORTED;
            }

            return getColumnSort();
        }

        private ColumnSort getColumnSort()
        {
            return getSortAscending() ? ColumnSort.ASCENDING : ColumnSort.DESCENDING;
        }

        public void updateSort(String columnId)
        {
            assert InternalUtils.isNonBlank(columnId);

            if (columnId.equals(paginationModel.getSortColumnId()))
            {
                setSortAscending(!getSortAscending());
                return;
            }

            paginationModel.setSortColumnId(columnId);
            setSortAscending(true);
        }

        public List<SortConstraint> getSortConstraints()
        {
            // In a few limited cases we may not have yet hit the SetupRender phase, and the model may be null.
            if (paginationModel == null || paginationModel.getSortColumnId() == null)
            {
                return Collections.emptyList();
            }

            PropertyModel sortModel = getDataModel().getById(paginationModel.getSortColumnId());

            SortConstraint constraint = new SortConstraint(sortModel, getColumnSort());

            return Collections.singletonList(constraint);
        }

        public void clear()
        {
            paginationModel.setSortColumnId(null);
            paginationModel.setSortAscending(null);
        }
    }

    GridSortModel defaultSortModel()
    {
        return new DefaultGridSortModel();
    }

    /**
     * Returns a {@link org.apache.tapestry5.Binding} instance that attempts to identify the model from the source
     * parameter (via {@link org.apache.tapestry5.grid.GridDataSource#getRowType()}. Subclasses may override to provide
     * a different mechanism. The returning binding is variant (not invariant).
     *
     * @see BeanModelSource#createDisplayModel(Class, org.apache.tapestry5.commons.Messages)
     */
    protected Binding defaultModel()
    {

        return new AbstractBinding()
        {
            public Object get()
            {
                // Get the default row type from the data source

                GridDataSource gridDataSource = source;

                Class rowType = gridDataSource.getRowType();

                if (renderTableIfEmpty || rowType == null)
                    throw new RuntimeException(
                            String.format(
                                    "Unable to determine the bean type for rows from %s. You should bind the model parameter explicitly.",
                                    gridDataSource));

                // Properties do not have to be read/write

                return modelSource.createDisplayModel(rowType, overrides.getOverrideMessages());
            }

            /**
             * Returns false. This may be overkill, but it basically exists because the model is
             * inherently mutable and therefore may contain client-specific state and needs to be
             * discarded at the end of the request. If the model were immutable, then we could leave
             * invariant as true.
             */
            @Override
            public boolean isInvariant()
            {
                return false;
            }
        };
    }

    static final ComponentAction<Grid> SETUP_DATA_SOURCE = new ComponentAction<Grid>()
    {
        private static final long serialVersionUID = 8545187927995722789L;

        public void execute(Grid component)
        {
            component.setupDataSource();
        }

        @Override
        public String toString()
        {
            return "Grid.SetupDataSource";
        }
    };

    Object setupRender()
    {
        if (formSupport != null)
        {
            formSupport.store(this, SETUP_DATA_SOURCE);
        }

        setupDataSource();

        // If there's no rows, display the empty block placeholder.

        return !renderTableIfEmpty && cachingSource.isEmpty() ? empty : null;
    }

    void cleanupRender()
    {
        // if an inPlace Grid is rendered inside a Loop, be sure to generate a new wrapper
        // zone for each iteration (TAP5-2256)
        zone = null;

        // If grid is rendered inside a Loop. be sure to generate a new data model for
        // each iteration (TAP5-2470)
        dataModel = null;
    }

    public GridPaginationModel getDefaultPaginationModel()
    {
        if (defaultPaginationModel == null)
        {
            defaultPaginationModel = new GridPaginationModelImpl();
        }

        return defaultPaginationModel;
    }

    void setupDataSource()
    {
        // TAP5-34: We pass the source into the CachingDataSource now; previously
        // we were accessing source directly, but during submit the value wasn't
        // cached, and therefore access was very inefficient, and sorting was
        // very inconsistent during the processing of the form submission.

        int effectiveCurrentPage = getCurrentPage();

        int numberOfRowsRequiredToShowCurrentPage = 1 + (effectiveCurrentPage - 1) * rowsPerPage;
        int numberOfRowsRequiredToFillCurrentPage = effectiveCurrentPage * rowsPerPage;

        cachingSource = new CachingDataSource(source);
        if (pagerPosition != GridPagerPosition.NONE)
        {
            // We're going to render the pager, so we need to determine the total number of rows anyway.
            // We do that eagerly here so we don't have to perform two count operations; the subsequent
            // ones will return a cached result
            cachingSource.getAvailableRows();
        }
        int availableRowsWithLimit = cachingSource.getAvailableRows(numberOfRowsRequiredToFillCurrentPage);

        if (availableRowsWithLimit == 0)
            return;

        // This captures when the number of rows has decreased, typically due to deletions.

        if (numberOfRowsRequiredToShowCurrentPage > availableRowsWithLimit)
        {
            int maxPage = ((availableRowsWithLimit - 1) / rowsPerPage) + 1;
            effectiveCurrentPage = maxPage;
        }

        int startIndex = (effectiveCurrentPage - 1) * rowsPerPage;

        int endIndex = Math.min(startIndex + rowsPerPage - 1, availableRowsWithLimit - 1);

        cachingSource.prepare(startIndex, endIndex, sortModel.getSortConstraints());
    }

    Object beginRender(MarkupWriter writer)
    {
        // Skip rendering of component (template, body, etc.) when there's nothing to display.
        // The empty placeholder will already have rendered.

        if (cachingSource.isEmpty())
            return !renderTableIfEmpty ? false : null;

        if (inPlace && zone == null)
        {
            javaScriptSupport.require("t5/core/zone");

            writer.element("div", "data-container-type", "zone");

            didRenderZoneDiv = true;

            // Through Tapestry 5.3, we had a specific id for the zone that had to be passed down to the
            // GridPager and etc.  That's no longer necessary, so zone will always be null or "^".  We don't
            // even need any special ids to be allocated!
            zone = "^";
        }

        return null;
    }

    void afterRender(MarkupWriter writer)
    {
        if (didRenderZoneDiv)
        {
            writer.end(); // div
            didRenderZoneDiv = false;
        }
    }

    public BeanModel getDataModel()
    {
        if (dataModel == null)
        {
            dataModel = model;

            BeanModelUtils.modify(dataModel, add, include, exclude, reorder);
        }

        return dataModel;
    }

    public int getNumberOfProperties()
    {
        return getDataModel().getPropertyNames().size();
    }

    public GridDataSource getDataSource()
    {
        return cachingSource;
    }

    public GridSortModel getSortModel()
    {
        return sortModel;
    }

    public Object getPagerTop()
    {
        return pagerPosition.isMatchTop() ? pager : null;
    }

    public Object getPagerBottom()
    {
        return pagerPosition.isMatchBottom() ? pager : null;
    }

    public int getCurrentPage()
    {
        Integer currentPage = paginationModel.getCurrentPage();

        return currentPage == null ? 1 : currentPage;
    }

    public void setCurrentPage(int currentPage)
    {
        paginationModel.setCurrentPage(currentPage);
    }

    private boolean getSortAscending()
    {
        Boolean sortAscending = paginationModel.getSortAscending();

        return sortAscending != null && sortAscending.booleanValue();
    }

    private void setSortAscending(boolean sortAscending)
    {
        paginationModel.setSortAscending(sortAscending);
    }

    public int getRowsPerPage()
    {
        return rowsPerPage;
    }

    public Object getRow()
    {
        return row;
    }

    public void setRow(Object row)
    {
        this.row = row;
    }

    /**
     * Resets the Grid to inital settings; this sets the current page to one, and
     * {@linkplain org.apache.tapestry5.grid.GridSortModel#clear() clears the sort model}.
     */
    public void reset()
    {
        sortModel.clear();
        setCurrentPage(1);
    }

    /**
     * Event handler for inplaceupdate event triggered from nested components when an Ajax update occurs. The event
     * context will carry the zone, which is recorded here, to allow the Grid and its sub-components to properly
     * re-render themselves. Invokes
     * {@link org.apache.tapestry5.services.ComponentEventResultProcessor#processResultValue(Object)} passing this (the
     * Grid component) as the content provider for the update.
     */
    void onInPlaceUpdate() throws IOException
    {
        this.zone = "^";

        componentEventResultProcessor.processResultValue(this);
    }

    public String getClientId()
    {
        return table.getClientId();
    }
}
