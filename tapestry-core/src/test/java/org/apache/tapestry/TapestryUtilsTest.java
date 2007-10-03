// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TapestryUtilsTest extends Assert
{
    @Test(dataProvider = "string_quoting_input")
    public void string_quoting(String input, String expected)
    {
        assertEquals(TapestryUtils.quote(input), expected);
    }

    @DataProvider(name = "string_quoting_input")
    public Object[][] string_quoting_input()
    {
        return new Object[][]
        {
                { "Suzy said: \"It's not the proper time\".",
                        "'Suzy said: \\\"It\\'s not the proper time\\\".'" },
                { "regexp: \\d{4}", "'regexp: \\\\d{4}'" },

        };
    }

    @Test(dataProvider = "join_input")
    public void join_array(String[] inputs, String expected)
    {
        assertEquals(TapestryUtils.join(inputs), expected);
    }

    @Test(dataProvider = "join_input")
    public void join_list(String[] inputs, String expected)
    {
        List<String> list = Arrays.asList(inputs);

        assertEquals(TapestryUtils.join(list), expected);
    }

    @DataProvider(name = "join_input")
    public Object[][] join_input()
    {
        return new Object[][]
        {
        { new String[0], "" },
        { new String[]
        { "fred" }, "fred" },
        { new String[]
        { "fred", "barney", "wilma" }, "barney fred wilma" } };
    }
}
