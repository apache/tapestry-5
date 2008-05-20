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

import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.ioc.internal.util.Defense;

/**
 * Identifies how a single column (identified as a {@link org.apache.tapestry5.beaneditor.PropertyModel}) is sorted.
 */
public class SortConstraint
{
    private final PropertyModel propertyModel;

    private final ColumnSort columnSort;

    public SortConstraint(PropertyModel propertyModel, ColumnSort columnSort)
    {
        Defense.notNull(propertyModel, "propertyModel");
        Defense.notNull(columnSort, "columnSort");

        this.propertyModel = propertyModel;
        this.columnSort = columnSort;
    }

    public PropertyModel getPropertyModel()
    {
        return propertyModel;
    }

    public ColumnSort getColumnSort()
    {
        return columnSort;
    }

    // equals() is useful for testing

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SortConstraint that = (SortConstraint) o;

        if (columnSort != that.columnSort) return false;

        return propertyModel.equals(that.propertyModel);
    }
}
