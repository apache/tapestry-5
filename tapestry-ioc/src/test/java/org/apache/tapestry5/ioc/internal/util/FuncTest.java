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

package org.apache.tapestry5.ioc.internal.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.ioc.Predicate;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.test.TestBase;
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

        List<Integer> lengths = Func.map(stringToLength, source);

        assertListsEquals(lengths, 4, 3, 1, 6, 4);
    }

    @Test
    public void combine_coercions()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        List<Boolean> even = Func.map(Func.combine(stringToLength, toEven), source);

        assertListsEquals(even, true, false, false, true, true);
    }

    @Test
    public void map_empty_collection_is_the_empty_list()
    {
        List<String> source = Arrays.asList();

        List<Integer> lengths = Func.map(stringToLength, source);

        assertSame(lengths, Collections.EMPTY_LIST);
    }

    @Test
    public void each()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        final StringBuffer buffer = new StringBuffer();

        Operation<String> op = new Operation<String>()
        {
            public void op(String value)
            {
                if (buffer.length() > 0)
                    buffer.append(" ");

                buffer.append(value);
            }
        };

        Func.each(op, source);

        assertEquals(buffer.toString(), "Mary had a little lamb");
    }

    @Test
    public void filter()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = Func.filter(evenp, input);

        assertListsEquals(output, 2, 4, 6);
    }

    @Test
    public void remove()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = Func.remove(evenp, input);

        assertListsEquals(output, 1, 3, 5, 7);
    }

    @Test
    public void filter_empty_is_the_empty_list()
    {
        List<Integer> input = Arrays.asList();

        List<Integer> output = Func.filter(evenp, input);

        assertSame(output, Collections.EMPTY_LIST);
    }

    @Test
    public void combine_predicate_with_and()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        List<Integer> output = Func.filter(Func.and(evenp, Func.gt(3)), input);

        assertListsEquals(output, 4, 6);
    }

    @Test
    public void numeric_comparison()
    {
        List<Integer> input = Arrays.asList(1, 2, 3, 4, 5, 6, 7);

        assertEquals(Func.filter(Func.eq(3), input), Arrays.asList(3));
        assertEquals(Func.filter(Func.neq(3), input), Arrays.asList(1, 2, 4, 5, 6, 7));
        assertEquals(Func.filter(Func.lt(3), input), Arrays.asList(1, 2));
        assertEquals(Func.filter(Func.lteq(3), input), Arrays.asList(1, 2, 3));
        assertEquals(Func.filter(Func.gteq(3), input), Arrays.asList(3, 4, 5, 6, 7));
    }

    @Test
    public void select_and_filter()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        Predicate<String> combinedp = Func.toPredicate(Func.combine(stringToLength, toEven));        
        Coercion<String, String> identity = Func.identity();
        Predicate<String> isNull = Func.isNull();

        // Converting to null and then filtering out nulls is the hard way to do filter or remove,
        // but exercises the code we want to test.
        
        List<String> filtered = Func.remove(isNull, Func.map(Func.select(combinedp, identity), source));

        assertListsEquals(filtered, "Mary", "little", "lamb");
    }
}
