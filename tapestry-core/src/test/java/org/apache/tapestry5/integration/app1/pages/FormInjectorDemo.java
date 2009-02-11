// Copyright 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.integration.app1.data.DoubleItem;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
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

    private static final Map<Long, DoubleItem> DB = CollectionFactory.newConcurrentMap();

    private static final AtomicLong ID_ALLOCATOR = new AtomicLong(System.currentTimeMillis());

    private static class DoubleItemComparator implements Comparator<DoubleItem>
    {
        public int compare(DoubleItem o1, DoubleItem o2)
        {
            return (int) (o1.getId() - o2.getId());
        }
    }

    public PrimaryKeyEncoder getEncoder()
    {
        return new PrimaryKeyEncoder<Long, DoubleItem>()
        {
            public Long toKey(DoubleItem value)
            {
                return value.getId();
            }

            public void prepareForKeys(List<Long> keys)
            {
            }

            public DoubleItem toValue(Long key)
            {
                return DB.get(key);
            }

            public Class<Long> getKeyType()
            {
                return Long.class;
            }
        };
    }

    public long getDemoContextValue()
    {
        return DEMO_CONTEXT_VALUE;
    }

    @Log
    public List<DoubleItem> getDoubleItems()
    {
        List<DoubleItem> items = CollectionFactory.newList(DB.values());

        Collections.sort(items, new DoubleItemComparator());

        return items;
    }

    Object onAddRow(long context)
    {
        Assert.assertEquals(context, DEMO_CONTEXT_VALUE,
                            "Context value provided to AjaxFormLoop must be provided to the event handler method.");

        DoubleItem item = new DoubleItem();
        item.setId(ID_ALLOCATOR.incrementAndGet());

        DB.put(item.getId(), item);

        return item;
    }

    void onRemoveRow(DoubleItem item)
    {
        DB.remove(item.getId());
    }

    void onPrepareForSubmit()
    {
        sum = 0;
    }

    void onAfterSubmit()
    {
        sum += item.getValue();
    }


    void onActionFromReset()
    {
        DB.clear();
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
