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

package org.apache.tapestry.internal;

import org.apache.tapestry.OptionModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;

public class OptionModelImplTest extends Assert
{
    @Test
    public void basics()
    {
        OptionModel model = new OptionModelImpl("Label", false, this);

        assertEquals(model.getLabel(), "Label");
        assertFalse(model.isDisabled());
        assertSame(model.getValue(), this);
        assertNull(model.getAttributes());

        model = new OptionModelImpl("Fred", true, "fred");

        assertEquals(model.getLabel(), "Fred");
        assertTrue(model.isDisabled());

        assertEquals(model.toString(), "OptionModel[Fred fred]");
    }

    @Test
    public void attributes_as_extra_parameters()
    {
        OptionModel model = new OptionModelImpl("Label", false, this, "fred", "flintstone",
                                                "barney", "rubble");

        Map<String, String> attributes = model.getAttributes();

        assertEquals(attributes.size(), 2);
        assertEquals(attributes.get("fred"), "flintstone");
        assertEquals(attributes.get("barney"), "rubble");
    }

    @Test
    public void attributes_as_map_are_retained()
    {
        Map<String, String> attributes = Collections.emptyMap();

        OptionModel model = new OptionModelImpl("Label", false, this, attributes);

        assertSame(model.getAttributes(), attributes);
    }
}
