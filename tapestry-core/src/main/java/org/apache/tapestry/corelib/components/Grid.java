// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.Binding;
import org.apache.tapestry.Block;
import org.apache.tapestry.ComponentAction;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.annotations.*;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.corelib.data.GridPagerPosition;
import org.apache.tapestry.grid.*;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.internal.beaneditor.BeanModelUtils;
import org.apache.tapestry.internal.bindings.AbstractBinding;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.FormSupport;

import java.util.Collections;
import java.util.List;

/**
 * A grid presents tabular data. It is a composite component, created in terms of several sub-components. The
 * sub-components are statically wired to the Grid, as it provides access to the data and other models that they need.
 * <p/>
 * A Grid may operate inside a {@link org.apache.tapestry.corelib.components.Form}. By overriding the cell renderers of
 * properties, the default output-only behavior can be changed to produce a complex form with individual control for
 * editing properties of each row. This is currently workable but less than ideal -- if the order of rows provided by
 * the {@link GridDataSource} changes between render and form submission, then there's the possibility that data will be
 * applied to the wrong server-side objects.
 *
 * @see org.apache.tapestry.beaneditor.BeanModel
 * @see org.apache.tapestry.services.BeanModelSource
 * @see org.apache.tapestry.grid.GridDataSource
 */
@SupportsInformalParameters
public class Grid implements GridModel
{
    /**
     * The source of data for the Grid to display. This will usually be a List or array but can also be an explicit
     * {@link GridDataSource}. For Lists and object arrays, a GridDataSource is created automatically as a wrapper
     * around the underlying List.
     */
    @Parameter(required = true)
    private GridDataSource _source;

    /**
     * The number of rows of data displayed on each page. If there are more rows than will fit, the Grid will divide up
     * the rows into "pages" and (normally) provide a pager to allow the user to navigate within the overall result
     * set.
     */
    @Parameter("25")
    @GenerateAccessors
    private int _rowsPerPage;

    /**
     * Defines where the pager (used to navigate within the "pages" of results) should be displayed: "top", "bottom",
     * "both" or "none".
     */
    @Parameter(value = "top", defaultPrefix = "literal")
    private GridPagerPosition _pagerPosition;

    /**
     * Used to store the current object being rendered (for the current row). This is used when parameter blocks are
     * provided to override the default cell renderer for a particular column ... the components within the block can
     * use the property bound to the row parameter to know what they should render.
     */
    @Parameter
    @GenerateAccessors
    private Object _row;

    /**
     * The model used to identify the properties to be presented and the order of presentation. The model may be
     * omitted, in which case a default model is generated from the first object in the data source (this implies that
     * the objects provided by the source are uniform). The model may be explicitly specified to override the default
     * behavior, say to reorder or rename columns or add additional columns.
     */
    @Parameter
    private BeanModel _model;

    /**
     * The model used to handle sorting of the Grid. This is generally not specified, and the built-in model supports
     * only single column sorting. The sort constraints (the column that is sorted, and ascending vs. descending) is
     * stored as persistent fields of the Grid component.
     */
    @Parameter
    private GridSortModel _sortModel;


    /**
     * A comma-separated list of property names to be removed from the {@link BeanModel}. The names are
     * case-insensitive.
     */
    @Parameter(defaultPrefix = "literal")
    private String _remove;

    /**
     * A comma-separated list of property names indicating the order in which the properties should be presented. The
     * names are case insensitive. Any properties not indicated in the list will be appended to the end of the display
     * order.
     */
    @Parameter(defaultPrefix = "literal")
    private String _reorder;

    /**
     * A Block to render instead of the table (and pager, etc.) when the source is empty. The default is simply the text
     * "There is no data to display". This parameter is used to customize that message, possibly including components to
     * allow the user to create new objects.
     */
    @Parameter(value = "block:empty")
    private Block _empty;


    /**
     * If true, then the CSS class on each &lt;TD&gt; and &lt;TH&gt; cell will be omitted, which can reduce the amount
     * of output from the component overall by a considerable amount. Leave this as false, the default, when you are
     * leveraging the CSS to customize the look and feel of particular columns.
     */
    @Parameter
    private boolean _lean;

    /**
     * If true and the Loop is enclosed by a Form, then the normal state persisting logic is turned off. Defaults to
     * false, enabling state persisting within Forms. If a Grid is present for some reason within a Form, but does not
     * contain any form control components (such as {@link TextField}), then binding volatile to false will reduce the
     * amount of client-side state that must be persisted.
     */
    @Parameter
    private boolean _volatile;

    /**
     * The CSS class for the tr element for each data row. This can be used to highlight particular rows, or cycle
     * between CSS values (for the "zebra effect"). If null or not bound, then no particular CSS class value is used.
     */
    @Parameter(cache = false)
    @GenerateAccessors(write = false)
    private String _rowClass;

