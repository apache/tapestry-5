// Copyright 2010, 2011, 2025 The Apache Software Foundation
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

import org.apache.commons.lang3.StringUtils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ZippedFlowTests extends BaseFuncTest
{
    Flow<Integer> numbers = F.flow(1, 2, 3);

    Flow<String> names = F.flow("fred", "barney", "wilma", "betty");

    ZippedFlow<Integer, String> zipped = numbers.zipWith(names);

    @Test
    public void zipped_flow_to_map()
    {
        Map<Integer, String> map = zipped.toMap();

        assertEquals(3, map.size());
        assertEquals("barney", map.get(2));
    }

    @Test
    public void map_to_zipped_flow()
    {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "fred");
        map.put(2, "barney");

        ZippedFlow<Integer, String> zipped = F.zippedFlow(map);

        ZippedFlow<Integer, String> sorted = zipped.sort(F.<Integer, String> orderBySecond());

        assertFlowValues(sorted.firsts(), 2, 1);
        assertFlowValues(sorted.seconds(), "barney", "fred");

        sorted = zipped.sort(F.<Integer, String> orderByFirst());

        assertFlowValues(sorted.firsts(), 1, 2);
        assertFlowValues(sorted.seconds(), "fred", "barney");
    }

    @Test
    public void filter_on_first()
    {
        assertFlowValues(zipped.filterOnFirst(F.eql(3)).seconds(), "wilma");
    }

    @Test
    public void remove_on_first()
    {
        assertFlowValues(zipped.removeOnFirst(F.neq(2)).seconds(), "barney");
    }

    @Test
    public void filter_on_second()
    {
        assertFlowValues(zipped.filterOnSecond(F.startsWith("b")).seconds(), "barney");
    }

    @Test
    public void remove_on_second()
    {
        assertFlowValues(zipped.removeOnSecond(F.startsWith("b")).seconds(), "fred", "wilma");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void zip_flows_together()
    {
        assertListsEquals(zipped.toList(), Tuple.create(1, "fred"), Tuple.create(2, "barney"), Tuple.create(3, "wilma"));
    }

    @Test
    public void unzip_zipped_flow()
    {
        Tuple<Flow<Integer>, Flow<String>> unzipped = zipped.drop(1).unzip();
        Flow<Integer> unzippedNumbers = unzipped.first;
        Flow<String> unzippedNames = unzipped.second;

        assertListsEquals(unzippedNumbers.toList(), 2, 3);
        assertListsEquals(unzippedNames.toList(), "barney", "wilma");
    }

    @Test
    public void first_tuple_from_zipped_flow()
    {
        assertEquals(Tuple.create(3, "wilma"), zipped.drop(2).first());
    }

    @Test
    public void is_zipped_flow_empty()
    {
        assertFalse(zipped.isEmpty());

        assertTrue(zipped.filter(F.isNull()).isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void removeNulls()
    {
        Tuple<Integer, String> pebbles = Tuple.create(9, "pebbles");
        ZippedFlow<Integer, String> extendedFlow = zipped.concat(Arrays.asList(null, pebbles, null));
        ZippedFlow<Integer, String> noNulls = extendedFlow.removeNulls();

        assertEquals(6, extendedFlow.count());
        assertEquals(4, noNulls.count());

        assertEquals("pebbles", noNulls.reverse().seconds().first());
    }

    @Test
    public void rest_of_zipped_flow()
    {
        assertEquals("barney", zipped.rest().first().second);
    }

    @Test
    public void count_of_zipped_flow()
    {
        assertEquals(3, zipped.count());
    }

    @Test
    public void take_from_zipped_flow()
    {
        assertEquals("barney", zipped.take(2).reverse().first().second);
    }

    @Test
    public void zipped_worker()
    {
        final AtomicInteger count = new AtomicInteger();

        zipped.each(value -> count.addAndGet(value.second.length()));

        assertEquals(15, count.get());
    }

    @Test
    public void reduce_zipped_flow()
    {
        int totalLength = zipped.reduce((acc, value) -> acc + value.second.length(), 0);

        assertEquals(15, totalLength);
    }

    @Test
    public void remove_from_zipped_flow()
    {
        assertEquals(0, zipped.remove(F.notNull()).count());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void concat_a_zipped_flow()
    {
        Tuple<Integer, String> bambam = Tuple.create(4, "bam-bam");

        List<Tuple<Integer, String>> asList = Arrays.asList(bambam);

        ZippedFlow<Integer, String> zipped2 = zipped.concat(asList);

        assertEquals(4, zipped2.count());

        assertEquals("bam-bam", zipped2.reverse().seconds().first());
    }

    @Test
    public void firsts()
    {
        assertEquals((Integer)3, zipped.reverse().firsts().first());
    }

    @Test
    public void seconds()
    {
        assertEquals("fred", zipped.seconds().first());
    }

    @Test
    public void mapTuples()
    {
        Tuple<String, String> firstTuple = zipped.mapTuples(new Mapper<Tuple<Integer, String>, Tuple<String, String>>()
        {
            @Override
            public Tuple<String, String> map(Tuple<Integer, String> value)
            {
                return Tuple.create(StringUtils.reverse(value.second),
                        String.format("%d-%d", value.first, value.second.length()));
            }

        }).first();

        assertEquals("derf", firstTuple.first);
        assertEquals("1-4", firstTuple.second);
    }
}
