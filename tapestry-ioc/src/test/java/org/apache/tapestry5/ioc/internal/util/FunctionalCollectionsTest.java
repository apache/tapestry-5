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
import java.util.List;

import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.Test;

public class FunctionalCollectionsTest extends TestBase
{
    @Test
    public void map()
    {
        List<String> source = Arrays.asList("Mary", "had", "a", "little", "lamb");

        Coercion<String, Integer> stringToLength = new Coercion<String, Integer>()
        {
            public Integer coerce(String input)
            {
                return input.length();
            }
        };

        List<Integer> lengths = FunctionalCollections.map(source, stringToLength);

        assertListsEquals(lengths, 4, 3, 1, 6, 4);
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

        FunctionalCollections.each(source, op);

        assertEquals(buffer.toString(), "Mary had a little lamb");
    }
}
