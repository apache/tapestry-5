// Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry5.dom.XMLMarkupModel;
import org.apache.tapestry5.internal.services.MarkupWriterImpl;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

public class TextOutputTest extends InternalBaseTestCase
{
    @Test
    public void null_value_is_noop()
    {
        MarkupWriter writer = mockMarkupWriter();

        replay();

        TextOutput component = new TextOutput();

        component.beginRender(writer);

        verify();
    }

    @Test
    public void normal_output()
    {
        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        TextOutput component = new TextOutput();

        component.injectValue("Fred\nBarney\rWilma\r\nBetty\nBam-Bam\n");

        writer.element("div");
        component.beginRender(writer);
        writer.end();

        assertEquals(writer.toString(),
                     "<?xml version=\"1.0\"?>\n" + "<div><p>Fred</p><p>Barney</p><p>Wilma</p><p>Betty</p><p>Bam-Bam</p></div>");
    }
}
