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

package org.apache.tapestry5.integration.app1.data;

import org.apache.tapestry5.beaneditor.NonVisual;
import org.apache.tapestry5.beaneditor.Validate;

import java.io.Serializable;

public class ToDoItem implements Serializable, Cloneable
{
    private static final long serialVersionUID = 329624498668043734L;

    private long id;

    private String title;

    private int order;

    private Urgency urgency = Urgency.MEDIUM;

    @Override
    public String toString()
    {
        return String.format("ToDoItem[%d %s]", id, title);
    }

    @Override
    public ToDoItem clone()
    {
        try
        {
            return (ToDoItem) super.clone();
        }
        catch (CloneNotSupportedException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @NonVisual
    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @Validate("required")
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Urgency getUrgency()
    {
        return urgency;
    }

    public void setUrgency(Urgency urgency)
    {
        this.urgency = urgency;
    }

    @NonVisual
    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

}