    @Persist
    @GenerateAccessors
    private int _currentPage = 1;

    @Persist
    private String _sortColumnId;

    @Persist
    private boolean _sortAscending = true;

    @Inject
    private ComponentResources _resources;

    @Inject
    private BeanModelSource _modelSource;


    @SuppressWarnings("unused")
    @Component(
            parameters = { "lean=inherit:lean", "overrides=componentResources" })
    private GridColumns _columns;

    @SuppressWarnings("unused")
    @Component(
            parameters = { "rowClass=rowClass", "rowsPerPage=rowsPerPage", "currentPage=currentPage", "row=row", "volatile=inherit:volatile", "lean=inherit:lean" })
    private GridRows _rows;

    @Component(parameters = { "source=dataSource", "rowsPerPage=rowsPerPage", "currentPage=currentPage" })
    private GridPager _pager;

    @SuppressWarnings("unused")
    @Component(parameters = "to=pagerTop")
    private Delegate _pagerTop;

    @SuppressWarnings("unused")
    @Component(parameters = "to=pagerBottom")
    private Delegate _pagerBottom;


    @Environmental(false)
    private FormSupport _formSupport;

    /**
     * Default implementation only allows a single column to be the sort column, and stores the sort information as
     * persistent fields of the Grid component.
     */
    class DefaultGridSortModel implements GridSortModel
    {
        public ColumnSort getColumnSort(String columnId)
        {
            if (!TapestryInternalUtils.isEqual(columnId, _sortColumnId))
                return ColumnSort.UNSORTED;

            return getColumnSort();
        }

        private ColumnSort getColumnSort()
        {
            return _sortAscending ? ColumnSort.ASCENDING : ColumnSort.DESCENDING;
        }


        public void updateSort(String columnId)
        {
            Defense.notBlank(columnId, "columnId");

            if (columnId.equals(_sortColumnId))
            {
                _sortAscending = !_sortAscending;
                return;
            }

            _sortColumnId = columnId;
            _sortAscending = true;
        }

        public List<SortConstraint> getSortContraints()
        {
            if (_sortColumnId == null)
                return Collections.emptyList();

            PropertyModel sortModel = _model.getById(_sortColumnId);

            SortConstraint constraint = new SortConstraint(sortModel, getColumnSort());

            return Collections.singletonList(constraint);
        }

        public void clear()
        {
            _sortColumnId = null;
        }
    }

    GridSortModel defaultSortModel()
    {
        return new DefaultGridSortModel();
    }

    Binding defaultModel()
    {
        final ComponentResources containerResources = _resources.getContainerResources();

        return new AbstractBinding()
        {

            public Object get()
            {
                // Get the default row type from the data source

                Class rowType = _source.getRowType();

                if (rowType == null) throw new RuntimeException(
                        "Unable to determine the bean type for rows from the GridDataSource. You should bind the model parameter explicitly.");

                // Properties do not have to be read/write

                return _modelSource.create(rowType, false, containerResources);
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
    };

    Object setupRender()
    {
        if (!_volatile && _formSupport != null) _formSupport.store(this, SETUP_DATA_SOURCE);

        setupDataSource();

        return _source.getAvailableRows() == 0 ? _empty : null;
    }

    void setupDataSource()
    {
        if (_remove != null) BeanModelUtils.remove(_model, _remove);

        if (_reorder != null) BeanModelUtils.reorder(_model, _reorder);

        // If there's no rows, display the empty block placeholder.

        int availableRows = _source.getAvailableRows();

        if (availableRows == 0) return;

        int maxPage = ((availableRows - 1) / _rowsPerPage) + 1;

        // This captures when the number of rows has decreased, typically due to deletions.

        if (_currentPage > maxPage)
            _currentPage = maxPage;

        int startIndex = (_currentPage - 1) * _rowsPerPage;

        int endIndex = Math.min(startIndex + _rowsPerPage - 1, availableRows - 1);

        _source.prepare(startIndex, endIndex, _sortModel.getSortContraints());

    }

    Object beginRender()
    {
        // Skip rendering of component (template, body, etc.) when there's nothing to display.
        // The empty placeholder will already have rendered.

        if (_source.getAvailableRows() == 0) return false;

        return null;
    }

    public BeanModel getDataModel()
    {
        return _model;
    }

    public GridDataSource getDataSource()
    {
        return _source;
    }

    public GridSortModel getSortModel()
    {
        return _sortModel;
    }

    public Object getPagerTop()
    {
        return _pagerPosition.isMatchTop() ? _pager : null;
    }

    public Object getPagerBottom()
    {
        return _pagerPosition.isMatchBottom() ? _pager : null;
    }


}
