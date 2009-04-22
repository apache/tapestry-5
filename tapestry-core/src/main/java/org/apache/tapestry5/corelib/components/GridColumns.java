// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.grid.ColumnSort;
import org.apache.tapestry5.grid.GridConstants;
import org.apache.tapestry5.grid.GridModel;
import org.apache.tapestry5.grid.GridSortModel;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.List;

/**
 * Renders out the column headers for the grid, including links (where appropriate) to control column sorting.
 */
@SupportsInformalParameters
@Events(InternalConstants.GRID_INPLACE_UPDATE + " (internal event)")
public class GridColumns
{
    /**
     * The object that provides access to bean and data models, which is typically the enclosing Grid component.
     */
    @Parameter(value = "componentResources.container")
    private GridModel gridModel;

    /**
     * If true, then the CSS class on each &lt;TH&gt; element will be omitted, which can reduce the amount of output
     * from the component overall by a considerable amount. Leave this as false, the default, when you are leveraging
     * the CSS to customize the look and feel of particular columns.
     */
    @Parameter
    private boolean lean;

    /**
     * Where to look for informal parameter Blocks used to override column headers.  The default is to look for such
     * overrides in the GridColumns component itself, but this is usually overridden.
     */
    @Parameter("this")
    private PropertyOverrides overrides;

    /**
     * If not null, then each link is output as a link to update the specified zone.
     */
    @Parameter
    private String zone;

    @SuppressWarnings("unused")
    @Component(
            parameters = { "event=sort", "disabled=sortDisabled", "context=columnContext", "class=sortLinkClass",
                    "zone=inherit:zone" })
    private EventLink sort, sort2;

    @Inject
    @Path("sort-asc.png")
    private Asset ascendingAsset;

    @Inject
    @Path("sort-desc.png")
    private Asset descendingAsset;

    @Inject
    @Path("sortable.png")
    private Asset sortableAsset;

    @Inject
    private Messages messages;

    @Inject
    private Block standardHeader;

    /**
     * Optional output parameter that stores the current column index.
     */
    @Parameter
    @Property
    private int index;

    /**
     * Caches the index of the last column.
     */
    private int lastColumnIndex;

    @Property(write = false)
    private PropertyModel columnModel;

    @Inject
    private ComponentResources resources;

    void setupRender()
    {
        lastColumnIndex = gridModel.getDataModel().getPropertyNames().size() - 1;
    }

    public boolean isSortDisabled()
    {
        return !columnModel.isSortable();
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
        GridSortModel sortModel = gridModel.getSortModel();

        String columnId = columnModel.getId();

        return sortModel.getColumnSort(columnId);
    }

    public String getHeaderClass()
    {
        List<String> classes = CollectionFactory.newList();

        if (!lean) classes.add(columnModel.getId());

        String sort = getSortLinkClass();

        if (sort != null) classes.add(sort);

        if (index == 0) classes.add(GridConstants.FIRST_CLASS);

        if (index == lastColumnIndex) classes.add(GridConstants.LAST_CLASS);

        return TapestryInternalUtils.toClassAttributeValue(classes);
    }

    public boolean isActiveSortColumn()
    {
        return getSortForColumn() != ColumnSort.UNSORTED;
    }

    /**
     * Normal, non-Ajax event handler.
     */

    void onSort(String columnId)
    {
        gridModel.getSortModel().updateSort(columnId);
    }

    /**
     * Ajax event handler, which carries the zone id.
     */
    boolean onSort(String columnId, String zone)
    {
        onSort(columnId);

        resources.triggerEvent(InternalConstants.GRID_INPLACE_UPDATE, new Object[] { zone }, null);

        // Event is handled, don't trigger further event handler methods.

        return true;
    }

    public Asset getIcon()
    {
        switch (getSortForColumn())
        {
            case ASCENDING:
                return ascendingAsset;

            case DESCENDING:
                return descendingAsset;

            default:
                return sortableAsset;
        }
    }

    public Object getColumnContext()
    {
        if (zone == null) return columnModel.getId();

        return new Object[] { columnModel.getId(), zone };
    }

    public String getIconLabel()
    {
        switch (getSortForColumn())
        {
            case ASCENDING:
                return messages.get("ascending");
            case DESCENDING:
                return messages.get("descending");
            default:
                return messages.get("sortable");
        }
    }

    public List<String> getColumnNames()
    {
        return gridModel.getDataModel().getPropertyNames();
    }


    public void setColumnName(String columnName)
    {
        columnModel = gridModel.getDataModel().get(columnName);
    }


    public Block getBlockForColumn()
    {
        Block override = overrides.getOverrideBlock(columnModel.getId() + "Header");

        if (override != null) return override;

        return standardHeader;
    }
}
