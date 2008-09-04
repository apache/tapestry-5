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

package org.apache.tapestry5.dom;

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

/**
 * Tests for a number of DOM node classes, including {@link org.apache.tapestry5.dom.Element} and {@link
 * org.apache.tapestry5.dom.Document}.
 */
public class DOMTest extends InternalBaseTestCase
{
    @Test
    public void document_with_empty_root_element()
    {
        Document d = new Document();

        d.newRootElement("empty");

        assertEquals(d.toString(), "<empty></empty>");
    }

    @Test
    public void xml_style_empty_element()
    {
        Document d = new Document(new XMLMarkupModel());

        d.newRootElement("empty");

        assertEquals(d.toString(), "<?xml version=\"1.0\"?>\n<empty/>");
    }

    @Test
    public void namespaced_elements() throws Exception
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fredns", "root");

        root.defineNamespace("fredns", "f");
        root.defineNamespace("barneyns", "b");

        Element nested = root.elementNS("fredns", "nested");

        Element deepest = nested.elementNS("barneyns", "deepest");

        assertEquals(d.toString(), readFile("namespaced_elements.txt"));
    }

    @Test
    public void namespace_element_without_a_prefix()
    {

        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fredns", "root");

        try
        {
            d.toString();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Namespace prefix for URI 'fredns' is not defined.");
        }
    }

    @Test
    public void default_namespace()
    {
        Document d = new Document(new XMLMarkupModel());

        String namespaceURI = "http://foo.com";

        Element root = d.newRootElement(namespaceURI, "root");

        root.defineNamespace(namespaceURI, "");
        root.attribute(namespaceURI, "gnip", "gnop");


        assertEquals(d.toString(), "<?xml version=\"1.0\"?>\n<root gnip=\"gnop\" xmlns=\"http://foo.com\"/>");
    }

    /**
     * Also demonstrates that attributes are provided in alphabetical order.
     */
    @Test
    public void document_with_root_element_and_attributes() throws Exception
    {
        Document d = new Document();

        Element e = d.newRootElement("has-attributes");

        e.attribute("fred", "flintstone");
        e.attribute("barney", "rubble");

        assertEquals(d.toString(), readFile("document_with_root_element_and_attributes.txt"));
    }

    @Test
    public void nested_elements() throws Exception
    {
        Document d = new Document();

        Element e = d.newRootElement("population");

        Element p = e.element("person");
        p.attribute("first-name", "Fred");
        p.attribute("last-name", "Flintstone");

        assertSame(p.getParent(), e);

        p = e.element("person");
        p.attribute("first-name", "Barney");
        p.attribute("last-name", "Rubble");

        assertSame(p.getParent(), e);

        assertEquals(d.toString(), readFile("nested_elements.txt"));
    }

    @Test
    public void to_string_on_empty_document()
    {
        Document d = new Document();

        assertEquals(d.toString(), "[empty Document]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void attribute_names_may_not_be_blank()
    {
        Document d = new Document();

        Element e = d.newRootElement("fred");

        e.attribute("", "value");
    }

    @Test
    public void element_name_may_not_be_blank()
    {
        Document d = new Document();

        d.newRootElement("");
    }

    @Test
    public void attribute_value_null_is_no_op()
    {
        Document d = new Document();

        Element e = d.newRootElement("root");

        e.attribute("foo", "bar");

        final String expected = "<root foo=\"bar\"></root>";

        assertEquals(d.toString(), expected);

        e.attribute("foo", null);

        assertEquals(d.toString(), expected);

        e.attribute("gnip", null);

        assertEquals(d.toString(), expected);
    }

    @Test
    public void comments() throws Exception
    {
        Document d = new Document();

        // Can't add comments to the document, not yet.

        Element e = d.newRootElement("html");

        e.comment("Created by Tapestry 5.0");

        assertEquals(d.toString(), "<html><!-- Created by Tapestry 5.0 --></html>");
    }

    @Test
    public void text()
    {
        Document d = new Document();

        Element e = d.newRootElement("body");

        e.text("Tapestry does DOM.");

        assertEquals(d.toString(), "<body>Tapestry does DOM.</body>");
    }

    @Test
    public void text_with_control_characters()
    {
        Document d = new Document();

        Element e = d.newRootElement("root");

        e.text("<this> & <that>");

        assertEquals(d.toString(), "<root>&lt;this&gt; &amp; &lt;that&gt;</root>");
    }

    @Test
    public void specify_attributes_with_new_element()
    {
        Document d = new Document();

        Element e = d.newRootElement("root");

        e.element("foo", "alpha", "legion");

        assertEquals(d.toString(), "<root><foo alpha=\"legion\"></foo></root>");
    }

    @Test
    public void writef_with_text()
    {
        Document d = new Document();

        Element e = d.newRootElement("root");

        Text t = e.text("Start: ");

        t.writef("** %s: %d **", "foo", 5);

        assertEquals(d.toString(), "<root>Start: ** foo: 5 **</root>");
    }

    @Test
    public void get_element_by_id()
    {
        Document d = new Document();
        Element e = d.newRootElement("root");
        Element e1 = e.element("e1", "id", "x");
        Element e2 = e.element("e2", "id", "y");
        assertSame(e1.getElementById("x"), e1);
        assertSame(e.getElementById("y"), e2);
        assertNull(e.getElementById("z"));
    }

    @Test
    public void get_child_markup()
    {
        Document d = new Document();
        Element e0 = d.newRootElement("root");
        Element e1 = e0.element("e1");
        e1.text("123");
        assertEquals(e1.getChildMarkup(), "123");
        assertEquals(e0.getChildMarkup(), "<e1>123</e1>");
    }

    @Test
    public void document_find_no_root_element()
    {
        Document d = new Document();

        assertNull(d.find("does/not/matter"));
    }

    @Test
    public void document_find_not_a_match()
    {
        Document d = new Document();

        d.newRootElement("fred");

        assertNull(d.find("barney"));
        assertNull(d.find("wilma/betty"));
    }

    @Test
    public void document_find_root_is_match()
    {
        Document d = new Document();

        Element root = d.newRootElement("fred");

        assertSame(d.find("fred"), root);
    }

    @Test
    public void document_find_match()
    {
        Document d = new Document();

        Element root = d.newRootElement("fred");

        root.text("text");
        Element barney = root.element("barney");
        Element bambam = barney.element("bambam");

        assertSame(d.find("fred/barney/bambam"), bambam);
        assertSame(root.find("barney/bambam"), bambam);
    }

    @Test
    public void document_find_no_match()
    {
        Document d = new Document();

        Element root = d.newRootElement("fred");

        root.text("text");
        Element barney = root.element("barney");
        barney.element("bambam");

        assertNull(d.find("fred/barney/pebbles"));
        assertNull(root.find("barney/pebbles"));
    }

    @Test
    public void insert_element_at()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fred");

        root.element("start");
        root.element("end");

        root.elementAt(1, "one").element("tiny");
        root.elementAt(2, "two").element("bubbles");

        assertEquals(d.toString(),
                     "<?xml version=\"1.0\"?>\n<fred><start/><one><tiny/></one><two><bubbles/></two><end/></fred>");
    }

    @Test
    public void force_attributes_overrides_existing()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fred");

        root.attributes("hi", "ho", "gnip", "gnop");

        assertEquals(root.toString(), "<fred gnip=\"gnop\" hi=\"ho\"/>");

        root.forceAttributes("hi", "bit", "gnip", null);

        assertEquals(root.toString(), "<fred hi=\"bit\"/>");
    }

    @Test
    public void raw_output()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fred");

        Element em = root.element("em");

        em.text("<");
        em.raw("&nbsp;");
        em.text(">");

        // The '<' and '>' are filtered into entities, but the '&' in &nbsp; is left alone (left
        // raw).

        assertEquals(root.toString(), "<fred><em>&lt;&nbsp;&gt;</em></fred>");
    }

    @Test
    public void dtd_with_markup()
    {
        Document d = new Document(new XMLMarkupModel());
        Element root = d.newRootElement("prime");
        root.element("slag");
        d.dtd("prime", "-//TF", "tf");
        String expected = "<?xml version=\"1.0\"?>\n<!DOCTYPE prime PUBLIC \"-//TF\" \"tf\"><prime><slag/></prime>";
        assertEquals(d.toString(), expected);
    }

    @Test
    public void dtd_with_nullids()
    {
        Document d = new Document(new XMLMarkupModel());
        d.newRootElement("prime");
        d.dtd("prime", null, null);
        assertEquals(d.toString(), "<?xml version=\"1.0\"?>\n<prime/>");
        d.dtd("prime", "-//TF", null);
        assertEquals(d.toString(), "<?xml version=\"1.0\"?>\n<!DOCTYPE prime PUBLIC \"-//TF\"><prime/>");

        d.dtd("prime", null, "tf");
        assertEquals(d.toString(), "<?xml version=\"1.0\"?>\n<!DOCTYPE prime SYSTEM \"tf\"><prime/>");
    }

    @Test
    public void markup_characters_inside_attributes_are_escaped()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("prime");

        root.attribute("alpha-only", "abcdef");
        root.attribute("entities", "\"<>&");

        assertEquals(root.toString(), "<prime alpha-only=\"abcdef\" entities=\"&quot;&lt;&gt;&amp;\"/>");
    }

    @Test
    public void add_class_names()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("div");

        assertSame(root.addClassName("fred"), root);

        assertEquals(root.toString(), "<div class=\"fred\"/>");

        assertSame(root.addClassName("barney", "wilma"), root);

        assertEquals(root.toString(), "<div class=\"fred barney wilma\"/>");
    }

    @Test
    public void cdata_in_HTML_document()
    {
        Document d = new Document();

        d.newRootElement("root").cdata("This & That");

        // The '&' is expanded to an entity:

        assertEquals(d.toString(), "<root>This &amp; That</root>");
    }

    @Test
    public void cdata_in_XML_document()
    {
        Document d = new Document(new XMLMarkupModel());

        d.newRootElement("root").cdata("This & That");

        // The '&' is expanded to an entity:

        assertEquals(d.toString(), "<?xml version=\"1.0\"?>\n<root><![CDATA[This & That]]></root>");
    }

    @Test
    public void encoding_specified()
    {
        Document d = new Document(new XMLMarkupModel(), "utf-8");
        d.newRootElement("root");

        assertEquals(d.toString(), "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<root/>");
    }

    @Test
    public void move_before()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        root.element("placeholder");

        Element target = root.element("target");
        Element mobile = root.element("source").element("mobile");

        mobile.text("On the move");

        assertEquals(d.toString(),
                     "<doc><placeholder></placeholder><target></target><source><mobile>On the move</mobile></source></doc>");


        mobile.moveBefore(target);

        assertEquals(d.toString(),
                     "<doc><placeholder></placeholder><mobile>On the move</mobile><target></target><source></source></doc>");
    }

    @Test
    public void move_after()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");
        root.element("placeholder");

        Element target = root.element("target");
        Element mobile = root.element("source").element("mobile");

        mobile.text("On the move");

        assertEquals(d.toString(),
                     "<doc><placeholder></placeholder><target></target><source><mobile>On the move</mobile></source></doc>");


        mobile.moveAfter(target);

        assertEquals(d.toString(),
                     "<doc><placeholder></placeholder><target></target><mobile>On the move</mobile><source></source></doc>");
    }

    @Test
    public void move_to_top()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        Element target = root.element("target");
        target.element("placeholder");
        Element mobile = root.element("source").element("mobile");

        mobile.text("On the move");

        assertEquals(d.toString(),
                     "<doc><target><placeholder></placeholder></target><source><mobile>On the move</mobile></source></doc>");

        mobile.moveToTop(target);

        assertEquals(d.toString(),
                     "<doc><target><mobile>On the move</mobile><placeholder></placeholder></target><source></source></doc>");
    }

    @Test
    public void move_to_bottom()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        Element target = root.element("target");
        target.element("placeholder");
        Element mobile = root.element("source").element("mobile");

        mobile.text("On the move");

        assertEquals(d.toString(),
                     "<doc><target><placeholder></placeholder></target><source><mobile>On the move</mobile></source></doc>");

        mobile.moveToBottom(target);

        assertEquals(d.toString(),
                     "<doc><target><placeholder></placeholder><mobile>On the move</mobile></target><source></source></doc>");
    }

    @Test
    public void remove_children()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        root.element("before");
        Element source = root.element("source");
        Element mobile = source.element("mobile");
        source.element("grok");
        root.element("after");

        mobile.text("On the move");

        assertEquals(d.toString(),
                     "<doc><before></before><source><mobile>On the move</mobile><grok></grok></source><after></after></doc>");

        source.removeChildren();

        assertEquals(d.toString(),
                     "<doc><before></before><source></source><after></after></doc>");
    }

    @Test
    public void pop()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        Element source = root.element("source");
        source.element("mobile").text("On the move");
        source.element("grok");

        assertEquals(d.toString(),
                     "<doc><source><mobile>On the move</mobile><grok></grok></source></doc>");

        source.pop();

        assertEquals(d.toString(),
                     "<doc><mobile>On the move</mobile><grok></grok></doc>");
    }

    @Test
    public void move_an_node_into_itself()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        Element target = root.element("target");
        target.element("placeholder");
        Element mobile = root.element("source").element("mobile");
        mobile.text("On the move");
        Element inside = mobile.element("inside");

        try
        {
            mobile.moveToTop(inside);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Unable to move a node relative to itself.");
        }
    }

    @Test
    public void wrap()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        Element target = root.element("target");
        target.element("placeholder");
        Element mobile = root.element("source").element("mobile");

        Node text = mobile.text("On the move");

        assertEquals(d.toString(),
                     "<doc><target><placeholder></placeholder></target><source><mobile>On the move</mobile></source></doc>");

        text.wrap("em", "class", "bold");

        assertEquals(d.toString(),
                     "<doc><target><placeholder></placeholder></target><source><mobile><em class=\"bold\">On the move</em></mobile></source></doc>");
    }
}
