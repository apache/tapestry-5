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

package org.apache.tapestry5.internal.grid;

import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.grid.ColumnSort;
import org.apache.tapestry5.grid.SortConstraint;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.services.BeanModelSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CollectionGridDataSourceTest extends InternalBaseTestCase
{
    // Just arbitrary numbers ...

    private static final int FRED = 99;

    private static final int BARNEY = 23;

    private static final int WILMA = 107;

    private static final int BETTY = 298;

    // Arrays.asList returns an unmodifiable list

    private final List raw = Arrays.asList(new Datum(FRED, "Fred"), new Datum(BARNEY, "Barney"),
                                           new Datum(WILMA, "Wilma"), new Datum(BETTY, null));

    private final CollectionGridDataSource source = new CollectionGridDataSource(raw);

    private BeanModel model;

    @BeforeClass
    public void setup()
    {
        BeanModelSource source = getService(BeanModelSource.class);

        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        model = source.create(Datum.class, false, messages);

        verify();
    }

    @AfterClass
    public void cleanup()
    {
        model = null;
    }

    @Test
    public void sort_on_number_ascending()
    {
        sort("id", true, BARNEY, FRED, WILMA, BETTY);
    }

    @Test
    public void sort_on_number_descending()
    {
        sort("id", false, BETTY, WILMA, FRED, BARNEY);
    }

    @Test
    public void sort_on_string_value_ascending()
    {
        // Nulls sort first

        // Without a secondary sort column, it's kind of arbitrary whether WILMA or BETTY is sorted
        // first.

        sort("value", true, BETTY, BARNEY, FRED, WILMA);
    }

    @Test
    public void sort_on_string_value_descending()
    {
        sort("value", false, WILMA, FRED, BARNEY, BETTY);
    }

    private void sort(String propertyName, boolean ascending, int... ids)
    {
        PropertyModel propertyModel = model.get(propertyName);

        int availableRows = source.getAvailableRows();

        SortConstraint constraint = new SortConstraint(propertyModel,
                                                       ascending ? ColumnSort.ASCENDING : ColumnSort.DESCENDING);
        List<SortConstraint> constraints = Collections.singletonList(constraint);

        source.prepare(0, availableRows - 1, constraints);

        for (int i = 0; i < ids.length; i++)
        {
            Datum row = (Datum) source.getRowValue(i);

            assertEquals(row.getId(), ids[i], "Id for Datum #" + i);
        }
    }
}
