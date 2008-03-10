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


package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.integration.app1.data.ToDoItem;
import org.apache.tapestry.integration.app1.services.ToDoDatabase;
import org.apache.tapestry.ioc.annotations.Inject;

import java.util.List;

public class DeleteFromGridDemo
{
    @Inject
    private ToDoDatabase _database;

    private ToDoItem _item;

    void onActionFromSetup()
    {
        _database.clear();

        for (int i = 1; i <= 10; i++)
        {
            ToDoItem item = new ToDoItem();
            item.setTitle(String.format("ToDo #%d", i));
            item.setOrder(i);

            _database.add(item);
        }
    }

    void onActionFromDelete(long id)
    {
        _database.remove(id);
    }

    public List<ToDoItem> getItems() { return _database.findAll(); }

    public ToDoItem getItem()
    {
        return _item;
    }

    public void setItem(ToDoItem item)
    {
        _item = item;
    }
}
