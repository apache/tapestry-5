// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.Locale;

public class OutputTest extends TapestryTestCase
{
    private final Number value = 22.7d;

    private final DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance(Locale.US);

    private final DecimalFormat filterFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);

    @BeforeClass
    public void setup()
    {
        format.applyPattern("0.00");
        filterFormat.applyPattern("<0.00>");
    }

    @Test
    public void simple_output()
    {
        MarkupWriter writer = createMarkupWriter();
        ComponentResources resources = mockComponentResources();

        replay();

        Output component = new Output();

        component.setup(value, format, true, null, resources);

        writer.element("root");
        assertFalse(component.beginRender(writer));
        writer.end();

        verify();

        assertEquals(writer.toString(), "<root>22.70</root>");
    }

    @Test
    public void simple_output_with_filter()
    {
        MarkupWriter writer = createMarkupWriter();
        ComponentResources resources = mockComponentResources();

        replay();

        Output component = new Output();

        component.setup(value, filterFormat, true, null, resources);

        writer.element("root");
        assertFalse(component.beginRender(writer));
        writer.end();

        verify();

        assertEquals(writer.toString(), "<root>&lt;22.70&gt;</root>");
    }

    @Test
    public void simple_output_with_filter_disabled()
    {
        MarkupWriter writer = createMarkupWriter();
        ComponentResources resources = mockComponentResources();

        replay();

        Output component = new Output();

        component.setup(value, filterFormat, false, null, resources);

        writer.element("root");
        assertFalse(component.beginRender(writer));
        writer.end();

        verify();

        // It's not valid XML output, but that's why it's called programmer error :-)

        assertEquals(writer.toString(), "<root><22.70></root>");
    }

    @Test
    public void null_output()
    {
        MarkupWriter writer = createMarkupWriter();
        ComponentResources resources = mockComponentResources();

        replay();

        Output component = new Output();

        component.setup(null, format, true, null, resources);

        writer.element("root");
        assertFalse(component.beginRender(writer));
        writer.end();

        verify();

        assertEquals(writer.toString(), "<root></root>");
    }

    @Test
    public void output_with_element_and_informals()
    {
        String elementName = "span";

        MarkupWriter writer = createMarkupWriter();

        ComponentResources resources = mockComponentResources();

        train_renderInformalParameters(resources, writer, "foo", "bar");

        replay();

        Output component = new Output();

        component.setup(value, format, true, elementName, resources);

        assertFalse(component.beginRender(writer));

        verify();

        assertEquals(writer.toString(), "<span foo=\"bar\">22.70</span>");
    }

    @Test
    public void null_format_is_a_noop()
    {
        String elementName = "span";

        MarkupWriter writer = createMarkupWriter();

        ComponentResources resources = mockComponentResources();

        Format format = new Format()
        {
            private static final long serialVersionUID = -4360045992642727894L;

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos)
            {
                return toAppendTo;
            }

            @Override
            public Object parseObject(String source, ParsePosition pos)
            {
                return null;
            }
        };

        replay();

        Output component = new Output();

        component.setup(value, format, true, elementName, resources);

        writer.element("root");
        assertFalse(component.beginRender(writer));
        writer.end();

        verify();

        assertEquals(writer.toString(), "<root></root>");
    }
}
