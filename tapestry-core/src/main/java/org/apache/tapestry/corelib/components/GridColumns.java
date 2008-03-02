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
import org.apache.tapestry.Block;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.annotations.*;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.grid.ColumnSort;
import org.apache.tapestry.grid.GridConstants;
import org.apache.tapestry.grid.GridModel;
import org.apache.tapestry.grid.GridSortModel;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;

import java.util.List;

/**
 * Renders out the column headers for the grid, including links (where appropriate) to control column sorting.
 */
@SupportsInformalParameters
public class GridColumns
{
    /**
     * The object that provides access to bean and data models, which is typically the enclosing Grid component.
     */
    @Parameter(value = "componentResources.container")
    private GridModel _gridModel;

    /**
     * If true, then the CSS class on each &lt;TH&gt; element will be omitted, which can reduce the amount of output
     * from the component overall by a considerable amount. Leave this as false, the default, when you are leveraging
     * the CSS to customize the look and feel of particular columns.
     */
    @Parameter
    private boolean _lean;

    /**
     * Where to look for informal parameter Blocks used to override column headers.  The default is to look for such
     * overrides in the GridColumns component itself, but this is usually overridden.
     */
    @Parameter("componentResources")
    private ComponentResources _overrides;


    @SuppressWarnings("unused")
    @Component(parameters = { "event=sort", "disabled=sortDisabled", "context=columnModel.id", "class=sortLinkClass" })
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

    @Inject
    private Block _standardHeader;

    @GenerateAccessors
    private int _columnIndex;

    private int _lastColumnIndex;

    @GenerateAccessors(write = false)
    private PropertyModel _columnModel;

    void setupRender()
    {
        _lastColumnIndex = _gridModel.getDataModel().getPropertyNames().size() - 1;
    }

    public boolean isSortDisabled()
    {
        return !_columnModel.isSortable();
    }

    public String getSortLinkClass()
    {
        switch (getSortForColumn())
        {
            case ASCENDING:
                return GridConstants.SORT_ASCENDING_CLASS;

            case DESCENDING:
                return GridConstants.SORT_DESCENDING_CLASS;

            default:
                return null;
        }
    }

    private ColumnSort getSortForColumn()
    {
        GridSortModel sortModel = _gridModel.getSortModel();

        String columnId = _columnModel.getId();

        return sortModel.getColumnSort(columnId);
    }

    public String getHeaderClass()
    {
        List<String> classes = CollectionFactory.newList();

        if (!_lean) classes.add(_columnModel.getId());

        String sort = getSortLinkClass();

        if (sort != null) classes.add(sort);

        if (_columnIndex == 0) classes.add(GridConstants.FIRST_CLASS);

        if (_columnIndex == _lastColumnIndex) classes.add(GridConstants.LAST_CLASS);

        return TapestryInternalUtils.toClassAttributeValue(classes);
    }

    public boolean isActiveSortColumn()
    {
        return getSortForColumn() != ColumnSort.UNSORTED;
    }

    void onSort(String columnId)
    {
        _gridModel.getSortModel().updateSort(columnId);
    }

    public Asset getIcon()
    {
        switch (getSortForColumn())
        {
            case ASCENDING:
                return _ascendingAsset;

            case DESCENDING:
                return _descendingAsset;

            default:
                return _sortableAsset;
        }
    }

    public String getIconLabel()
    {
        switch (getSortForColumn())
        {
            case ASCENDING:
                return _messages.get("ascending");
            case DESCENDING:
                return _messages.get("descending");
            default:
                return _messages.get("sortable");
        }
    }

    public List<String> getColumnNames()
    {
        return _gridModel.getDataModel().getPropertyNames();
    }


    public void setColumnName(String columnName)
    {
        _columnModel = _gridModel.getDataModel().get(columnName);
    }


    public Block getBlockForColumn()
    {
        Block override = _overrides.getBlockParameter(_columnModel.getId() + "Header");

        if (override != null) return override;

        return _standardHeader;
    }
}
