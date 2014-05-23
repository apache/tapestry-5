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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

public class MapperTest extends BaseFuncTest
{
    protected Mapper<Integer, Flow<Integer>> sequencer = new Mapper<Integer, Flow<Integer>>()
    {

        @Override
        public Flow<Integer> map(Integer value)
        {
            Flow<Integer> flow = F.flow();

            for (int i = 0; i < value; i++)
                flow = flow.append(value);

            return flow;
        }
    };

    @Test
    public void map()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        List<Integer> lengths = F.flow(source).map(stringToLength).toList();

        assertListsEquals(lengths, 4, 3, 1, 6, 4);
    }

    @Test
    public void flow_map()
    {
        assertFlowValues(F.flow("Mary", "had", "a", "little", "lamb").map(stringToLength), 4, 3, 1, 6, 4);
    }

    @Test
    public void map_of_filtered_empty_is_empty()
    {
        assertTrue(filteredEmpty.map(new Mapper<Integer, Integer>()
        {
            @Override
            public Integer map(Integer value)
            {
                unreachable();

                return value;
            }
        }).isEmpty());
    }

    @Test
    public void mapcat_on_empty_flow_is_empty()
    {
        Flow<Integer> flow = F.flow();

        assertSame(flow.mapcat(sequencer), flow);

        assertTrue(filteredEmpty.mapcat(sequencer).isEmpty());
    }

    @Test
    public void mapcat()
    {
        Flow<Integer> flow = F.flow(3, 1, 2);

        assertFlowValues(flow.mapcat(sequencer), 3, 3, 3, 1, 2, 2);
    }

    @Test
    public void count_of_a_mapped_filtered_empty_flow()
    {
        Flow<Integer> flow = F.flow("Mary", "had", "etc.").filter(F.isNull()).map(stringToLength);

        assertTrue(flow.isEmpty());
        assertEquals(flow.count(), 0);
    }

    @Test
    public void toString_mapper()
    {
        Flow<Integer> flow = F.flow(1, 2, 3);

        assertFlowValues(flow.map(F.<Integer> stringValueOf()), "1", "2", "3");
    }

    @Test
    public void no_excess_mapping()
    {
        final AtomicInteger count = new AtomicInteger();

        Mapper<Integer, Integer> doubler = new Mapper<Integer, Integer>()
        {
            @Override
            public Integer map(Integer value)
            {
                count.incrementAndGet();

                return value * 2;
            }
        };

        assertFlowValues(F.range(1, 100).filter(F.gt(10)).map(doubler).take(3), 22, 24, 26);

        assertEquals(count.get(), 3);

        count.set(0);

        // Because of laziness, its possible to count all the values in some mapped lists, without
        // ever actually running the mapper to determine the final value.

        assertEquals(F.range(1, 100).map(doubler).count(), 99);
        assertEquals(count.get(), 0);

        // Because values are now lazily evaluated as well as flows, we can count the size of a flow
        // without ever actually calculating (via the mapper) an output value.

        Flow<Integer> flow = F.range(1, 100).map(doubler).concat(F.range(1, 10).map(doubler));
        assertEquals(flow.count(), 108);
        assertEquals(count.get(), 0);

        assertFlowValues(flow.take(2), 2, 4);
        assertEquals(count.get(), 2);

        count.set(0);

        // Isn't this nifty; take-ing from a flow will create a flow that references parts
        // of the original flow, and previously computed values (2 and 4) are still accessible!

        assertFlowValues(flow.take(3), 2, 4, 6);
        assertEquals(count.get(), 1);
    }

    @Test
    public void map2_empty_is_the_empty_flow()
    {
        Flow<Integer> flow = F.flow(1, 2, 3);

        assertSame(flow.map(F.ADD_INTS, filteredEmpty), F.EMPTY_FLOW);
        assertSame(filteredEmpty.map(F.ADD_INTS, flow), F.EMPTY_FLOW);
    }

    @Test
    public void map2()
    {
        Flow<Integer> left = F.range(10, 13);
        Flow<Integer> right = F.series(20, 2);

        assertFlowValues(left.map(F.ADD_INTS, right), 10 + 20, 11 + 22, 12 + 24);
    }
}
