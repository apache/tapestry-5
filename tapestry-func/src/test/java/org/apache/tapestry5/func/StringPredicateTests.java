// Copyright 2011, 2012 The Apache Software Foundation
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

public class StringPredicateTests extends BaseFuncTest
{
    @Test
    public void prefix_predicate()
    {
        Flow<String> names = F.flow("Howard", "Henry", "Anna", "Hubert");

        assertFlowValues(names.remove(F.startsWith("H")), "Anna");
    }

    @Test
    public void case_insensitive_prefix_predicate()
    {
        Flow<String> names = F.flow("Howard", "Henry", "Anna", "Hubert");

        assertFlowValues(names.filter(F.startsWithIgnoringCase("h")), "Howard", "Henry", "Hubert");
    }

    @Test
    public void suffix_predicate()
    {
        Flow<String> names = F.flow("Ted", "Charly", "Fred", "Anna");

        assertFlowValues(names.filter(F.endsWith("red")), "Fred");
    }

    @Test
    public void case_insensitive_suffix_predicate()
    {
        Flow<String> names = F.flow("Ted", "Charly", "Fred", "Anna");

        assertFlowValues(names.filter(F.endsWithIgnoringCase("RED")), "Fred");
    }

    @Test
    public void IS_BLANK()
    {
        Flow<String> names = F.flow("red", "", "green", null, "blue");

        assertFlowValues(names.remove(F.IS_BLANK), "red", "green", "blue");
    }
}
