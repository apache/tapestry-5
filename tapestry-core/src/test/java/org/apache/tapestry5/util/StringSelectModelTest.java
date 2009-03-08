// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.util;

import org.apache.tapestry5.test.TapestryTestCase;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.OptionModel;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Arrays;

public class StringSelectModelTest extends TapestryTestCase
{
    @Test
    public void generated_option_models_list_constructor()
    {
        replay();

        List<String> stooges = Arrays.asList("Moe", "Larry", "Curly Joe");
        SelectModel model = new StringSelectModel(stooges);

        List<OptionModel> options = model.getOptions();

        assertEquals(options.size(), stooges.size());

        for (int i = 0; i < options.size(); i++)
        {
            checkOption(options, i, stooges.get(i));
        }

        verify();
    }

    @Test
    public void generated_option_models_vararg_constructor()
    {
        replay();

        SelectModel model = new StringSelectModel("Moe", "Larry", "Curly Joe");

        List<OptionModel> options = model.getOptions();

        assertEquals(options.size(), 3);

        checkOption(options, 0, "Moe");
        checkOption(options, 1, "Larry");
        checkOption(options, 2, "Curly Joe");

        verify();
    }

    private void checkOption(List<OptionModel> options, int i, String value)
    {
        OptionModel model = options.get(i);

        assertEquals(model.getLabel(), value);
        assertFalse(model.isDisabled());
        assertSame(model.getValue(), value);
        assertNull(model.getAttributes());
    }
}
