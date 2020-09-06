// Copyright 2007-2013 The Apache Software Foundation
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

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.beanmodel.PropertyModel;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.grid.ColumnSort;
import org.apache.tapestry5.grid.GridModel;
import org.apache.tapestry5.grid.GridSortModel;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Inject;

import java.util.List;

/**
 * Renders out the column headers for the grid, including links (where appropriate) to control column sorting.
 *
 * @tapestrydoc
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
            parameters = {"event=sort", "disabled=sortDisabled", "context=columnModel.id", "zone=inherit:zone"})
    private EventLink sort;

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

    @Inject
    private Request request;

    void setupRender()
    {
        lastColumnIndex = gridModel.getDataModel().getPropertyNames().size() - 1;
    }

    public boolean isSortDisabled()
    {
        return !columnModel.isSortable();
    }

    private ColumnSort getSortForColumn()
    {
        GridSortModel sortModel = gridModel.getSortModel();

        String columnId = columnModel.getId();

        return sortModel.getColumnSort(columnId);
    }

    void onBeginRenderFromLoop(MarkupWriter writer) {

        if (!lean) {
            writer.attributes("data-grid-property", columnModel.getId());
        }
        if(!isSortDisabled()){
            switch (getSortForColumn())
            {
                case ASCENDING:
                    writer.attributes("data-grid-column-sort", "ascending");
                    break;
  
                case DESCENDING:
                    writer.attributes("data-grid-column-sort", "descending");
                default:
                    writer.attributes("data-grid-column-sort", "sortable");
            }
        }

        if (index == 0) {
            writer.attributes("data-grid-column", "first");
        }

        if (index == lastColumnIndex) {
            writer.attributes("data-grid-column", "last");
        }
    }

    public boolean isActiveSortColumn()
    {
        return getSortForColumn() != ColumnSort.UNSORTED;
    }

    /**
     * Normal, non-Ajax event handler.
     */

    boolean onSort(String columnId)
    {
        gridModel.getSortModel().updateSort(columnId);

        if (request.isXHR())
        {
            resources.triggerEvent(InternalConstants.GRID_INPLACE_UPDATE, null, null);
        }

        return true;
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

    /**
     * Returns null or "true", depending on whether the Grid is rendering for in-place updates or not ("true"
     * means in-place updates). The affects whether the data-inplace-grid-links attribute will be rendered or not.
     *
     */
    public String getInplaceGridLinks()
    {
        return zone == null ? null : "true";
    }
}
