//  Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.PrimaryKeyEncoder;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.integration.app1.data.DateHolder;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.util.DefaultPrimaryKeyEncoder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DateFieldAjaxFormLoop
{
    @Persist
    private Map<Integer, DateHolder> database;

    @Property
    private DateHolder current;

    void beginRender()
    {
        if (database == null)
            database = CollectionFactory.newMap();
    }


    public List<DateHolder> getDateHolders()
    {
        List<DateHolder> result = CollectionFactory.newList(database.values());

        Collections.sort(result, new Comparator<DateHolder>()
        {
            public int compare(DateHolder o1, DateHolder o2)
            {
                return o1.getId() - o2.getId();
            }
        });

        return result;
    }

    public PrimaryKeyEncoder<Integer, DateHolder> getDateHolderConverter()
    {
        DefaultPrimaryKeyEncoder<Integer, DateHolder> result =
                new DefaultPrimaryKeyEncoder<Integer, DateHolder>(Integer.class);

        for (DateHolder dh : getDateHolders())
        {
            result.add(dh.getId(), dh);
        }

        return result;
    }

    DateHolder onAddRowFromLoop()
    {
        DateHolder dh = new DateHolder();

        dh.setId(database.size() + 1);

        database.put(dh.getId(), dh);

        return dh;
    }

    void onRemoveRowFromLoop(DateHolder holder)
    {
        database.remove(holder.getId());
    }
}
