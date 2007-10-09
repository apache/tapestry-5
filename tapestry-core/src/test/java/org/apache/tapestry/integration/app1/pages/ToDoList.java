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

package org.apache.tapestry.integration.app1.pages;

import java.util.List;

import org.apache.tapestry.PrimaryKeyEncoder;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.corelib.components.Form;
import org.apache.tapestry.integration.app1.data.ToDoItem;
import org.apache.tapestry.integration.app1.services.ToDoDatabase;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.util.DefaultPrimaryKeyEncoder;

public class ToDoList
{
    @Inject
    private ToDoDatabase _database;

    private ToDoItem _item;

    private DefaultPrimaryKeyEncoder<Long, ToDoItem> _encoder;

    @Component
    private Form _form;

    public List<ToDoItem> getItems()
    {
        return _encoder.getValues();
    }

    public ToDoItem getItem()
    {
        return _item;
    }

    public void setItem(ToDoItem item)
    {
        _item = item;
    }

    public ToDoDatabase getDatabase()
    {
        return _database;
    }

    public PrimaryKeyEncoder getEncoder()
    {
        return _encoder;
    }

    void onPrepare()
    {
        List<ToDoItem> items = _database.findAll();

        _encoder = new DefaultPrimaryKeyEncoder<Long, ToDoItem>();

        for (ToDoItem item : items)
        {
            _encoder.add(item.getId(), item);
        }
    }

    void onSuccess()
    {
        int order = 0;

        for (ToDoItem item : _encoder.getValues())
        {
            item.setOrder(order++);
            _database.update(item);
        }
    }

    void onSelectedFromAddNew()
    {
        if (_form.isValid())
        {
            ToDoItem item = new ToDoItem();
            item.setTitle("<New To Do>");
            item.setOrder(_encoder.getValues().size());

            _database.add(item);
        }
    }

    void onActionFromReset()
    {
        _database.reset();
    }
}
