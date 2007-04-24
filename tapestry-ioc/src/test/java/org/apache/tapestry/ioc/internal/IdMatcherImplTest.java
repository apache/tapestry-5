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

import org.apache.tapestry.ioc.IdMatcher;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class IdMatcherImplTest extends IOCInternalTestCase
{
    @Test
    public void invalid_pattern()
    {
        try
        {
            new IdMatcherImpl("foo");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            Assert.assertEquals(
                    ex.getMessage(),
                    "Pattern 'foo' does not contain a '.' seperator character.");
        }
    }

    @Test
    public void invalid_input()
    {
        try
        {
            new IdMatcherImpl("*.*").matches("fred");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            Assert.assertEquals(
                    ex.getMessage(),
                    "Input id 'fred' does not contain a '.' seperator character.");
        }

    }

    @Test(dataProvider = "id_match_values")
    public void id_match(String pattern, boolean expectedMatch)
    {
        IdMatcher matcher = new IdMatcherImpl(pattern);

        Assert.assertEquals(matcher.matches("foo.bar.Baz"), expectedMatch);
    }

    @DataProvider(name = "id_match_values")
    public Object[][] id_match_values()
    {
        return new Object[][]
        {
        { "foo*.Baz", true },
        { "*.Baz", true },
        { "foo.bar.*az", true },
        { "*fie*.*az", false },
        { "*.Goop", false } };
    }

}
