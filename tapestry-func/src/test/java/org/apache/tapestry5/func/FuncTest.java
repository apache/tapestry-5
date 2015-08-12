// Copyright 2010, 2011 The Apache Software Foundation
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

import org.testng.annotations.Test;

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
        List<String> source = Arrays.asList();

        List<Integer> lengths = F.flow(source).map(stringToLength).toList();

        assertSame(lengths, Collections.EMPTY_LIST);
    }

    @Test
    public void each()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        final StringBuffer buffer = new StringBuffer();

        Worker<String> worker = new Worker<String>()
        {
            @Override
            public void work(String value)
            {
                if (buffer.length() > 0)
                    buffer.append(' ');

                buffer.append(value);
            }
        };

        F.flow(source).each(worker);

        assertEquals(buffer.toString(), "Mary had a little lamb");
    }

    @Test
    public void each_on_non_array_flow()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        final StringBuffer buffer = new StringBuffer();

        Worker<String> worker = new Worker<String>()
        {
            @Override
            public void work(String value)
            {
                if (buffer.length() > 0)
                    buffer.append(' ');

                buffer.append(value);
            }
        };

        F.flow(source).filter(new Predicate<String>()
        {
            @Override
            public boolean accept(String object)
            {
                return object.contains("a");
            }
        }).each(worker);

        assertEquals(buffer.toString(), "Mary had a lamb");
    }

    @Test
    public void flow_each()
    {
        Flow<String> flow = F.flow("Mary", "had", "a", "little", "lamb");

        final StringBuffer buffer = new StringBuffer();

        Worker<String> worker = new Worker<String>()
        {
            @Override
            public void work(String value)
            {
                if (buffer.length() > 0)
                    buffer.append(' ');

                buffer.append(value);
            }
        };

        assertSame(flow.each(worker), flow);

        assertEquals(buffer.toString(), "Mary had a little lamb");
    }

    @Test
    public void combine_workers()
    {
        final StringBuffer buffer = new StringBuffer();

        Worker<String> appendWorker = new Worker<String>()
        {
            @Override
            public void work(String value)
            {
                if (buffer.length() > 0)
                    buffer.append(' ');

                buffer.append(value);
            }
        };

        Worker<String> appendLength = new Worker<String>()
        {
            @Override
            public void work(String value)
            {
                buffer.append('(');
                buffer.append(value.length());
                buffer.append(')');
            }
        };

        F.flow("Mary", "had", "a", "little", "lamb").each(F.combine(appendWorker, appendLength));

        assertEquals(buffer.toString(), "Mary(4) had(3) a(1) little(6) lamb(4)");
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

        assertSame(output, Collections.EMPTY_LIST);
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

        assertEquals(isNull.accept(null), true);
        assertEquals(isNotNull.accept(null), false);

        assertEquals(isNull.accept("foo"), false);
        assertEquals(isNotNull.accept("bar"), true);
    }

    @Test
    public void array_flow_reduce()
    {
        int total = F.flow(F.flow("Mary", "had", "a", "little", "lamb").map(stringToLength).toList()).reduce(
                F.SUM_INTS, 0);

        assertEquals(total, 18);
    }

    @Test
    public void general_flow_reduce()
    {
        int total = F.flow("Mary", "had", "a", "little", "lamb").map(stringToLength).reduce(F.SUM_INTS, 0);

        assertEquals(total, 18);
    }

    @Test
    public void reverse_a_short_list_is_same_object()
    {
        Flow<Integer> empty = F.flow();

        assertSame(empty.reverse(), empty);

        Flow<Integer> one = F.flow(1);

        assertSame(one.reverse(), one);
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

        assertSame(empty.concat(flow), flow);
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

        Comparator<String> comparator = new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.length() - o2.length();
            }
        };

        assertSame(zero.sort(), zero);
        assertSame(zero.sort(comparator), zero);

        Flow<String> one = F.flow("Hello");

        assertSame(one.sort(), one);
        assertSame(one.sort(comparator), one);
    }

    @Test
    public void sort_using_explicit_comparator()
    {
        Flow<String> flow = F.flow("a", "eeeee", "ccc", "bb", "dddd");
        Comparator<String> comparator = new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.length() - o2.length();
            }
        };

        assertFlowValues(flow.sort(comparator), "a", "bb", "ccc", "dddd", "eeeee");
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void unable_to_sort_a_flow_of_non_comparables()
    {
        Flow<Locale> flow = F.flow(Locale.ENGLISH, Locale.FRANCE);

        flow.sort();
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

        assertEquals(total, 16);
    }

    @Test
    public void first_of_non_empty_flow()
    {
        assertEquals(F.flow("Mary", "had", "a", "little", "lamb").first(), "Mary");
    }

    @Test
    public void rest_of_non_empty_flow()
    {
        assertFlowValues(F.flow("Mary", "had", "a", "little", "lamb").rest(), "had", "a", "little", "lamb");
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
        assertSame(filteredEmpty.toList(), Collections.EMPTY_LIST);
    }

    @Test
    public void operations_on_empty_list_yield_empty()
    {
        assertSame(filteredEmpty.reverse(), F.EMPTY_FLOW);
        assertSame(filteredEmpty.sort(), F.EMPTY_FLOW);
        assertSame(filteredEmpty.sort(new Comparator<Integer>()
        {
            @Override
            public int compare(Integer o1, Integer o2)
            {
                unreachable();

                return 0;
            }
        }), F.EMPTY_FLOW);
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

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void remove_on_flow_iterator_is_not_supported()
    {
        Flow<Integer> flow = F.flow(1, 2, 3).filter(evenp);

        Iterator<Integer> it = flow.iterator();

        assertTrue(it.hasNext());
        assertEquals(it.next(), new Integer(2));

        it.remove();
    }

    @Test
    public void sort_with_comparator_on_non_array_flow()
    {
        Flow<String> flow = F.flow("Mary", "had", "a", "little", "lamb");

        List<String> result = flow.filter(new Predicate<String>()
        {
            @Override
            public boolean accept(String object)
            {
                return object.contains("a");
            }
        }).sort(new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.length() - o2.length();

            };
        }).toList();

        assertListsEquals(result, "a", "had", "Mary", "lamb");
    }

    @Test
    public void each_on_empty_flow()
    {
        Flow<Integer> flow = F.emptyFlow();

        assertSame(flow.each(new Worker<Integer>()
        {
            @Override
            public void work(Integer value)
            {
                unreachable();
            }
        }), flow);
    }

    @Test
    public void remove_on_empty_flow()
    {
        Flow<Integer> flow = F.emptyFlow();

        assertSame(flow.remove(evenp), flow);
    }

    @Test
    public void reduce_on_empty_flow()
    {
        Flow<Integer> flow = F.emptyFlow();
        Integer initial = 99;

        assertSame(flow.reduce(new Reducer<Integer, Integer>()
        {
            @Override
            public Integer reduce(Integer accumulator, Integer value)
            {
                unreachable();

                return null;
            }
        }, initial), initial);
    }

    @Test
    public void count_of_the_empty_flow_is_zero()
    {
        assertEquals(F.flow().count(), 0);
    }

    @Test
    public void count_of_array_flow()
    {
        assertEquals(F.flow(1, 2, 3).count(), 3);
    }

    @Test
    public void count_of_a_filtered_flow()
    {
        Flow<String> flow = F.flow("Mary", "had", "a", "little", "lamb");

        assertEquals(flow.filter(F.isNull()).count(), 0);
        assertEquals(flow.removeNulls().count(), 5);
    }

    @Test
    public void count_of_a_large_flow()
    {
        Flow<Integer> flow = F.series(1, 1).take(50000);

        assertEquals(flow.count(), 50000);
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
        Iterable<Integer> iterable = new Iterable<Integer>()
        {

            @Override
            public Iterator<Integer> iterator()
            {
                return Arrays.asList(9, 7, 1).iterator();
            }
        };

        Flow<Integer> flow = F.flow(iterable);

        assertFlowValues(flow, 9, 7, 1);
    }
}
