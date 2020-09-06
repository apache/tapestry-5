// Copyright 2008, 2009, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.integration.app1.data.DoubleItem;
import org.testng.Assert;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class FormInjectorDemo
{
    private static final long DEMO_CONTEXT_VALUE = System.currentTimeMillis();

    @Property
    private DoubleItem item;

    @Persist
    @Property(write = false)
    private double sum;

    @Persist
    private Map<Long, DoubleItem> database;

    private static final AtomicLong ID_ALLOCATOR = new AtomicLong(System.currentTimeMillis());

    private static class DoubleItemComparator implements Comparator<DoubleItem>
    {
        public int compare(DoubleItem o1, DoubleItem o2)
        {
            return (int) (o1.getId() - o2.getId());
        }
    }

    public ValueEncoder<DoubleItem> getDoubleItemEncoder()
    {
        return new ValueEncoder<DoubleItem>()
        {

            public DoubleItem toValue(String clientValue)
            {
                Long key = new Long(clientValue);

                return database.get(key);
            }

            public String toClient(DoubleItem value)
            {
                return String.valueOf(value.getId());
            }
        };
    }


    void onActivate()
    {
        if (database == null)
        {
            database = CollectionFactory.newConcurrentMap();
        }
    }

    public long getDemoContextValue()
    {
        return DEMO_CONTEXT_VALUE;
    }

    @Log
    public List<DoubleItem> getDoubleItems()
    {
        List<DoubleItem> items = CollectionFactory.newList(database.values());

        Collections.sort(items, new DoubleItemComparator());

        return items;
    }

    Object onAddRow(long context)
    {
        Assert.assertEquals(context, DEMO_CONTEXT_VALUE,
                "Context value provided to AjaxFormLoop must be provided to the event handler method.");

        DoubleItem item = new DoubleItem();
        item.setId(ID_ALLOCATOR.incrementAndGet());

        database.put(item.getId(), item);

        return item;
    }

    void onRemoveRow(DoubleItem item)
    {
        database.remove(item.getId());
    }

    void onPrepareForSubmit()
    {
        sum = 0;
    }

    void onAfterSubmit()
    {
        sum += item.getValue();
    }

    Object onPassivate()
    {
        return "FakePageActivationContextValue";
    }

    void onActivate(String context)
    {
        Assert.assertEquals(context, "FakePageActivationContextValue",
                "Page activation context was not passed through correctly.");
    }
}
