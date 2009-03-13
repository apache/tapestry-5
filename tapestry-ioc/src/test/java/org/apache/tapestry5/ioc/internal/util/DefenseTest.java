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

package org.apache.tapestry5.ioc.internal.util;

import static org.apache.tapestry5.ioc.internal.util.Defense.*;
import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.Test;

public class DefenseTest extends TestBase
{

    /**
     * Check that {@link Defense#notNull(T, String)} returns a non-null value.
     */
    @Test
    public void parameter_not_null()
    {
        assertSame(this, notNull(this, "foo"));
    }

    /**
     * Check that {@link Defense#notNull(T, String)} throws NPE when null, and check the message.
     */

    @Test
    public void parameter_is_null()
    {
        try
        {
            notNull(null, "foo");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Parameter foo was null.");
        }
    }

    @Test
    public void non_blank_parameter_is_null()
    {
        try
        {
            notBlank(null, "bar");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Parameter bar was null or contained only whitespace.");
        }
    }

    @Test
    public void non_blank_parameter_is_only_whitespace()
    {
        try
        {
            notBlank("  \t\n", "baz");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Parameter baz was null or contained only whitespace.");
        }
    }

    @Test
    public void non_blank_parameter_is_valid()
    {
        assertEquals("fred", notBlank(" fred\n", "biff"));
    }

    @Test
    public void cast_is_also_not_null()
    {
        try
        {
            cast(null, String.class, "fred");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Parameter fred was null.");
        }
    }

    @Test
    public void succesful_cast()
    {
        StringBuffer b = new StringBuffer();

        Appendable a = cast(b, Appendable.class, "fred");

        assertSame(a, b);
    }

    @Test
    public void bad_cast()
    {
        StringBuffer b = new StringBuffer("fred-value");

        try
        {
            cast(b, String.class, "fred");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Parameter fred (fred-value) is not assignable to type java.lang.String.");
        }
    }
}
