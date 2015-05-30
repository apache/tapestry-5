package org.apache.tapestry5.integration.app1.mixins;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.corelib.components.Grid;
import org.apache.tapestry5.grid.ColumnSort;
import org.apache.tapestry5.grid.GridSortModel;

public class InitialSort {
    @InjectContainer
    private Grid grid;

    @Parameter(required=true,allowNull=false,defaultPrefix = BindingConstants.LITERAL,name="column")
    private String sortColumn;

    @Parameter(allowNull = false, defaultPrefix = BindingConstants.LITERAL,name="order")
    private ColumnSort sortOrder = ColumnSort.ASCENDING;


    @SetupRender
    void setupRender() {
        GridSortModel sortModel = grid.getSortModel();
        if (sortModel.getSortConstraints().isEmpty()) {
            while (sortModel.getColumnSort(sortColumn) != sortOrder) {
                sortModel.updateSort(sortColumn);
            }
        }
    }
}
