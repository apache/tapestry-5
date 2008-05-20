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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.OptionGroupModel;
import org.apache.tapestry5.OptionModel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OptionGroupModelImplTest extends Assert
{
    @Test
    public void basics()
    {
        List<OptionModel> options = Collections.emptyList();

        OptionGroupModel group = new OptionGroupModelImpl("Label", true, options);

        assertEquals(group.toString(), "OptionGroupModel[Label]");
        assertTrue(group.isDisabled());
        assertNull(group.getAttributes());
        assertSame(group.getOptions(), options);
    }

    @Test
    public void map_contructor_retains_map()
    {
        List<OptionModel> options = Collections.emptyList();
        Map<String, String> attributes = Collections.emptyMap();

        OptionGroupModel group = new OptionGroupModelImpl("Label", true, options, attributes);

        assertSame(group.getAttributes(), attributes);
    }

    @Test
    public void strings_contructor_builds_map()
    {
        List<OptionModel> options = Collections.emptyList();

        OptionGroupModel group = new OptionGroupModelImpl("Label", true, options, "fred",
                                                          "flintstone", "barney", "rubble");

        Map<String, String> attributes = group.getAttributes();

        assertEquals(attributes.size(), 2);
        assertEquals(attributes.get("fred"), "flintstone");
        assertEquals(attributes.get("barney"), "rubble");
    }
}
