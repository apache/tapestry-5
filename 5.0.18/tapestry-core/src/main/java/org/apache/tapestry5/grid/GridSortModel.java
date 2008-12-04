// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.grid;

import java.util.List;

/**
 * Models the sorting applied to the a {@link org.apache.tapestry5.grid.GridDataSource}.
 */
public interface GridSortModel
{
    /**
     * Identifies how (or if) a column is sorted.
     *
     * @param columnId
     * @return the sort for the indicated column or {@link org.apache.tapestry5.grid.ColumnSort#UNSORTED} if the column
     *         is not used for sorting
     */
    ColumnSort getColumnSort(String columnId);

    /**
     * Updates the column sort.  The receiver determines how to handle the sort request.
     *
     * @param columnId property id of column to sort on
     */
    void updateSort(String columnId);

    /**
     * Returns a list of sort constraints, identifying which columns are sorted, and how.  May return an empty list (but
     * won't return null).
     *
     * @see org.apache.tapestry5.grid.GridDataSource#prepare(int, int, java.util.List)
     */
    List<SortConstraint> getSortConstraints();

    void clear();
}
