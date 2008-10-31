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

import java.util.List;

public interface ToDoDatabase
{
    /**
     * Adds an item to the database, first assigning a unique id to the item.
     */
    void add(ToDoItem item);

    /**
     * Finds all items, sorted ascending by each item's order property.
     */
    List<ToDoItem> findAll();

    /**
     * Updates an existing item.
     *
     * @param item
     * @throws RuntimeException if the item does not exist
     */
    void update(ToDoItem item);

    /**
     * Resets the database, clearing out all data, re-adding base data.
     */
    void reset();

    /**
     * Deletes all items from the database.
     */
    void clear();

    /**
     * Removes an existing item
     *
     * @param itemId item to remove
     * @throws RuntimeException if the item does not exist
     */
    void remove(long itemId);

    /**
     * Gets the item, or returns null.
     */
    ToDoItem get(long itemId);
}
