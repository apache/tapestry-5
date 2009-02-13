// Copyright 2006, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CodeEqTest extends Assert
{
    @Test(enabled = false, dataProvider = "stripValues")
    public void strip(String input, String expected)
    {
        assertEquals(CodeEq.strip(input), expected);
    }

    @DataProvider
    public Object[][] stripValues()
    {
        return new Object[][]
                {
                        { "foo", "foo" },
                        { " foo\n", "foo" },
                        { "  foo \nbar\n\n  \tbaz", "foo bar baz" },
                        { "{\n  bar();\n  baz();\n  if (gnip())\n  {\n    gnop();\n  }\n}\n",
                                "{bar(); baz(); if (gnip()){gnop();}}" } };
    }

    @Test(enabled = false)
    public void to_string()
    {
        CodeEq eq = new CodeEq("{ foo(); bar(); baz(); }");

        StringBuffer buffer = new StringBuffer();

        eq.appendTo(buffer);

        assertEquals(buffer.toString(), "codeEq({foo(); bar(); baz();})");
    }

    @Test(enabled = false, dataProvider = "matchValues")
    public void matches(String pattern, String parameter, boolean matches)
    {
        CodeEq ceq = new CodeEq(pattern);

        assertEquals(ceq.matches(parameter), matches);
    }

    @DataProvider
    public Object[][] matchValues()
    {
        return new Object[][]
                {
                        { "{ foo(); }", "{\n  foo();\n}", true },
                        { " foo();", "foo ();", false }, };
    }
}
