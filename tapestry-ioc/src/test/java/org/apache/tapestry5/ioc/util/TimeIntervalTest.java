// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.util;

import org.apache.tapestry5.ioc.test.TestBase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TimeIntervalTest extends TestBase
{
    @Test
    public void use_constructor()
    {
        TimeInterval p = new TimeInterval("30 s");

        assertEquals(p.seconds(), 30);
        assertEquals(p.milliseconds(), 30 * 1000);

        assertEquals(p.toString(), "TimeInterval[30000 ms]");
    }

    @Test
    public void invalid_units()
    {
        try
        {
            TimeInterval.parseMilliseconds("30s 500mz");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "Unknown time interval unit 'mz' (in '30s 500mz').  Defined units: d, h, m, ms, s, y.");
        }
    }

    @Test
    public void unrecognized_input()
    {
        try
        {
            TimeInterval.parseMilliseconds("30s z 500ms");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Unexpected string 'z' (in time interval '30s z 500ms').");
        }
    }

    @Test
    public void unrecognized_input_at_end()
    {
        try
        {
            TimeInterval.parseMilliseconds("30s  500ms xyz");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Unexpected string 'xyz' (in time interval '30s  500ms xyz').");
        }
    }

    @Test(dataProvider = "mix_of_units_data")
    public void mix_of_units(String input, long expected)
    {
        assertEquals(TimeInterval.parseMilliseconds(input), expected);
    }

    @DataProvider
    public Object[][] mix_of_units_data()
    {
        return new Object[][] { { "54321", 54321 },

                { "30s", 30 * 1000 },

                { "1h 30m", 90 * 60 * 1000 },

                { "2d", 2 * 24 * 60 * 60 * 1000 },

                { "2m", 2 * 60 * 1000 },

                { "23ms", 23 }

        };
    }
}
