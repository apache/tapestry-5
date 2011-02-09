// Copyright 2007, 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app1.pages;

import java.util.List;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.integration.app1.data.ToDoItem;
import org.apache.tapestry5.integration.app1.services.ToDoDatabase;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ToDoList
{
    @Property
    @Inject
    private ToDoDatabase database;

    private List<ToDoItem> items;

    private ToDoItem item;

    @Component
    private Form form;

    public ValueEncoder<ToDoItem> getToDoItemEncoder()
    {
        return new ValueEncoder<ToDoItem>()
        {
            public ToDoItem toValue(String clientValue)
            {
                final long id = Long.parseLong(clientValue);

                return F.flow(items).filter(new Predicate<ToDoItem>()
                {
                    public boolean accept(ToDoItem element)
                    {
                        return element.getId() == id;
                    }
                }).first();
            }

            public String toClient(ToDoItem value)
            {
                return String.valueOf(value.getId());
            }
        };
    }

    public List<ToDoItem> getItems()
    {
        return items;
    }

    public ToDoItem getItem()
    {
        return item;
    }

    public void setItem(ToDoItem item)
    {
        this.item = item;
    }

    void onPrepare()
    {
        items = database.findAll();
    }

    void onSuccess()
    {
        int order = 0;

        for (ToDoItem item : items)
        {
            item.setOrder(order++);
            database.update(item);
        }
    }

    void onSelectedFromAddNew()
    {
        if (form.isValid())
        {
            ToDoItem item = new ToDoItem();
            item.setTitle("<New To Do>");
            item.setOrder(items.size());

            database.add(item);
        }
    }

    void onActionFromReset()
    {
        database.reset();
    }
}
