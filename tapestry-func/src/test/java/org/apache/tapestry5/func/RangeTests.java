// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.func;

import org.testng.annotations.Test;

public class RangeTests extends BaseFuncTest
{
    @Test
    public void empty_range_if_values_equal()
    {
        assertTrue(F.range(9, 9).isEmpty());
    }

    @Test
    public void ascending_range()
    {

        assertFlowValues(F.range(5, 8), 5, 6, 7);
    }

    @Test
    public void descending_range()
    {
        assertFlowValues(F.range(8, 5), 8, 7, 6);
    }

    @Test
    public void series()
    {
        Flow<Integer> series = F.series(3, 5);

        assertFlowValues(series.take(5), 3, 8, 13, 18, 23);
    }

    @Test
    public void filtered_series()
    {
        Flow<Integer> series = F.series(1, 1);

        assertFlowValues(series.filter(evenp).take(4), 2, 4, 6, 8);
    }

    @Test
    public void iterate()
    {
        Mapper<Integer, Integer> times2 = new Mapper<Integer, Integer>()
        {
            @Override
            public Integer map(Integer value)
            {
                return 2 * value;
            }
        };

        assertFlowValues(F.iterate(1, times2).take(5), 1, 2, 4, 8, 16);
    }
}
