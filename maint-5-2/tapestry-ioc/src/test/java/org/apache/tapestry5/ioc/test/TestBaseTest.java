//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.test;

import org.testng.annotations.Test;

public class TestBaseTest extends TestBase
{
    @Test
    public void create_instance()
    {
        Bean b = create(Bean.class, "value", "Magic");

        assertEquals(b.getValue(), "Magic");
    }

    @Test
    public void create_instance_failure()
    {
        try
        {
            create(Runnable.class);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Unable to instantiate instance of java.lang.Runnable");
        }
    }

    @Test
    public void create_instance_field_missing()
    {
        try
        {
            create(Bean.class, "unknownField", "doesn't matter");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Unable to set field 'unknownField' of org.apache.tapestry5.ioc.test.Bean",
                                  "Class org.apache.tapestry5.ioc.test.Bean does not contain a field named 'unknownField'.");
        }
    }

    @Test
    public void type_mismatch_when_setting_field_value()
    {
        try
        {
            create(Bean.class, "value", 99);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Unable to set field 'value' of org.apache.tapestry5.ioc.test.Bean");
        }
    }

    @Test
    public void set_fields_from_base_class()
    {
        BeanSubclass b = create(BeanSubclass.class, "flag", true, "value", "magic");

        assertEquals(b.isFlag(), true);
        assertEquals(b.getValue(), "magic");
    }

    @Test
    public void get_field()
    {
        Bean b = new Bean();

        String expectedValue = "fred";

        set(b, "value", expectedValue);

        assertSame(b.getValue(), expectedValue);
        assertSame(get(b, "value"), expectedValue);
    }

    @Test
    public void error_getting_field()
    {
        Bean b = new Bean();

        try
        {
            get(b, "missingField");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex,
                                  "Unable to read field 'missingField' of org.apache.tapestry5.ioc.test.Bean",
                                  "Class org.apache.tapestry5.ioc.test.Bean does not contain a field named 'missingField'.");

        }
    }
}
