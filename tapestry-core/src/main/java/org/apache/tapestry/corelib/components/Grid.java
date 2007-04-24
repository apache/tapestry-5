// Copyright 2007 The Apache Software Foundation
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
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.corelib.data.GridPagerPosition;
import org.apache.tapestry.grid.GridDataSource;
import org.apache.tapestry.grid.GridModelProvider;
import org.apache.tapestry.internal.bindings.AbstractBinding;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.services.BeanModelSource;

/**
 * A grid presents tabular data. It is a composite component, created in terms of several
 * sub-components. The sub-components are statically wired to the Grid, as it provides access to the
 * data and other models that they need.
 * 
 * @see BeanModel
 * @see BeanModelSource
 */
@SupportsInformalParameters
public class Grid implements GridModelProvider
{
    /**
     * The source of data for the Grid to display. This will usually be a List or array but can also
     * be an explicit {@link GridDataSource}. For Lists and Arrays, a GridDataSource is created
     * automatically as a wrapper around the underlying List.
     */
    @Parameter(required = true)
    private Object _source;

    /**
     * The number of rows of data displayed on each page. If there are more rows than will fit, the
     * Grid will divide up the rows into "pages" and (normally) provide a pager to allow the user to
     * navigate within the overall result set.
     */
    @Parameter("25")
    private int _rowsPerPage;

    /**
     * Defines where the pager (used to navigate within the "pages" of results) should be displayed:
     * "top", "bottom", "both" or "none".
     */
    @Parameter(value = "bottom", defaultPrefix = "literal")
    private GridPagerPosition _pagerPosition;

    @Persist
    private int _currentPage = 1;

    @Persist
    private String _sortColumnId;

    @Persist
    private boolean _sortAscending = true;

    /**
     * Used to store the current object being rendered (for the current row). This is used when
     * parameter blocks are provided to override the default cell renderer for a particular column
     * ... the components within the block can use the property bound to the row parameter to know
     * what they should render.
     */
    @Parameter
    private Object _row;

    /**
     * The model used to identify the properties to be presented and the order of presentation. The
     * model may be omitted, in which case a default model is generated from the first object in the
     * data source (this implies that the objects provided by the source are uniform). The model may
     * be explicitly specified to override the default behavior, say to reorder or rename columns or
     * add additional columns.
     */
    @Parameter
    private BeanModel _model;

    /**
     * A Block to render instead of the table (and pager, etc.) when the source is empty. The
     * default is simply the text "There is no data to display". This parameter is used to customize
     * that message, possibly including components to allow the user to create new objects.
     */
    @Parameter(value = "block:empty")
    private Block _empty;

    @Inject
    private ComponentResources _resources;

    @Inject
    private BeanModelSource _modelSource;

    @Inject
    private TypeCoercer _typeCoercer;

    // Transformed version of the source parameter.

    private GridDataSource _dataSource;

    /**
     * The CSS class for the tr element for each data row. This can be used to highlight particular
     * rows, or cycle between CSS values (for the "zebra effect"). If null or not bound, then no
     * particular CSS class value is used.
     */
    @Parameter(cache = false)
    private String _rowClass;

    @SuppressWarnings("unused")
    @Component(parameters =
    { "sortColumnId=sortColumnId", "sortAscending=sortAscending" })
    private GridColumns _columns;

    @SuppressWarnings("unused")
    @Component(parameters =
    { "rowClass=rowClass", "rowsPerPage=rowsPerPage", "currentPage=currentPage", "row=row" })
    private GridRows _rows;

    @Component(parameters =
    { "source=dataSource", "rowsPerPage=rowsPerPage", "currentPage=currentPage" })
    private GridPager _pager;

    @SuppressWarnings("unused")
    @Component(parameters = "to=pagerTop")
    private Delegate _pagerTop;

    @SuppressWarnings("unused")
    @Component(parameters = "to=pagerBottom")
    private Delegate _pagerBottom;

    Binding defaultModel()
    {
        final ComponentResources containerResources = _resources.getContainerResources();

        return new AbstractBinding()
        {

            public Object get()
            {
                // Get the default row type from the data source

                Class rowType = _dataSource.getRowType();

                if (rowType == null)
                    throw new RuntimeException("xxx -- no source to determine list type from");

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

    Object setupRender()
    {
        _dataSource = _typeCoercer.coerce(_source, GridDataSource.class);

        // If there's no rows, display the empty block placeholder.

        int availableRows = _dataSource.getAvailableRows();

        if (availableRows == 0) return _empty;

        PropertyModel sortModel = null;

        if (_sortColumnId != null)
        {
            for (String name : _model.getPropertyNames())
            {
                PropertyModel propertyModel = _model.get(name);

                if (propertyModel.getId().equals(_sortColumnId))
                {
                    sortModel = propertyModel;
                    break;
                }
            }
        }

        int startIndex = (_currentPage - 1) * _rowsPerPage;
        int endIndex = Math.min(startIndex + _rowsPerPage - 1, availableRows - 1);

        _dataSource.prepare(startIndex, endIndex, sortModel, _sortAscending);

        return null;
    }

    Object beginRender()
    {
        // Skip rendering of component (template, body, etc.) when there's nothing to display.
        // The empty placeholder will already have rendered.

        if (_dataSource.getAvailableRows() == 0) return false;

        return null;
    }

    public BeanModel getDataModel()
    {
        return _model;
    }

    public GridDataSource getDataSource()
    {
        return _dataSource;
    }

    public String getRowClass()
    {
        return _rowClass;
    }

    public int getCurrentPage()
    {
        return _currentPage;
    }

    public void setCurrentPage(int currentPage)
    {
        _currentPage = currentPage;
    }

    public Object getRow()
    {
        return _row;
    }

    public void setRow(Object row)
    {
        _row = row;
    }

    public int getRowsPerPage()
    {
        return _rowsPerPage;
    }

    public void setRowsPerPage(int rowsPerPage)
    {
        _rowsPerPage = rowsPerPage;
    }

    public boolean isSortAscending()
    {
        return _sortAscending;
    }

    public String getSortColumnId()
    {
        return _sortColumnId;
    }

    public void setSortAscending(boolean sortAscending)
    {
        _sortAscending = sortAscending;
    }

    public void setSortColumnId(String sortColumnId)
    {
        _sortColumnId = sortColumnId;
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
