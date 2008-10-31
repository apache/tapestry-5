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

package org.apache.tapestry5.integration.app1.services;

import org.apache.tapestry5.integration.app1.data.ToDoItem;
import org.apache.tapestry5.integration.app1.data.Urgency;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * We clone everything that comes in or goes out. This does a reasonable job of simulating an external database. We just
 * use cloned copies of objects to represent data that's been marshalled into tables and columns.
 */
public class ToDoDatabaseImpl implements ToDoDatabase
{
    private long nextId = 1000;

    private final Map<Long, ToDoItem> items = CollectionFactory.newMap();

    public ToDoDatabaseImpl()
    {
        // A couple of items to get us started:

        reset();
    }

    public void clear()
    {
        items.clear();
    }


    public void reset()
    {
        items.clear();

        add("End World Hunger", Urgency.MEDIUM, 1);
        add("Develop Faster-Than-Light Travel", Urgency.HIGH, 2);
        add("Cure Common Cold", Urgency.LOW, 3);
    }

    private void add(String title, Urgency urgency, int order)
    {
        ToDoItem item = new ToDoItem();

        item.setTitle(title);
        item.setUrgency(urgency);
        item.setOrder(order);

        add(item);
    }

    public void add(ToDoItem item)
    {
        long id = nextId++;

        item.setId(id);

        items.put(id, item.clone());
    }

    public List<ToDoItem> findAll()
    {
        List<ToDoItem> result = newList();

        for (ToDoItem item : items.values())
            result.add(item.clone());

        Comparator<ToDoItem> comparator = new Comparator<ToDoItem>()
        {
            public int compare(ToDoItem o1, ToDoItem o2)
            {
                return o1.getOrder() - o2.getOrder();
            }
        };

        Collections.sort(result, comparator);

        return result;
    }

    public void update(ToDoItem item)
    {
        long id = item.getId();

        if (!items.containsKey(id))
            throw new RuntimeException(String.format("ToDoItem #%d not found.", id));

        items.put(id, item.clone());
    }

    public void remove(long itemId)
    {
        ToDoItem item = items.remove(itemId);

        if (item == null)
            throw new RuntimeException(String.format("ToDoItem #%d not found.", itemId));
    }

    public ToDoItem get(long itemId)
    {
        ToDoItem item = items.get(itemId);

        return item == null ? null : item.clone();
    }
}
