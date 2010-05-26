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

import org.apache.tapestry5.ioc.Predicate;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.test.TestBase;
import org.apache.tapestry5.ioc.util.func.F;
import org.apache.tapestry5.ioc.util.func.Worker;
import org.testng.annotations.Test;

public class FuncTest extends TestBase
{
    private Coercion<String, Integer> stringToLength = new Coercion<String, Integer>()
    {
        public Integer coerce(String input)
        {
            return input.length();
        }
    };

    private Coercion<Integer, Boolean> toEven = new Coercion<Integer, Boolean>()
    {
        public Boolean coerce(Integer input)
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
    public void combine_coercions()
    {
        List<Boolean> even = F.map(F.combine(stringToLength, toEven), "Mary", "had", "a", "little", "lamb");

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

        Worker<String> op = new Worker<String>()
        {
            public void work(String value)
            {
                if (buffer.length() > 0)
                    buffer.append(" ");

                buffer.append(value);
            }
        };

        F.each(op, source);

        assertEquals(buffer.toString(), "Mary had a little lamb");
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
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        Predicate<String> combinedp = F.toPredicate(F.combine(stringToLength, toEven));
        Coercion<String, String> identity = F.identity();
        Predicate<String> isNull = F.isNull();

        // Converting to null and then filtering out nulls is the hard way to do filter or remove,
        // but exercises the code we want to test.

        List<String> filtered = F.remove(isNull, F.map(F.select(combinedp, identity), source));

        assertListsEquals(filtered, "Mary", "little", "lamb");
    }
}
