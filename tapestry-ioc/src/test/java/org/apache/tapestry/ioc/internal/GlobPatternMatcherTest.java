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

package org.apache.tapestry.ioc.internal;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

/**
 * 
 */
public class GlobPatternMatcherTest
{
    private boolean globMatch(String input, String pattern)
    {
        return new GlobPatternMatcher(pattern).matches(input);
    }

    @Test
    public void glob_match_exact()
    {
        assertTrue(globMatch("fred", "fred"));
        assertTrue(globMatch("fred", "FRED"));
        assertFalse(globMatch("xfred", "fred"));
        assertFalse(globMatch("fredx", "fred"));
        assertFalse(globMatch("fred", "xfred"));
        assertFalse(globMatch("fred", "fredx"));
    }

    @Test
    public void glob_match_wild()
    {
        assertTrue(globMatch("fred", "*"));
        assertTrue(globMatch("", "*"));
    }

    @Test
    public void glob_match_prefix()
    {
        assertTrue(globMatch("fred.Barney", "*Barney"));
        assertTrue(globMatch("fred.Barney", "*BARNEY"));
        assertFalse(globMatch("fred.Barneyx", "*Barney"));
        assertFalse(globMatch("fred.Barney", "*Barneyx"));
        assertFalse(globMatch("fred.Barney", "*xBarney"));
    }

    @Test
    public void glob_match_suffix()
    {
        assertTrue(globMatch("fred.Barney", "fred*"));
        assertTrue(globMatch("fred.Barney", "FRED*"));
        assertFalse(globMatch("xfred.Barney", "fred*"));
        assertFalse(globMatch("fred.Barney", "fredx*"));
        assertFalse(globMatch("fred.Barney", "xfred*"));
    }

    @Test
    public void glob_match_infix()
    {
        assertTrue(globMatch("fred.Barney", "*d.B*"));
        assertTrue(globMatch("fred.Barney", "*D.B*"));
        assertTrue(globMatch("fred.Barney", "*Barney*"));
        assertTrue(globMatch("fred.Barney", "*fred*"));
        assertTrue(globMatch("fred.Barney", "*FRED*"));
        assertFalse(globMatch("fred.Barney", "*flint*"));
    }

}
