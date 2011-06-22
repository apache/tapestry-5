// Copyright 2007, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.integration.app1.data.ToDoItem;
import org.apache.tapestry5.integration.app1.services.ToDoDatabase;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.util.DefaultPrimaryKeyEncoder;

import java.util.List;

public class ToDoList
{
    @Inject
    private ToDoDatabase database;

    private ToDoItem item;

    private DefaultPrimaryKeyEncoder<Long, ToDoItem> encoder;

    @Component
    private Form form;

    public List<ToDoItem> getItems()
    {
        return encoder.getValues();
    }

    public ToDoItem getItem()
    {
        return item;
    }

    public void setItem(ToDoItem item)
    {
        this.item = item;
    }

    public ToDoDatabase getDatabase()
    {
        return database;
    }

    public PrimaryKeyEncoder getEncoder()
    {
        return encoder;
    }

    void onPrepare()
    {
        List<ToDoItem> items = database.findAll();

        encoder = new DefaultPrimaryKeyEncoder<Long, ToDoItem>(long.class);

        for (ToDoItem item : items)
        {
            encoder.add(item.getId(), item);
        }
    }

    void onSuccess()
    {
        int order = 0;

        for (ToDoItem item : encoder.getValues())
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
            item.setOrder(encoder.getValues().size());

            database.add(item);
        }
    }

    void onActionFromReset()
    {
        database.reset();
    }
}
