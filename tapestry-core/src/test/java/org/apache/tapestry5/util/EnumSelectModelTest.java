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

package org.apache.tapestry5.util;

import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.Stooge;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.util.List;

public class EnumSelectModelTest extends TapestryTestCase
{
    @Test
    public void generated_labels()
    {
        Messages messages = mockMessages();
        stub_contains(messages, false);

        replay();

        SelectModel model = new EnumSelectModel(Stooge.class, messages);

        List<OptionModel> options = model.getOptions();

        assertEquals(options.size(), 3);

        checkOption(options, 0, "Moe", Stooge.MOE);
        checkOption(options, 1, "Larry", Stooge.LARRY);
        checkOption(options, 2, "Curly Joe", Stooge.CURLY_JOE);

        verify();
    }

    @Test
    public void prefixed_name_in_message_catalog()
    {
        Messages messages = mockMessages();
        stub_contains(messages, false);

        train_contains(messages, "Stooge.LARRY", true);
        train_get(messages, "Stooge.LARRY", "Mr. Larry Fine");

        replay();

        SelectModel model = new EnumSelectModel(Stooge.class, messages);

        List<OptionModel> options = model.getOptions();

        assertEquals(options.size(), 3);

        checkOption(options, 0, "Moe", Stooge.MOE);
        checkOption(options, 1, "Mr. Larry Fine", Stooge.LARRY);
        checkOption(options, 2, "Curly Joe", Stooge.CURLY_JOE);

        verify();
    }

    @Test
    public void unprefixed_name_in_message_catalog()
    {
        Messages messages = mockMessages();
        stub_contains(messages, false);

        train_contains(messages, "MOE", true);
        train_get(messages, "MOE", "Sir Moe Howard");

        replay();

        SelectModel model = new EnumSelectModel(Stooge.class, messages);

        List<OptionModel> options = model.getOptions();

        assertEquals(options.size(), 3);

        checkOption(options, 0, "Sir Moe Howard", Stooge.MOE);
        checkOption(options, 1, "Larry", Stooge.LARRY);
        checkOption(options, 2, "Curly Joe", Stooge.CURLY_JOE);

        verify();
    }

    private void checkOption(List<OptionModel> options, int i, String label, Stooge value)
    {
        OptionModel model = options.get(i);

        assertEquals(model.getLabel(), label);
        assertFalse(model.isDisabled());
        assertSame(model.getValue(), value);
        assertNull(model.getAttributes());
    }
}
