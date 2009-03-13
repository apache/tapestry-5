// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GlobPatternMatcherTest extends Assert
{
    private boolean globMatch(String input, String pattern)
    {
        return new GlobPatternMatcher(pattern).matches(input);
    }

    @DataProvider
    public Object[][] matches()
    {
        return new Object[][]
                {
                        { "fred", "fred" },
                        { "fred", "FRED" },
                        { "fred", "*" },
                        { "", "*" },
                        { "fred.Barney", "*Barney" },
                        { "fred.Barney", "*BARNEY" },
                        { "fred.Barney", "fred*" },
                        { "fred.Barney", "FRED*" },
                        { "fredBarney", "*dB*" },
                        { "fredBarney", "*DB*" },
                        { "fred.Barney", "*Barney*" },
                        { "fred.Barney", "*fred*" },
                        { "fred.Barney", "*FRED*" },
                        { "MyEntityDAO", ".*dao" },
                        { "FredDAO", "(fred|barney)dao" }
                };
    }

    @Test(dataProvider = "matches")
    public void successful_glob_match(String input, String pattern)
    {
        assertTrue(globMatch(input, pattern));
    }


    @DataProvider
    public Object[][] mismatches()
    {
        return new Object[][]
                {
                        { "xfred", "fred" },
                        { "fredx", "fred" },
                        { "fred", "xfred" },
                        { "fred", "fredx" },
                        { "fred.Barneyx", "*Barney" },
                        { "fred.Barney", "*Barneyx" },
                        { "fred.Barney", "*xBarney" },
                        { "xfred.Barney", "fred*" },
                        { "fred.Barney", "fredx*" },
                        { "fred.Barney", "xfred*" },
                        { "fred.Barney", "*flint*" },
                        { "MyEntityDAL", ".*dao" },
                        { "WilmaDAO", "(fred|barney)dao" }
                };
    }


    @Test(dataProvider = "mismatches")
    public void unsuccessful_glob_match(String input, String pattern)
    {
        assertFalse(globMatch(input, pattern));
    }
}
