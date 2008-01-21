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

import org.apache.tapestry.Asset;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.grid.GridConstants;
import org.apache.tapestry.grid.GridModelProvider;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;

import java.util.List;

/**
 * Renders out the column headers for the grid. Eventually, this will include control over column sorting, perhaps even
 * column ordering.
 */
public class GridColumns
{
    /**
     * The object that provides access to bean and data models, which is typically the enclosing Grid component.
     */
    @Parameter(value = "componentResources.container")
    private GridModelProvider _dataProvider;

    /**
     * If true, then the CSS class on each &lt;TH&gt; cell will be omitted, which can reduce the amount of output from
     * the component overall by a considerable amount. Leave this as false, the default, when you are leveraging the CSS
     * to customize the look and feel of particular columns.
     */
    @Parameter
    private boolean _lean;

    /**
     * The column which is currently being sorted. This value is the column's {@link PropertyModel#getId() id}, not its
     * {@link PropertyModel#getPropertyName() name}. This parameter may be null, in which case no column is being used
     * for sorting.
     */
    @Parameter(required = true)
    private String _sortColumnId;

    /**
     * If true, then the sort is ascending (A - Z), if false the descending (Z - A).
     */
    @Parameter(required = true)
    private boolean _sortAscending;

    @SuppressWarnings("unused")
    @Component(parameters = {"event=sort", "disabled=sortDisabled", "context=columnModel.id", "class=sortLinkClass"})
    private EventLink _sort, _sort2;

    @Inject
    @Path("sort-asc.png")
    private Asset _ascendingAsset;

    @Inject
    @Path("sort-desc.png")
    private Asset _descendingAsset;

    @Inject
    @Path("sortable.png")
    private Asset _sortableAsset;

    @Inject
    private Messages _messages;


    private int _columnIndex;

    private int _lastColumnIndex;

    private PropertyModel _columnModel;

    void setupRender()
    {
        _lastColumnIndex = _dataProvider.getDataModel().getPropertyNames().size() - 1;
    }

    public boolean isSortDisabled()
    {
        return !_columnModel.isSortable();
    }

    public String getSortLinkClass()
    {
        if (isActiveSortColumn())
            return _sortAscending ? GridConstants.SORT_ASCENDING_CLASS : GridConstants.SORT_DESCENDING_CLASS;

        return null;
    }

    public String getHeaderClass()
    {
        List<String> classes = CollectionFactory.newList();

        if (!_lean) classes.add(_columnModel.getId() + "-header");

        String sort = getSortLinkClass();

        if (sort != null) classes.add(sort);

        if (_columnIndex == 0) classes.add(GridConstants.FIRST_CLASS);

        if (_columnIndex == _lastColumnIndex) classes.add(GridConstants.LAST_CLASS);

        return TapestryInternalUtils.toClassAttributeValue(classes);
    }

    public boolean isActiveSortColumn()
    {
        return _columnModel.getId().equals(_sortColumnId);
    }

    void onSort(String columnId)
    {
        if (columnId.equals(_sortColumnId))
        {
            _sortAscending = !_sortAscending;
        }
        else
        {
            _sortColumnId = columnId;
            _sortAscending = true;
        }
    }

    public Asset getIcon()
    {
        if (isActiveSortColumn()) return _sortAscending ? _ascendingAsset : _descendingAsset;

        return _sortableAsset;
    }

    public String getIconLabel()
    {
        String key = isActiveSortColumn() ? (_sortAscending ? "ascending" : "descending") : "sortable";

        return _messages.get(key);
    }

    public List<String> getColumnNames()
    {
        return _dataProvider.getDataModel().getPropertyNames();
    }

    public PropertyModel getColumnModel()
    {
        return _columnModel;
    }

    public void setColumnName(String columnName)
    {
        _columnModel = _dataProvider.getDataModel().get(columnName);
    }

    /**
     * Set by the Loop component.
     */
    public void setColumnIndex(int columnIndex)
    {
        _columnIndex = columnIndex;
    }

    public int getColumnIndex()
    {
        return _columnIndex;
    }
}
