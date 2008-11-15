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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

public class OutputRawTest extends TapestryTestCase
{
    @Test
    public void value_is_null()
    {
        MarkupWriter writer = mockMarkupWriter();

        replay();

        OutputRaw component = new OutputRaw();

        assertFalse(component.beginRender(writer));

        verify();
    }

    @Test
    public void value_is_empty_string()
    {
        MarkupWriter writer = mockMarkupWriter();

        replay();

        OutputRaw component = new OutputRaw();

        component.setValue("");

        assertFalse(component.beginRender(writer));

        verify();
    }

    @Test
    public void value_is_non_blank()
    {
        String value = "&nbsp;";

        MarkupWriter writer = mockMarkupWriter();

        writer.writeRaw(value);

        replay();

        OutputRaw component = new OutputRaw();

        component.setValue(value);

        assertFalse(component.beginRender(writer));

        verify();
    }
}
