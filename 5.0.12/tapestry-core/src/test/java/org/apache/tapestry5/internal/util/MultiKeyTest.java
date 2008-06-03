// Copyright 2006 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MultiKeyTest extends Assert
{
    @Test
    public void same_values_same_hash_codes()
    {
        MultiKey key1 = new MultiKey(1, 3, "foo");
        MultiKey key2 = new MultiKey(1, 3, "foo");
        MultiKey key3 = new MultiKey(1, 3);
        MultiKey key4 = new MultiKey(1, 3, "bar");
        MultiKey key5 = new MultiKey(1, 3, "foo", "bar");

        assertEquals(key2.hashCode(), key1.hashCode());
        assertFalse(key3.hashCode() == key1.hashCode());
        assertFalse(key4.hashCode() == key1.hashCode());
        assertFalse(key5.hashCode() == key1.hashCode());
    }

    @Test
    public void comparisons_against_not_multi_key()
    {
        MultiKey key = new MultiKey(1, 3, "foo");

        assertFalse(key.equals(null));
        assertFalse(key.equals("foo"));
    }

    @Test
    public void comparison_against_self()
    {
        MultiKey key = new MultiKey(1, 3, "foo");

        assertTrue(key.equals(key));
    }

    @Test
    public void comparisons_against_other_keys()
    {
        MultiKey key1 = new MultiKey(1, 3, "foo");
        MultiKey key2 = new MultiKey(1, 3, "foo");
        MultiKey key3 = new MultiKey(1, 3);
        MultiKey key4 = new MultiKey(1, 3, "bar");
        MultiKey key5 = new MultiKey(1, 3, "foo", "bar");

        assertEquals(key2, key1);
        assertFalse(key3.equals(key1));
        assertFalse(key4.equals(key1));
        assertFalse(key5.equals(key1));
    }

    @Test
    public void to_string()
    {
        assertEquals(new MultiKey("fred").toString(), "MultiKey[fred]");
        assertEquals(new MultiKey("fred", "barney").toString(), "MultiKey[fred, barney]");
    }
}
