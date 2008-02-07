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

package org.apache.tapestry.grid;

import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.corelib.components.Grid;

/**
 * A provider of model data to the sub-components of the {@link Grid} component. The primary
 * implementor of this component is the Grid component itself.   In addition, provides access
 * to properties used to control paging and sort order.
 */
public interface GridModel
{
    /**
     * Returns the data model, which defines the columns (in terms of properties of the row type),
     * and provides access to actual values for a given row instance.
     */
    BeanModel getDataModel();

    /**
     * Returns the source for the data to be presented in the Grid.
     */
    GridDataSource getDataSource();

    /**
     * Indicates the current sort column.
     *
     * @return current sort column, or null for no column
     */
    String getSortColumnId();

    /**
     * Indicates if soft is ascending or descending
     *
     * @return sort ascending flag
     */
    boolean isSortAscending();

    /**
     * Updates the column sort.  When the columnId matches the current sort column, the
     * ascending/descending flag is toggled.  Otherwise, the specified column becomes
     * the sort column, and sort mode is switched to ascending.
     *
     * @param columnId property id of column to sort on
     */
    void updateSort(String columnId);
}
