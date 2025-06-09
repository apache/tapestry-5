// Copyright 2010, 2025 The Apache Software Foundation
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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseFuncTest
{

    protected Mapper<String, Integer> stringToLength = String::length;

    protected Mapper<Integer, Boolean> toEven = input -> this.evenp.accept(input);

    protected Predicate<Number> evenp = object -> object.longValue() % 2 == 0;

    protected Flow<Integer> filteredEmpty = F.flow(1, 3, 5, 7).filter(evenp);

    protected <T> void assertFlowValues(Flow<T> actual, T... expected)
    {
        assertListsEquals(actual.toList(), expected);
    }

    protected <T> void assertListsEquals(List<T> actual, T... expected)
    {
        assertEquals(Arrays.asList(expected), actual);
    }

    protected void unreachable()
    {
        throw new RuntimeException("Should not be reachable.");
    }
}
