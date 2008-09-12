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

package org.apache.tapestry5.internal.grid;

import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;

import java.util.List;

/**
 * An implementation of {@link org.apache.tapestry5.grid.GridDataSource} used when the value null is provided as the
 * source.
 */
public class NullDataSource implements GridDataSource
{
    public int getAvailableRows()
    {
        return 0;
    }

    public Class getRowType()
    {
        return null;
    }

    public void prepare(int startIndex, int endIndex, List<SortConstraint> sortConstraints)
    {
    }

    public Object getRowValue(int index)
    {
        return null;
    }

}
