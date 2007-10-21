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

package org.apache.tapestry.grid;

import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.corelib.components.Form;
import org.apache.tapestry.corelib.components.Grid;

/**
 * Defines how a {@link Grid} components (and its sub-components) gain access to the row data that
 * is displayed on the page. In many cases, this is just a wrapper around a simple List, but the
 * abstractions exist to support access to a large data set that is accessible in sections.
 * <p>
 * This interface is still under development, as we work out the best approach to handling a
 * {@link Grid} inside a {@link Form} using a large dataset.
 */
public interface GridDataSource
{
    /** Returns the number of rows available in the data source. */
    int getAvailableRows();

    /**
     * Invoked to allow the source to prepare to present values. This gives the source a chance to
     * pre-fetch data (when appropriate) and informs the source of the desired sort order.
     * 
     * @param startIndex
     *            the starting index to be retrieved
     * @param endIndex
     *            the ending index to be retrieved
     * @param sortModel
     *            the property model that defines what data will be used for sorting, or null if no
     *            sorting is required (in which case, whatever natural order is provided by the
     *            underlying data source will be used)
     * @param ascending
     *            if true, then sort ascending, else descending
     */
    void prepare(int startIndex, int endIndex, PropertyModel sortModel, boolean ascending);

    /** Returns the row value at the provided index. This method will be invoked in sequential order. */
    Object getRowValue(int index);

    /**
     * Returns the type of value in the rows, or null if not known. This value is used to create a
     * default {@link BeanModel} when no such model is explicitly provided.
     * 
     * @return the row type, or null
     */
    Class getRowType();
}
