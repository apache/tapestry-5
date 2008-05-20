// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.MarkupWriterListener;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.dom.XMLMarkupModel;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

public class MarkupWriterImplTest extends InternalBaseTestCase
{
    @Test(expectedExceptions = IllegalStateException.class)
    public void write_with_no_current_element()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.write("fail!");
    }

    @Test
    public void write_whitespace_before_start_of_root_element_is_ignored()
    {
        MarkupWriter w = new MarkupWriterImpl(new XMLMarkupModel());

        w.write("  ");

        w.element("root");
        w.end();

        assertEquals(w.toString(), "<?xml version=\"1.0\"?>\n<root/>");
    }

    @Test
    public void write_whitespace_after_end_of_root_element_is_ignored()
    {
        MarkupWriter w = new MarkupWriterImpl(new XMLMarkupModel());

        w.element("root");
        w.end();

        w.write("  ");

        assertEquals(w.toString(), "<?xml version=\"1.0\"?>\n<root/>");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void comment_with_no_current_element()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.comment("fail!");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void attribute_ns_with_no_current_element()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.attributeNS("foo", "bar", "baz");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void define_namespace_with_no_current_element()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.defineNamespace("foo", "bar");
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void end_with_no_current_element()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.end();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void attributes_with_no_current_element()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.attributes("fail", "now");
    }

    @Test
    public void current_element_at_end_of_root_element_is_null()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.element("root");

        assertNull(w.end());
    }

    @Test
    public void element_nesting()
    {
        MarkupWriter w = new MarkupWriterImpl();

        Element root = w.element("root");

        w.attributes("foo", "bar");

        w.write("before child");

        assertNotSame(w.element("nested"), root);

        w.write("inner text");

        assertSame(w.end(), root);

        w.write("after child");

        root.attribute("gnip", "gnop");

        assertEquals(w.toString(),
                     "<root foo=\"bar\" gnip=\"gnop\">before child<nested>inner text</nested>after child</root>");
    }

    @Test
    public void element_with_attributes()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.element("img", "src", "foo.png", "width", 20, "height", 20);
        w.end();

        // img is a tag with an end tag style of omit, so no close tag is written.

        assertEquals(w.toString(), "<img height=\"20\" src=\"foo.png\" width=\"20\">");
    }

    @Test
    public void attributes()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.element("root");

        w.attributes("foo", "bar", "gnip", "gnop");

        assertEquals(w.toString(), "<root foo=\"bar\" gnip=\"gnop\"></root>");
    }

    @Test
    public void comment()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.element("root");
        w.comment("A comment");
        w.end();

        assertEquals(w.toString(), "<root><!-- A comment --></root>");
    }

    @Test
    public void new_text_node_after_comment_node()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.element("root");
        w.write("before");
        w.comment("A comment");
        w.write("after");
        w.end();

        assertEquals(w.toString(), "<root>before<!-- A comment -->after</root>");
    }

    @Test
    public void null_write_is_ok()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.element("root");
        w.write(null);
        w.end();

        assertEquals(w.toString(), "<root></root>");
    }

    @Test
    public void writef()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.element("root");
        w.writef("Test name: %s", "writef");

        assertEquals(w.toString(), "<root>Test name: writef</root>");
    }


    @Test
    public void write_raw()
    {
        MarkupWriter w = new MarkupWriterImpl();

        w.element("root");
        w.write("<");
        w.writeRaw("&nbsp;");
        w.write(">");
        w.end();

        assertEquals(w.toString(), "<root>&lt;&nbsp;&gt;</root>");
    }

    @Test
    public void namespaced_elements_and_attributes()
    {
        MarkupWriter w = new MarkupWriterImpl(new XMLMarkupModel());

        Element root = w.elementNS("fredns", "root");

        assertSame(root.defineNamespace("fredns", "fred"), root);

        root.defineNamespace("barneyns", "barney");

        assertSame(w.attributeNS("fredns", "foo", "bar"), root);

        Element child = w.elementNS("barneyns", "child");

        assertSame(child.getParent(), root);

        w.end(); // child
        w.end(); // root

        assertEquals(w.toString(),
                     "<?xml version=\"1.0\"?>\n<fred:root fred:foo=\"bar\" xmlns:barney=\"barneyns\" xmlns:fred=\"fredns\"><barney:child/></fred:root>");
    }

    @Test
    public void cdata_content()
    {
        MarkupWriter w = new MarkupWriterImpl(new XMLMarkupModel());

        w.element("root");
        w.write("Normal Text ");
        w.cdata("< & >");
        w.write("More Normal Text");

        assertEquals(w.toString(),
                     "<?xml version=\"1.0\"?>\n<root>Normal Text <![CDATA[< & >]]>More Normal Text</root>");
    }

    @Test
    public void listeners()
    {
        MarkupWriter w = new MarkupWriterImpl(new XMLMarkupModel());

        MarkupWriterListener l = new MarkupWriterListener()
        {
            public void elementDidStart(Element element)
            {
                element.text("[Start: " + element.getName() + "]");
            }

            public void elementDidEnd(Element element)
            {
                element.text("[End: " + element.getName() + "]");
            }
        };

        w.element("root");
        w.element("no-listener");

        w.write("before listener");

        w.addListener(l);

        w.element("listener");
        w.write("before n-w-l");
        w.element("nested-with-listener");
        w.write("n-w-l text");
        w.end();
        w.write("after n-w-l");
        w.end();

        w.removeListener(l);

        w.write("after listener");

        w.end();
        w.end();

        // Because we are invoking Element.text(), the text added by the listener is appended to the body of the element,
        // which is correct but may not be what you'd expect.

        assertEquals(w.toString(), "<?xml version=\"1.0\"?>\n" +
                "<root><no-listener>before listener<listener>[Start: listener]before n-w-l<nested-with-listener>[Start: nested-with-listener]n-w-l text[End: nested-with-listener]</nested-with-listener>after n-w-l[End: listener]</listener>after listener</no-listener></root>");
    }
}
