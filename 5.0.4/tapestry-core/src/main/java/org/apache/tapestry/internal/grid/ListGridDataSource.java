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

package org.apache.tapestry.internal.grid;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.grid.GridDataSource;

public class ListGridDataSource implements GridDataSource
{
    private final List _list;

    @SuppressWarnings("unchecked")
    public ListGridDataSource(final List list)
    {
        notNull(list, "list");

        // Copy the list so that we can sort it without distubing the original

        _list = newList(list);
    }

    public int getAvailableRows()
    {
        return _list.size();
    }

    @SuppressWarnings("unchecked")
    public void prepare(int startIndex, int endIndex, PropertyModel sortModel,
            final boolean ascending)
    {
        if (sortModel == null)
            return;

        final PropertyConduit conduit = sortModel.getConduit();

        final Comparator valueComparator = new Comparator<Comparable>()
        {
            public int compare(Comparable o1, Comparable o2)
            {
                // Simplify comparison, and handle case where both are nulls.

                if (o1 == o2)
                    return 0;

                if (o2 == null)
                    return 1;

                if (o1 == null)
                    return -1;

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
                int modifier = ascending ? 1 : -1;

                return modifier * rowComparator.compare(o1, o2);
            }
        };

        // We can freely sort this list because its just a copy.

        Collections.sort(_list, reverseComparator);
    }

    /** Returns the type of the first element in the list, or null if the list is empty. */
    public Class getRowType()
    {
        return _list.isEmpty() ? null : _list.get(0).getClass();
    }

    public Object getRowValue(int index)
    {
        return _list.get(index);
    }

}
