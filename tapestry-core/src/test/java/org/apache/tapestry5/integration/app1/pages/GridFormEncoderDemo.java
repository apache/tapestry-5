package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.PrimaryKeyEncoder;
import org.apache.tapestry5.integration.app1.data.ToDoItem;
import org.apache.tapestry5.util.DefaultPrimaryKeyEncoder;

public class GridFormEncoderDemo extends GridFormDemo
{
    public PrimaryKeyEncoder<Long, ToDoItem> getEncoder()
    {
        DefaultPrimaryKeyEncoder<Long, ToDoItem> result = new DefaultPrimaryKeyEncoder<Long, ToDoItem>();

        for (ToDoItem item : getItems())
        {
            result.add(item.getId(), item);
        }

        return result;
    }
}
