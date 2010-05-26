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

package org.apache.tapestry5.ioc.util.func;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.Test;

public class FuncTest extends TestBase
{
    private Mapper<String, Integer> stringToLength = new AbstractMapper<String, Integer>()
    {
        public Integer map(String input)
        {
            return input.length();
        }
    };

    private Mapper<Integer, Boolean> toEven = new AbstractMapper<Integer, Boolean>()
    {
        public Boolean map(Integer input)
        {
            return evenp.accept(input);
        }
    };

    private Predicate<Number> evenp = new Predicate<Number>()
    {
        public boolean accept(Number object)
        {
            return object.longValue() % 2 == 0;
        };
    };

    @Test
    public void map()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        List<Integer> lengths = F.map(stringToLength, source);

        assertListsEquals(lengths, 4, 3, 1, 6, 4);
    }

    @Test
    public void combine_mappers()
    {
        List<Boolean> even = F.map(stringToLength.combine(toEven), "Mary", "had", "a", "little", "lamb");

        assertListsEquals(even, true, false, false, true, true);
    }

    @Test
    public void map_empty_collection_is_the_empty_list()
    {
        List<String> source = Arrays.asList();

        List<Integer> lengths = F.map(stringToLength, source);

        assertSame(lengths, Collections.EMPTY_LIST);
    }

    @Test
    public void each()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        final StringBuffer buffer = new StringBuffer();

        Worker<String> worker = new AbstractWorker<String>()
        {
            public void work(String value)
            {
                if (buffer.length() > 0)
                    buffer.append(" ");

                buffer.append(value);
            }
        };

        F.each(worker, source);

        assertEquals(buffer.toString(), "Mary had a little lamb");
    }

    @Test
    public void combine_workers()
    {
        final StringBuffer buffer = new StringBuffer();

        Worker<String> appendWorker = new AbstractWorker<String>()
        {
            public void work(String value)
            {
                if (buffer.length() > 0)
                    buffer.append(" ");

                buffer.append(value);
            }
        };

        Worker<String> appendLength = new AbstractWorker<String>()
        {
            public void work(String value)
            {
                buffer.append("(");
                buffer.append(value.length());
                buffer.append(")");
            }
        };

        F.each(appendWorker.combine(appendLength), "Mary", "had", "a", "little", "lamb");

        assertEquals(buffer.toString(), "Mary(4) had(3) a(1) little(6) lamb(4)");
    }

    @Test
    public void wrap_coercion_as_mapper()
    {
        Coercion<String, String> toUpper = new Coercion<String, String>()
        {
            public String coerce(String input)
            {
                return input.toUpperCase();
            }
        };

        assertListsEquals(F.map(F.toMapper(toUpper), "Mary", "had", "a", "little", "lamb"), "MARY", "HAD", "A",
                "LITTLE", "LAMB");
    }

    @Test
    public void filter()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = F.filter(evenp, input);

        assertListsEquals(output, 2, 4, 6);
    }

    @Test
    public void remove()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = F.remove(evenp, input);

        assertListsEquals(output, 1, 3, 5, 7);
    }

    @Test
    public void filter_empty_is_the_empty_list()
    {
        List<Integer> input = Arrays.asList();

        List<Integer> output = F.filter(evenp, input);

        assertSame(output, Collections.EMPTY_LIST);
    }

    @Test
    public void combine_predicate_with_and()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = F.filter(F.and(evenp, F.gt(3)), input);

        assertListsEquals(output, 4, 6);
    }

    @Test
    public void numeric_comparison()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        assertEquals(F.filter(F.eq(3), input), Arrays.asList(3));
        assertEquals(F.filter(F.neq(3), input), Arrays.asList(1, 2, 4, 5, 6, 7));
        assertEquals(F.filter(F.lt(3), input), Arrays.asList(1, 2));
        assertEquals(F.filter(F.lteq(3), input), Arrays.asList(1, 2, 3));
        assertEquals(F.filter(F.gteq(3), input), Arrays.asList(3, 4, 5, 6, 7));
    }

    @Test
    public void select_and_filter()
    {
        Predicate<String> combinedp = F.toPredicate(stringToLength.combine(toEven));

        Mapper<String, String> identity = F.identity();
        Predicate<String> isNull = F.isNull();

        // Converting to null and then filtering out nulls is the hard way to do filter or remove,
        // but exercises the code we want to test.

        List<String> filtered = F.remove(isNull, F.map(F.select(combinedp, identity), "Mary", "had", "a", "little",
                "lamb"));

        assertListsEquals(filtered, "Mary", "little", "lamb");
    }
}
