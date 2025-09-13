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

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FuncTest extends BaseFuncTest
{

    @Test
    public void flow_reverse()
    {
        assertFlowValues(F.flow(1, 2, 3).reverse(), 3, 2, 1);
    }

    @Test
    public void combine_mappers()
    {
        List<Boolean> even = F.flow("Mary", "had", "a", "little", "lamb").map(F.combine(stringToLength, toEven))
                .toList();

        assertListsEquals(even, true, false, false, true, true);
    }

    @Test
    public void map_empty_collection_is_the_empty_list()
    {
        List<String> source = Collections.emptyList();

        List<Integer> lengths = F.flow(source).map(stringToLength).toList();

        assertSame(Collections.emptyList(), lengths);
    }

    @Test
    public void each()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        final StringBuffer buffer = new StringBuffer();

        Worker<String> worker = value -> {
            if (buffer.length() > 0)
                buffer.append(' ');

            buffer.append(value);
        };

        F.flow(source).each(worker);

        assertEquals("Mary had a little lamb", buffer.toString());
    }

    @Test
    public void each_on_non_array_flow()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        final StringBuffer buffer = new StringBuffer();

        Worker<String> worker = value -> {
            if (buffer.length() > 0)
                buffer.append(' ');

            buffer.append(value);
        };

        F.flow(source).filter(object -> object.contains("a")).each(worker);

        assertEquals("Mary had a lamb", buffer.toString());
    }

    @Test
    public void flow_each()
    {
        Flow<String> flow = F.flow("Mary", "had", "a", "little", "lamb");

        final StringBuffer buffer = new StringBuffer();

        Worker<String> worker = value -> {
            if (buffer.length() > 0)
                buffer.append(' ');

            buffer.append(value);
        };

        assertSame(flow, flow.each(worker));

        assertEquals("Mary had a little lamb", buffer.toString());
    }

    @Test
    public void combine_workers()
    {
        final StringBuffer buffer = new StringBuffer();

        Worker<String> appendWorker = value -> {
            if (buffer.length() > 0)
                buffer.append(' ');

            buffer.append(value);
        };

        Worker<String> appendLength = value -> {
            buffer.append('(');
            buffer.append(value.length());
            buffer.append(')');
        };

        F.flow("Mary", "had", "a", "little", "lamb").each(F.combine(appendWorker, appendLength));

        assertEquals("Mary(4) had(3) a(1) little(6) lamb(4)", buffer.toString());
    }

    @Test
    public void filter()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = F.flow(input).filter(evenp).toList();

        assertListsEquals(output, 2, 4, 6);
    }

    @Test
    public void flow_filter()
    {
        assertFlowValues(F.flow(1, 2, 3, 4, 5, 6, 7).filter(evenp), 2, 4, 6);
    }

    @Test
    public void remove()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = F.flow(input).remove(evenp).toList();

        assertListsEquals(output, 1, 3, 5, 7);
    }

    @Test
    public void flow_remove()
    {
        List<Integer> output = F.flow(1, 2, 3, 4, 5, 6, 7).remove(evenp).toList();

        assertListsEquals(output, 1, 3, 5, 7);
    }

    @Test
    public void filter_empty_is_the_empty_list()
    {
        List<Integer> input = Arrays.asList();

        List<Integer> output = F.flow(input).filter(evenp).toList();

        assertSame(Collections.emptyList(), output);
    }

    @Test
    public void combine_predicate_with_and()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = F.flow(input).filter(F.and(F.gt(2), F.lt(5))).toList();

        assertListsEquals(output, 3, 4);
    }

    @Test
    public void combine_predicate_with_or()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = F.flow(input).filter(F.or(F.lt(3), F.gt(5))).toList();

        assertListsEquals(output, 1, 2, 6, 7);
    }

    @Test
    public void eql_predicate()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = F.flow(input).filter(F.eql(4)).toList();

        assertListsEquals(output, 4);
    }

    @Test
    public void numeric_comparison()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        assertEquals(F.flow(input).filter(F.eq(3)).toList(), Arrays.asList(3));
        assertEquals(F.flow(input).filter(F.neq(3)).toList(), Arrays.asList(1, 2, 4, 5, 6, 7));
        assertEquals(F.flow(input).filter(F.lt(3)).toList(), Arrays.asList(1, 2));
        assertEquals(F.flow(input).filter(F.lteq(3)).toList(), Arrays.asList(1, 2, 3));
        assertEquals(F.flow(input).filter(F.gteq(3)).toList(), Arrays.asList(3, 4, 5, 6, 7));
    }

    @Test
    public void select_and_filter()
    {
        Predicate<String> combinedp = F.toPredicate(F.combine(stringToLength, toEven));

        Mapper<String, String> identity = F.identity();
        Predicate<String> isNull = F.isNull();

        // Converting to null and then filtering out nulls is the hard way to do filter or remove,
        // but exercises the code we want to test.

        List<String> filtered = F.flow("Mary", "had", "a", "little", "lamb").map(F.select(combinedp, identity))
                .remove(isNull).toList();

        assertListsEquals(filtered, "Mary", "little", "lamb");
    }

    @Test
    public void null_and_not_null()
    {
        Predicate<String> isNull = F.isNull();
        Predicate<String> isNotNull = F.notNull();

        assertTrue(isNull.accept(null));
        assertFalse(isNotNull.accept(null));

        assertFalse(isNull.accept("foo"));
        assertTrue(isNotNull.accept("bar"));
    }

    @Test
    public void array_flow_reduce()
    {
        int total = F.flow(F.flow("Mary", "had", "a", "little", "lamb").map(stringToLength).toList()).reduce(
                F.SUM_INTS, 0);

        assertEquals(18, total);
    }

    @Test
    public void general_flow_reduce()
    {
        int total = F.flow("Mary", "had", "a", "little", "lamb").map(stringToLength).reduce(F.SUM_INTS, 0);

        assertEquals(18, total);
    }

    @Test
    public void reverse_a_short_list_is_same_object()
    {
        Flow<Integer> empty = F.flow();

        assertSame(empty, empty.reverse());

        Flow<Integer> one = F.flow(1);

        assertSame(one, one.reverse());
    }

    @Test
    public void concat_flows()
    {
        Flow<Integer> first = F.flow(1, 2, 3);

        Flow<Integer> updated = first.concat(F.flow(4, 5, 6));

        assertFlowValues(updated, 1, 2, 3, 4, 5, 6);
    }

    @Test
    public void concat_onto_empty_list()
    {
        Flow<Integer> empty = F.flow();
        Flow<Integer> flow = F.flow(1, 2, 3);

        assertSame(flow, empty.concat(flow));
    }

    @Test
    public void concat_list_onto_flow()
    {
        Flow<Integer> first = F.flow(1, 2, 3);

        Flow<Integer> updated = first.concat(Arrays.asList(4, 5, 6));

        assertFlowValues(updated, 1, 2, 3, 4, 5, 6);
    }

    @Test
    public void append_values_onto_flow()
    {
        Flow<Integer> first = F.flow(1, 2, 3);

        Flow<Integer> updated = first.append(4, 5, 6);

        assertFlowValues(updated, 1, 2, 3, 4, 5, 6);
    }

    @Test
    public void sort_comparable_list()
    {
        assertFlowValues(F.flow("fred", "barney", "wilma", "betty").sort(), "barney", "betty", "fred", "wilma");
    }

    @Test
    public void sort_a_short_list_returns_same()
    {
        Flow<String> zero = F.flow();

        Comparator<String> comparator = (o1, o2) -> o1.length() - o2.length();

        assertSame(zero, zero.sort());
        assertSame(zero, zero.sort(comparator));

        Flow<String> one = F.flow("Hello");

        assertSame(one, one.sort());
        assertSame(one, one.sort(comparator));
    }

    @Test
    public void sort_using_explicit_comparator()
    {
        Flow<String> flow = F.flow("a", "eeeee", "ccc", "bb", "dddd");
        Comparator<String> comparator = (o1, o2) -> o1.length() - o2.length();

        assertFlowValues(flow.sort(comparator), "a", "bb", "ccc", "dddd", "eeeee");
    }

    @Test
    public void unable_to_sort_a_flow_of_non_comparables()
    {
        Flow<Locale> flow = F.flow(Locale.ENGLISH, Locale.FRANCE);

        assertThrows(ClassCastException.class, flow::sort);
    }

    @Test
    public void flows_are_iterable()
    {
        Flow<Integer> flow = F.flow(1, 3, 5, 7);

        int total = 0;

        for (int i : flow)
        {
            total += i;
        }

        assertEquals(16, total);
    }

    @Test
    public void first_of_non_empty_flow()
    {
        String first = F.flow("Mary", "had", "a", "little", "lamb").first();

        assertEquals("Mary", first);
    }

    @Test
    public void rest_of_non_empty_flow()
    {
        Flow<String> rest = F.flow("Mary", "had", "a", "little", "lamb").rest();

        assertFlowValues(rest, "had", "a", "little", "lamb");
    }

    @Test
    public void flow_rest_is_cached()
    {
        Flow<Integer> flow = F.flow(1, 2, 3);

        assertSame(flow.rest(), flow.rest());
    }

    @Test
    public void first_of_empty_is_null()
    {
        assertNull(F.flow().first());
    }

    @Test
    public void rest_of_empty_is_still_empty_and_not_null()
    {
        assertTrue(F.flow().rest().isEmpty());
    }

    @Test
    public void list_of_empty_flow_is_empty()
    {
        assertTrue(filteredEmpty.isEmpty());
        assertSame(Collections.emptyList(), filteredEmpty.toList());
    }

    @Test
    public void operations_on_empty_list_yield_empty()
    {
        assertSame(filteredEmpty.reverse(), F.EMPTY_FLOW);
        assertSame(filteredEmpty.sort(), F.EMPTY_FLOW);
        assertSame(F.EMPTY_FLOW, filteredEmpty.sort((o1, o2) -> {
            unreachable();

            return 0;
        }));
    }

    @Test
    public void sort_non_array_flow()
    {
        assertFlowValues(filteredEmpty.append(7, 3, 9).sort(), 3, 7, 9);
    }

    @Test
    public void reverse_non_array_flow()
    {
        assertFlowValues(filteredEmpty.append(1, 2, 3).reverse(), 3, 2, 1);
    }

    @Test
    public void remove_on_flow_iterator_is_not_supported()
    {
        Flow<Integer> flow = F.flow(1, 2, 3).filter(evenp);

        Iterator<Integer> it = flow.iterator();

        assertTrue(it.hasNext());
        assertEquals(it.next(), Integer.valueOf(2));

        assertThrows(UnsupportedOperationException.class, it::remove);
    }

    @Test
    public void sort_with_comparator_on_non_array_flow()
    {
        Flow<String> flow = F.flow("Mary", "had", "a", "little", "lamb");

        List<String> result = flow
            .filter(object -> object.contains("a"))
            .sort((o1, o2) -> o1.length() - o2.length())
            .toList();

        assertListsEquals(result, "a", "had", "Mary", "lamb");
    }

    @Test
    public void each_on_empty_flow()
    {
        Flow<Integer> flow = F.emptyFlow();

        assertSame(flow, flow.each(value -> unreachable()));
    }

    @Test
    public void remove_on_empty_flow()
    {
        Flow<Integer> flow = F.emptyFlow();

        assertSame(flow, flow.remove(evenp));
    }

    @Test
    public void reduce_on_empty_flow()
    {
        Flow<Integer> flow = F.emptyFlow();
        Integer initial = 99;

        assertSame(initial, flow.reduce((accumulator, value) -> {
            unreachable();

            return null;
        }, initial));
    }

    @Test
    public void count_of_the_empty_flow_is_zero()
    {
        assertEquals(0, F.flow().count());
    }

    @Test
    public void count_of_array_flow()
    {
        assertEquals(3, F.flow(1, 2, 3).count());
    }

    @Test
    public void count_of_a_filtered_flow()
    {
        Flow<String> flow = F.flow("Mary", "had", "a", "little", "lamb");

        assertEquals(0, flow.filter(F.isNull()).count());
        assertEquals(5, flow.removeNulls().count());
    }

    @Test
    public void count_of_a_large_flow()
    {
        Flow<Integer> flow = F.series(1, 1).take(50_000);

        assertEquals(50_000, flow.count());
    }

    @Test
    public void concat_empty_list()
    {
        Flow<Integer> flow = F.flow(1, 3);

        assertFlowValues(flow.concat(flow.filter(evenp)), 1, 3);
    }

    @Test
    public void to_array()
    {
        Flow<Integer> flow = F.range(1, 10).drop(2).take(3);

        assertTrue(Arrays.equals(flow.toArray(Integer.class), new Integer[]
        { 3, 4, 5 }));
    }

    @Test
    public void lazy_flow_from_iterable()
    {
        Iterable<Integer> iterable = () -> Arrays.asList(9, 7, 1).iterator();

        Flow<Integer> flow = F.flow(iterable);

        assertFlowValues(flow, 9, 7, 1);
    }
}
