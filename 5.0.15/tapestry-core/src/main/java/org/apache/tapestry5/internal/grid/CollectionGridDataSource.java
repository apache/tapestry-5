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

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.grid.ColumnSort;
import org.apache.tapestry5.grid.GridDataSource;
import org.apache.tapestry5.grid.SortConstraint;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectionGridDataSource implements GridDataSource
{
    private final List list;

    @SuppressWarnings("unchecked")
    public CollectionGridDataSource(final Collection collection)
    {
        Defense.notNull(collection, "collection");

        // Copy the collection so that we can sort it without disturbing the original

        list = CollectionFactory.newList(collection);
    }

    public int getAvailableRows()
    {
        return list.size();
    }

    public void prepare(int startIndex, int endIndex, List<SortConstraint> sortConstraints)
    {
        for (SortConstraint constraint : sortConstraints)
        {
            final ColumnSort sort = constraint.getColumnSort();

            if (sort == ColumnSort.UNSORTED) continue;

            final PropertyConduit conduit = constraint.getPropertyModel().getConduit();

            final Comparator valueComparator = new Comparator<Comparable>()
            {
                public int compare(Comparable o1, Comparable o2)
                {
                    // Simplify comparison, and handle case where both are nulls.

                    if (o1 == o2) return 0;

                    if (o2 == null) return 1;

                    if (o1 == null) return -1;

                    return o1.compareTo(o2);
                }
            };

            final Comparator rowComparator = new Comparator()
            {
                public int compare(Object row1, Object row2)
                {
                    Comparable value1 = (Comparable) conduit.get(row1);
                    Comparable value2 = (Comparable) conduit.get(row2);

                    return valueComparator.compare(value1, value2);
                }
            };

            final Comparator reverseComparator = new Comparator()
            {
                public int compare(Object o1, Object o2)
                {
                    int modifier = sort == ColumnSort.ASCENDING ? 1 : -1;

                    return modifier * rowComparator.compare(o1, o2);
                }
            };

            // We can freely sort this list because its just a copy.

            Collections.sort(list, reverseComparator);
        }
    }

    /**
     * Returns the type of the first element in the list, or null if the list is empty.
     */
    public Class getRowType()
    {
        return list.isEmpty() ? null : list.get(0).getClass();
    }

    public Object getRowValue(int index)
    {
        return list.get(index);
    }

}
