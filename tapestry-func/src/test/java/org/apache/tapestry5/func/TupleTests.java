package org.apache.tapestry5.func;
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

import org.apache.tapestry5.func.Tuple;
import org.testng.annotations.Test;

public class TupleTests extends BaseFuncTest
{
    private Tuple<String, Integer> t = Tuple.create("tapestry", 5);

    @Test
    public void tuple_to_string()
    {
        assertEquals(t.toString(), "(tapestry, 5)");
    }

    @Test
    public void not_equal_null()
    {
        assertFalse(t.equals(null));
    }

    @Test
    public void not_equal_anything_else()
    {
        assertFalse(t.equals("a string"));
    }

    @Test
    public void values_must_be_equal()
    {
        assertFalse(t.equals(Tuple.create("tapestry", 4)));
    }

    @Test
    void identity_is_equal()
    {
        assertTrue(t.equals(t));
    }

    @Test
    public void equivalent_tuples_are_equal()
    {
        assertTrue(t.equals(Tuple.create("tapestry", 5)));
    }

    @Test
    public void equivalent_tuples_have_equal_hashcode()
    {
        assertTrue(t.hashCode() == Tuple.create("tapestry", 5).hashCode());
    }

    @Test
    public void unequal_values_have_different_hashcode()
    {
        assertFalse(t.hashCode() == Tuple.create("tapestry", 4).hashCode());
    }
}
