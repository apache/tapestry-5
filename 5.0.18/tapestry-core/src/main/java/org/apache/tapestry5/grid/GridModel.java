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

import org.apache.tapestry5.beaneditor.BeanModel;

/**
 * A provider of model data to the sub-components of the {@link org.apache.tapestry5.corelib.components.Grid} component.
 * The primary implementor of this component is the Grid component itself. This is effectively a way to package three
 * values as a single parameter to components such as {@link org.apache.tapestry5.corelib.components.GridColumns} and
 * {@link org.apache.tapestry5.corelib.components.GridRows}.
 */
public interface GridModel
{
    /**
     * Returns the data model, which defines the columns (in terms of properties of the row type), and provides access
     * to actual values for a given row instance.
     */
    BeanModel getDataModel();

    /**
     * Returns the source for the data to be presented in the Grid.
     */
    GridDataSource getDataSource();

    /**
     * Returns the object used to track sorting behavior of the Grid.
     */
    GridSortModel getSortModel();
}
