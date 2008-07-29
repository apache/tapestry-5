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

package org.apache.tapestry5.grid;

import java.util.List;

/**
 * Defines how a {@link org.apache.tapestry5.corelib.components.Grid} component (and its sub-components) gain access to
 * the row data that is displayed on the page. In many cases, this is just a wrapper around a simple List, but the
 * abstractions exist to support access to a large data set that is accessible in sections.
 */
public interface GridDataSource
{
    /**
     * Returns the number of rows available in the data source.
     */
    int getAvailableRows();

    /**
     * Invoked to allow the source to prepare to present values. This gives the source a chance to pre-fetch data (when
     * appropriate) and informs the source of the desired sort order.  Sorting comes first, then extraction by range.
     *
     * @param startIndex      the starting index to be retrieved
     * @param endIndex        the ending index to be retrieved
     * @param sortConstraints identify how data is to be sorted
     */
    void prepare(int startIndex, int endIndex, List<SortConstraint> sortConstraints);

    /**
     * Returns the row value at the provided index. This method will be invoked in sequential order. In rare instances,
     * {@link #getAvailableRows()} may return a different number of rows than are actually available (i.e., the database
     * was changed between calls to {@link #getAvailableRows()} and the call to {@link #prepare(int, int,
     * java.util.List)}).  In that case, this method should return null for any out-of-range indexes.
     */
    Object getRowValue(int index);

    /**
     * Returns the type of value in the rows, or null if not known. This value is used to create a default {@link
     * org.apache.tapestry5.beaneditor.BeanModel} when no such model is explicitly provided.
     *
     * @return the row type, or null
     */
    Class getRowType();
}
