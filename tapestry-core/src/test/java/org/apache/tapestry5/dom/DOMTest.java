// Copyright 2006-2012, 2026 The Apache Software Foundation
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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.services.MarkupWriterImpl;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for a number of DOM node classes, including {@link org.apache.tapestry5.dom.Element} and {@link
 * org.apache.tapestry5.dom.Document}.
 */
class DOMTest
{
    @Test
    void document_with_empty_root_element()
    {
        Document d = new Document();

        d.newRootElement("empty");

        assertEquals("<empty></empty>", d.toString());
    }

    @Test
    void xml_style_empty_element()
    {
        Document d = new Document(new XMLMarkupModel());

        d.newRootElement("empty");

        assertEquals("<?xml version=\"1.0\"?>\n<empty/>", d.toString());
    }

    @Test
    void namespaced_elements() throws Exception
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fredns", "root");

        root.defineNamespace("fredns", "f");
        root.defineNamespace("barneyns", "b");

        Element nested = root.elementNS("fredns", "nested");

        nested.elementNS("barneyns", "deepest");

        assertEquals(readFile("namespaced_elements.txt"), d.toString());
    }

    @Test
    void quote_using_apostrophes() throws Exception
    {
        Document d = new Document(new XMLMarkupModel(true));

        Element root = d.newRootElement("fredns", "root");

        root.defineNamespace("fredns", "f");
        root.defineNamespace("barneyns", "b");

        Element nested = root.elementNS("fredns", "nested");

        nested.attribute("attribute", "value");

        nested.elementNS("barneyns", "deepest");

        assertEquals(readFile("quote_using_apostrophes.txt"), d.toString());
    }

    @Test
    void namespace_element_without_a_prefix() throws Exception
    {

        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fredns", "root");

        Element child = root.element("child");

        Element barney = child.elementNS("barneyns", "barney");

        barney.attribute("simple", "a");
        barney.defineNamespace("bettyns", "betty");
        barney.attribute("bettyns", "betty", "b");
        barney.attribute("wilmans", "wilma", "c");

        assertEquals(readFile("namespace_element_without_a_prefix.txt"), d.toString());
    }

    @Test
    void default_namespace()
    {
        Document d = new Document(new XMLMarkupModel());

        String namespaceURI = "http://foo.com";

        Element root = d.newRootElement(namespaceURI, "root");

        root.defineNamespace(namespaceURI, "");
        root.attribute(namespaceURI, "gnip", "gnop");


        assertEquals("<?xml version=\"1.0\"?>\n<root gnip=\"gnop\" xmlns=\"http://foo.com\"/>", d.toString());
    }

    /**
     * Also demonstrates that attributes are provided in alphabetical order.
     */
    @Test
    void document_with_root_element_and_attributes() throws Exception
    {
        Document d = new Document();

        Element e = d.newRootElement("has-attributes");

        e.attribute("fred", "flintstone");
        e.attribute("barney", "rubble");

        assertEquals(readFile("document_with_root_element_and_attributes.txt"), d.toString());
    }

    @Test
    void nested_elements() throws Exception
    {
        Document d = new Document();

        Element e = d.newRootElement("population");

        Element p = e.element("person");
        p.attribute("first-name", "Fred");
        p.attribute("last-name", "Flintstone");

        assertSame(e, p.getContainer());

        p = e.element("person");
        p.attribute("first-name", "Barney");
        p.attribute("last-name", "Rubble");

        assertSame(e, p.getContainer());

        assertEquals(readFile("nested_elements.txt"), d.toString());
    }

    @Test
    void attribute_names_may_not_be_blank()
    {
        Document d = new Document();

        Element e = d.newRootElement("fred");

        assertThrows(AssertionError.class, () -> e.attribute("", "value"));
    }

    @Test
    void element_name_may_not_be_blank()
    {
        Document d = new Document();

        d.newRootElement("");
    }

    @Test
    void attribute_value_null_is_no_op()
    {
        Document d = new Document();

        Element e = d.newRootElement("root");

        e.attribute("foo", "bar");

        final String expected = "<root foo=\"bar\"></root>";

        assertEquals(expected, d.toString());

        e.attribute("foo", null);

        assertEquals(expected, d.toString());

        e.attribute("gnip", null);

        assertEquals(expected, d.toString());
    }

    @Test
    void comments() throws Exception
    {
        Document d = new Document();

        // Can't add comments to the document, not yet.

        Element e = d.newRootElement("html");

        e.comment(" Created by Tapestry 5.0 ");

        assertEquals("<html><!-- Created by Tapestry 5.0 --></html>", d.toString());
    }

    @Test
    void text()
    {
        Document d = new Document();

        Element e = d.newRootElement("body");

        e.text("Tapestry does DOM.");

        assertEquals("<body>Tapestry does DOM.</body>", d.toString());
    }

    @Test
    void text_with_control_characters()
    {
        Document d = new Document();

        Element e = d.newRootElement("root");

        e.text("<this> & <that>");

        assertEquals("<root>&lt;this&gt; &amp; &lt;that&gt;</root>", d.toString());
    }

    @Test
    void specify_attributes_with_new_element()
    {
        Document d = new Document();

        Element e = d.newRootElement("root");

        e.element("foo", "alpha", "legion");

        assertEquals("<root><foo alpha=\"legion\"></foo></root>", d.toString());
    }

    @Test
    void writef_with_text()
    {
        Document d = new Document();

        Element e = d.newRootElement("root");

        Text t = e.text("Start: ");

        t.writef("** %s: %d **", "foo", 5);

        assertEquals("<root>Start: ** foo: 5 **</root>", d.toString());
    }

    @Test
    void get_element_by_id()
    {
        Document d = new Document();
        Element e = d.newRootElement("root");
        Element e1 = e.element("e1", "id", "x");
        Element e2 = e.element("e2", "id", "y");
        assertSame(e1, e1.getElementById("x"));
        assertSame(e2, e.getElementById("y"));
        assertNull(e.getElementById("z"));
    }

    @Test
    void get_child_markup()
    {
        Document d = new Document();
        Element e0 = d.newRootElement("root");
        Element e1 = e0.element("e1");
        e1.text("123");
        assertEquals("123", e1.getChildMarkup());
        assertEquals("<e1>123</e1>", e0.getChildMarkup());
    }

    @Test
    void document_find_no_root_element()
    {
        Document d = new Document();

        assertNull(d.find("does/not/matter"));
    }

    @Test
    void document_find_not_a_match()
    {
        Document d = new Document();

        d.newRootElement("fred");

        assertNull(d.find("barney"));
        assertNull(d.find("wilma/betty"));
    }

    @Test
    void document_find_root_is_match()
    {
        Document d = new Document();

        Element root = d.newRootElement("fred");

        assertSame(root, d.find("fred"));
    }

    @Test
    void document_find_match()
    {
        Document d = new Document();

        Element root = d.newRootElement("fred");

        root.text("text");
        Element barney = root.element("barney");
        Element bambam = barney.element("bambam");

        assertSame(bambam, d.find("fred/barney/bambam"));
        assertSame(bambam, root.find("barney/bambam"));
    }

    @Test
    void document_find_no_match()
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
    void insert_element_at()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fred");

        root.element("start");
        root.element("end");

        root.elementAt(1, "one").element("tiny");
        root.elementAt(2, "two").element("bubbles");

        assertEquals(
                "<?xml version=\"1.0\"?>\n<fred><start/><one><tiny/></one><two><bubbles/></two><end/></fred>", d.toString());
    }

    @Test
    void force_attributes_overrides_existing()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fred");

        root.attributes("hi", "ho", "gnip", "gnop");

        assertEquals("<fred gnip=\"gnop\" hi=\"ho\"/>", root.toString());

        root.forceAttributes("hi", "bit", "gnip", null);

        assertEquals("<fred hi=\"bit\"/>", root.toString());
    }

    /**
     * TAP5-708
     */
    @Test
    void namespace_element_force_attributes_overrides_existing()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fredns", "fred");

        root.attributes("hi", "ho", "gnip", "gnop");

        assertEquals("<fred gnip=\"gnop\" hi=\"ho\" xmlns=\"fredns\"/>", root.toString());

        root.forceAttributes("hi", "bit", "gnip", null);

        assertEquals("<fred hi=\"bit\" xmlns=\"fredns\"/>", root.toString());
    }


    @Test
    void raw_output()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("fred");

        Element em = root.element("em");

        em.text("<");
        em.raw("&nbsp;");
        em.text(">");

        // The '<' and '>' are filtered into entities, but the '&' in &nbsp; is left alone (left
        // raw).

        assertEquals("<fred><em>&lt;&nbsp;&gt;</em></fred>", root.toString());
    }

    @Test
    void dtd_with_markup()
    {
        Document d = new Document(new XMLMarkupModel());
        Element root = d.newRootElement("prime");
        root.element("slag");
        d.dtd("prime", "-//TF", "tf");
        String expected = "<?xml version=\"1.0\"?>\n<!DOCTYPE prime PUBLIC \"-//TF\" \"tf\"><prime><slag/></prime>";
        assertEquals(expected, d.toString());
    }

    @Test
    void dtd_with_nullids()
    {
        Document d = new Document(new XMLMarkupModel());
        d.newRootElement("prime");

        d.dtd("prime", null, null);
        assertEquals("<?xml version=\"1.0\"?>\n<!DOCTYPE prime><prime/>", d.toString());

        d.dtd("prime", "-//TF", null);
        assertEquals("<?xml version=\"1.0\"?>\n<!DOCTYPE prime PUBLIC \"-//TF\"><prime/>", d.toString());

        d.dtd("prime", null, "tf");
        assertEquals("<?xml version=\"1.0\"?>\n<!DOCTYPE prime SYSTEM \"tf\"><prime/>", d.toString());
    }

    @Test
    void markup_characters_inside_attributes_are_escaped()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("prime");

        root.attribute("alpha-only", "abcdef");
        root.attribute("entities", "\"<>&");

        assertEquals("<prime entities=\"&quot;&lt;&gt;&amp;\" alpha-only=\"abcdef\"/>", root.toString());
    }

    @Test
    void apostrophes_are_escaped()
    {
        Document d = new Document(new XMLMarkupModel(true));

        Element root = d.newRootElement("prime");

        root.attribute("apostrophie", "some'thing");

        assertEquals("<prime apostrophie='some&#39;thing'/>", root.toString());
    }

    @Test
    void add_class_names()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("div");

        assertSame(root, root.addClassName("fred"));

        assertEquals("<div class=\"fred\"/>", root.toString());

        assertSame(root, root.addClassName("barney", "wilma"));

        assertEquals(stringSet("fred", "barney", "wilma"), extract(root, "class"));
    }

    // TAP5-2660
    @Test
    void add_class_names_empty_namespace()
    {
        Document d = new Document(new XMLMarkupModel());

        Element root = d.newRootElement("div");

        assertSame(root, root.attribute("", "class", "fred"));

        assertEquals("<div class=\"fred\"/>", root.toString());

        assertSame(root, root.addClassName("barney", "wilma"));

        assertEquals(stringSet("fred", "barney", "wilma"), extract(root, "class"));
    }

    private Set<String> extract(Element e, String attributeName)
    {
        String attribute = e.getAttribute(attributeName);

        return stringSet(attribute.split(" "));
    }

    private Set<String> stringSet(String... value) {
        return CollectionFactory.newSet(Arrays.asList(value));
    }

    /**
     * TAP5-804
     */
    @Test
    void namespace_add_class_name()
    {
        Document document = new Document(new DefaultMarkupModel());

        Element element = document.newRootElement("fredns", "e");

        element.attribute("class", "a");

        assertEquals("<e class=\"a\" xmlns=\"fredns\"></e>", element.toString());

        element.addClassName("b");

        assertEquals(stringSet("a", "b"), extract(element, "class"));
    }

    @Test
    void cdata_in_HTML_document()
    {
        Document d = new Document();

        d.newRootElement("root").cdata("This & That");

        // The '&' is expanded to an entity:

        assertEquals("<root>This &amp; That</root>", d.toString());
    }

    @Test
    void cdata_in_XML_document()
    {
        Document d = new Document(new XMLMarkupModel());

        d.newRootElement("root").cdata("This & That");

        // The '&' is expanded to an entity:

        assertEquals("<?xml version=\"1.0\"?>\n<root><![CDATA[This & That]]></root>", d.toString());
    }

    @Test
    void encoding_specified()
    {
        Document d = new Document(new XMLMarkupModel(), "utf-8");
        d.newRootElement("root");

        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<root/>", d.toString());
    }

    @Test
    void move_before()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        root.element("placeholder");

        Element target = root.element("target");
        Element mobile = root.element("source").element("mobile");

        mobile.text("On the move");

        assertEquals(
                "<doc><placeholder></placeholder><target></target><source><mobile>On the move</mobile></source></doc>", d.toString());


        mobile.moveBefore(target);

        assertEquals(
                "<doc><placeholder></placeholder><mobile>On the move</mobile><target></target><source></source></doc>", d.toString());
    }

    @Test
    void move_after()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");
        root.element("placeholder");

        Element target = root.element("target");
        Element mobile = root.element("source").element("mobile");

        mobile.text("On the move");

        assertEquals(
                "<doc><placeholder></placeholder><target></target><source><mobile>On the move</mobile></source></doc>", d.toString());


        mobile.moveAfter(target);

        assertEquals(
                "<doc><placeholder></placeholder><target></target><mobile>On the move</mobile><source></source></doc>", d.toString());
    }

    @Test
    void move_to_top()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        Element target = root.element("target");
        target.element("placeholder");
        Element mobile = root.element("source").element("mobile");

        mobile.text("On the move");

        assertEquals(
                "<doc><target><placeholder></placeholder></target><source><mobile>On the move</mobile></source></doc>", d.toString());

        mobile.moveToTop(target);

        assertEquals(
                "<doc><target><mobile>On the move</mobile><placeholder></placeholder></target><source></source></doc>", d.toString());
    }

    @Test
    void move_to_bottom()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        Element target = root.element("target");
        target.element("placeholder");
        Element mobile = root.element("source").element("mobile");

        mobile.text("On the move");

        assertEquals(
                "<doc><target><placeholder></placeholder></target><source><mobile>On the move</mobile></source></doc>", d.toString());

        mobile.moveToBottom(target);

        assertEquals(
                "<doc><target><placeholder></placeholder><mobile>On the move</mobile></target><source></source></doc>", d.toString());
    }

    @Test
    void remove_children()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        root.element("before");
        Element source = root.element("source");
        Element mobile = source.element("mobile");
        source.element("grok");
        root.element("after");

        mobile.text("On the move");

        assertEquals(
                "<doc><before></before><source><mobile>On the move</mobile><grok></grok></source><after></after></doc>", d.toString());

        source.removeChildren();

        assertEquals(
                "<doc><before></before><source></source><after></after></doc>", d.toString());
    }

    @Test
    void pop()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        Element source = root.element("source");
        source.element("mobile").text("On the move");
        source.element("grok");

        assertEquals(
                "<doc><source><mobile>On the move</mobile><grok></grok></source></doc>", d.toString());

        source.pop();

        assertEquals(
                "<doc><mobile>On the move</mobile><grok></grok></doc>", d.toString());
    }

    @Test
    void move_an_node_into_itself()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        Element target = root.element("target");
        target.element("placeholder");
        Element mobile = root.element("source").element("mobile");
        mobile.text("On the move");
        Element inside = mobile.element("inside");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mobile.moveToTop(inside));
        assertEquals("Unable to move a node relative to itself.", ex.getMessage());
    }

    @Test
    void wrap()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        Element target = root.element("target");
        target.element("placeholder");
        Element mobile = root.element("source").element("mobile");

        Node text = mobile.text("On the move");

        assertEquals(
                "<doc><target><placeholder></placeholder></target><source><mobile>On the move</mobile></source></doc>", d.toString());

        text.wrap("em", "class", "bold");

        assertEquals(
                "<doc><target><placeholder></placeholder></target><source><mobile><em class=\"bold\">On the move</em></mobile></source></doc>", d.toString());
    }

    /**
     * TAP5-385
     */
    @Test
    void empty_html_elements()
    {
        Document d = new Document();

        Element root = d.newRootElement("doc");

        root.element("hr");
        root.element("br");
        root.element("img");

        assertEquals("<doc><hr/><br/><img/></doc>", d.toString());
    }

    /**
     * TAP5-402
     */
    @Test
    void is_empty()
    {
        Document d = new Document();

        Element root = d.newRootElement("root");

        assertTrue(root.isEmpty());

        root.text("");

        assertTrue(root.isEmpty());

        root.text("  ");

        assertTrue(root.isEmpty());

        Element child = root.element("child");

        assertFalse(root.isEmpty());

        assertTrue(child.isEmpty());

        child.text("not empty");

        assertFalse(child.isEmpty());
    }

    /**
     * TAP5-457
     */
    @Test
    void defaults_for_xml_defined_namespaces() throws Exception
    {
        Document d = new Document();

        String XHTML_NAMESPACE = "http://www.w3.org/1999/xhtml";

        Element root = d.newRootElement(XHTML_NAMESPACE, "html");

        root.attribute(Document.XML_NAMESPACE_URI, "lang", "de");

        // Before TAP5-457, it would be ns0: not xml:

        assertEquals(readFile("defaults_for_xml_defined_namespaces.txt"), d.toString());
    }

    @Test
    void visit_order()
    {
        Document d = new Document();

        Element root = d.newRootElement("parent");

        Element child1 = root.element("child1");
        Element child2 = root.element("child2");

        child1.element("child1a");
        child1.text("Does not affect traversal");
        child1.element("child1b");

        child2.element("child2a");
        child2.element("child2b");
        child2.element("child2c");

        final List<String> elementNames = CollectionFactory.newList();

        d.visit(new Visitor()
        {
            public void visit(Element element)
            {
                elementNames.add(element.getName());
            }
        });

        assertIterableEquals(Arrays.asList("parent", "child1", "child1a", "child1b", "child2", "child2a", "child2b",
                "child2c"), elementNames);
    }

    // --- NodeVisitor ---

    @Test
    void nodeVisitor_visits_all_node_types_in_document_order()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        root.text("hello");
        root.comment(" c ");
        Element nested = root.element("nested");
        nested.cdata("data");
        nested.raw("<x/>");

        List<String> visits = CollectionFactory.newList();

        root.visit(new NodeVisitor()
        {
            @Override public void visit(Element e)  { visits.add("Element:" + e.getName()); }
            @Override public void visit(Text t)     { visits.add("Text"); }
            @Override public void visit(Comment c)  { visits.add("Comment"); }
            @Override public void visit(CData c)    { visits.add("CData"); }
            @Override public void visit(Raw r)      { visits.add("Raw"); }
        });

        assertIterableEquals(Arrays.asList("Element:root", "Text", "Comment", "Element:nested", "CData", "Raw"), visits);
    }

    @Test
    void nodeVisitor_default_methods_allow_selective_override()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        root.text("first");
        root.element("child").text("second");

        List<String> textContents = CollectionFactory.newList();

        // Only override visit(Text) — elements are silently skipped
        root.visit(new NodeVisitor()
        {
            @Override
            public void visit(Text t) { textContents.add(t.toString()); }
        });

        assertIterableEquals(Arrays.asList("first", "second"), textContents);
    }

    @Test
    void nodeVisitor_on_document_delegates_to_root_element()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        root.element("child");

        List<String> names = CollectionFactory.newList();

        d.visit(new NodeVisitor()
        {
            @Override
            public void visit(Element e) { names.add(e.getName()); }
        });

        assertIterableEquals(Arrays.asList("root", "child"), names);
    }

    /**
     * TAP5-559
     */
    @Test
    void later_updates_to_same_attribute_are_ignored()
    {
        Document d = new Document();

        Element root = d.newRootElement("parent");

        root.attribute("baggins", "bilbo");

        // This will be ignored.

        root.attribute("baggins", "frodo");


        assertEquals("<parent baggins=\"bilbo\"></parent>", d.toString());
    }

    @Test
    void force_attributes_changes_attribute_value()
    {
        Document d = new Document();


        Element root = d.newRootElement("parent");

        root.attribute("baggins", "bilbo");

        // This will be ignored.

        root.forceAttributes("baggins", "frodo");


        assertEquals("<parent baggins=\"frodo\"></parent>", d.toString());
    }

    @Test
    void force_attributes_to_null_removes_attribute()
    {
        Document d = new Document();


        Element root = d.newRootElement("parent");

        root.attributes("baggins", "frodo",
                "friend", "sam");

        root.forceAttributes("friend", null);

        assertEquals("<parent baggins=\"frodo\"></parent>", root.toString());

        root.forceAttributes("baggins", null,
                "enemy", "gollum");

        assertEquals("<parent enemy=\"gollum\"></parent>", root.toString());
    }

    @Test
    void get_attributes()
    {
        Document d = new Document();

        Element root = d.newRootElement("parent");

        assertTrue(root.getAttributes().isEmpty());

        root.attribute("fred", "flintstone");

        Collection<Attribute> attributes = root.getAttributes();

        assertEquals(1, attributes.size());

        Attribute attribute = attributes.iterator().next();

        assertEquals("fred", attribute.getName());
        assertEquals("flintstone", attribute.getValue());
    }

    /**
     * TAP5-636
     */
    @Test
    void force_null_for_first_attribute_is_noop()
    {
        Document d = new Document();

        Element root = d.newRootElement("root");

        root.forceAttributes("null", null);

        assertEquals("<root></root>", root.toString());
    }

    @Test
    void remove_while_rendering()
    {
        MarkupWriter writer = new MarkupWriterImpl(new XMLMarkupModel());

        writer.element("ul");

        for (int i = 0; i < 4; i++)
        {
            Element e = writer.element("li");

            if (i != 2)
            {
                writer.write(String.valueOf(i));
            }

            writer.end();

            if (e.getChildren().isEmpty())
            {
                e.remove();
            }
        }

        writer.end();

        assertEquals("<?xml version=\"1.0\"?>\n" +
                "<ul><li>0</li><li>1</li><li>3</li></ul>", writer.toString());
    }
    
    /**
     * TAP5-2071
     */
    @Test
    void html5_void_elements()
    {
        final List<String> voidElements = CollectionFactory.newList("area", "base", "br", "col",
                "command", "embed", "hr", "img", "input", "keygen", "link", "meta", "param",
                "source", "track", "wbr");
        
        MarkupWriter writer = new MarkupWriterImpl(new Html5MarkupModel());
        
        writer.element("html");
        
        for(String element : voidElements)
        {
            writer.element(element);
            writer.end();
        }
        
        writer.end();
        
        assertEquals(
                "<html><area><base><br><col><command><embed><hr><img><input><keygen><link><meta><param><source><track><wbr></html>", writer.toString());
    }

    // --- Deep clone ---

    @Test
    void element_deep_clone_is_detached()
    {
        Document d = new Document();
        Element src = d.newRootElement("root").element("section");

        assertNull(src.deepClone().getContainer());
    }

    @Test
    void element_deep_clone_preserves_name()
    {
        Document d = new Document();
        Element src = d.newRootElement("root").element("section");

        assertEquals("section", src.deepClone().getName());
    }

    @Test
    void element_deep_clone_copies_attributes()
    {
        Document d = new Document();
        Element src = d.newRootElement("root").element("div", "id", "foo", "class", "bar");

        Element clone = src.deepClone();

        assertEquals("foo", clone.getAttribute("id"));
        assertEquals("bar", clone.getAttribute("class"));
    }

    @Test
    void element_deep_clone_copies_children_recursively()
    {
        Document d = new Document();
        Element src = d.newRootElement("root").element("parent");
        src.element("child-a").element("grandchild");
        src.element("child-b");

        Element clone = src.deepClone();

        List<Node> children = clone.getChildren();
        assertEquals(2, children.size());
        assertEquals("child-a",   ((Element) children.get(0)).getName());
        assertEquals("child-b",   ((Element) children.get(1)).getName());
        assertEquals(1, ((Element) children.get(0)).getChildren().size()); // grandchild present
    }

    @Test
    void detached_node_toString_uses_default_markup_model()
    {
        Document d = new Document();
        Element src = d.newRootElement("root").element("p", "class", "note");
        src.text("Hello, ");
        src.element("strong").text("world");
        src.text("!");

        // Both src (attached, DefaultMarkupModel) and clone (detached, fallback DefaultMarkupModel)
        // must produce identical markup without any extra attachment step.
        Element clone = src.deepClone();

        assertEquals(src.toString(), clone.toString());
    }

    @Test
    void detached_node_getChildMarkup_uses_default_markup_model()
    {
        Document d = new Document();
        Element src = d.newRootElement("root").element("div");
        src.element("span").text("content");

        Element clone = src.deepClone();

        assertEquals(src.getChildMarkup(), clone.getChildMarkup());
    }

    @Test
    void detached_node_toMarkup_uses_provided_markup_model()
    {
        Document d = new Document();
        Element src = d.newRootElement("root").element("br");

        Element clone = src.deepClone();

        // Html5MarkupModel treats <br> as a void element (no closing tag or slash).
        // DefaultMarkupModel abbreviates it as a self-closing tag.
        assertEquals("<br>",  clone.toMarkup(new Html5MarkupModel()));
        assertEquals("<br/>", clone.toMarkup(new DefaultMarkupModel()));
    }

    @Test
    void element_deep_clone_is_independent_of_original()
    {
        Document d = new Document();
        Element src = d.newRootElement("root").element("div", "id", "original");

        Element clone = src.deepClone();

        // Mutate the original — clone must remain unchanged.
        src.forceAttributes("id", "changed");
        src.element("extra");

        assertEquals("original", clone.getAttribute("id"));
        assertEquals(0, clone.getChildren().size());
    }

    @Test
    void element_deep_clone_changes_do_not_affect_original()
    {
        Document d = new Document();
        Element src = d.newRootElement("root").element("div", "id", "original");

        // Mutate the clone — original must remain unchanged.
        Element clone = src.deepClone();
        Document wrapper = new Document();
        wrapper.newRootElement("wrapper").addChild(clone);
        clone.forceAttributes("id", "modified");
        clone.element("extra");

        assertEquals("original", src.getAttribute("id"));
        assertEquals(0, src.getChildren().size());
    }

    @Test
    void document_deep_clone_produces_same_markup()
    {
        Document d = new Document();
        Element root = d.newRootElement("html");
        Element body = root.element("body");
        body.element("h1", "class", "title").text("Hello");
        body.element("p").text("World");

        Document clone = d.deepClone();

        assertEquals(d.toString(), clone.toString());
    }

    @Test
    void document_deep_clone_is_independent()
    {
        Document d = new Document();
        d.newRootElement("root").element("child").text("original");

        Document clone = d.deepClone();

        // Empty the clone's tree.
        clone.getRootElement().removeChildren();

        // Original is unaffected.
        assertEquals(1, d.getRootElement().getChildren().size());
        assertTrue(clone.getRootElement().getChildren().isEmpty());
    }

    // --- Detach ---

    @Test
    void detach_removes_node_from_parent()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element child = root.element("child");

        child.detach();

        assertNull(child.getContainer());
        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    void detach_returns_the_same_instance()
    {
        Document d = new Document();
        Element child = d.newRootElement("root").element("child");

        assertSame(child, child.detach());
    }

    @Test
    void detach_is_noop_when_already_detached()
    {
        Document d = new Document();
        Element child = d.newRootElement("root").element("child");
        child.detach();

        // Second call must not throw.
        assertSame(child, child.detach());
        assertNull(child.getContainer());
    }

    @Test
    void detach_returns_element_without_cast()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element child = root.element("child", "id", "c");

        // detach() on Element has a covariant return type of Element, so Element-specific
        // methods (like getAttribute) can be called directly without a cast.
        String id = child.detach().getAttribute("id");

        assertEquals("c", id);
        assertTrue(root.getChildren().isEmpty());
    }

    // --- replaceWith ---

    @Test
    void replaceWith_swaps_node_in_place()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element a = root.element("a");
        Element b = root.element("b");
        Element c = root.element("c");

        Element newB = new Element((Element) null, null, "x");
        // Attach newB to a temp parent so we can verify detach
        Document tmp = new Document();
        Element tmpRoot = tmp.newRootElement("tmp");
        tmpRoot.addChild(newB);

        Node result = b.replaceWith(newB);

        assertSame(newB, result);
        // root now contains a, newB, c in order
        List<Node> children = root.getChildren();
        assertEquals(3, children.size());
        assertSame(a,    children.get(0));
        assertSame(newB, children.get(1));
        assertSame(c,    children.get(2));
        // b is now detached
        assertNull(b.getContainer());
        // newB was detached from tmpRoot
        assertTrue(tmpRoot.getChildren().isEmpty());
    }

    @Test
    void replaceWith_detaches_replacement_from_prior_parent()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element target = root.element("target");

        Document d2 = new Document();
        Element root2 = d2.newRootElement("root2");
        Element mover = root2.element("mover");

        target.replaceWith(mover);

        // After replaceWith, mover's container is root (not root2)
        assertEquals("root", mover.getContainer().getName());
        assertTrue(root2.getChildren().isEmpty());
    }

    @Test
    void replaceWith_returns_replacement()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element old = root.element("old");
        Element fresh = root.element("fresh");
        // detach fresh so it's free to use as replacement
        fresh.detach();

        Node returned = old.replaceWith(fresh);
        assertSame(fresh, returned);
    }

    @Test
    void replaceWith_throws_for_null()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element child = root.element("child");
        assertThrows(IllegalArgumentException.class, () -> child.replaceWith(null));
    }

    @Test
    void replaceWith_throws_when_replacing_self()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element child = root.element("child");
        assertThrows(IllegalArgumentException.class, () -> child.replaceWith(child));
    }

    @Test
    void replaceWith_throws_when_node_is_detached()
    {
        Element detached = new Element((Element) null, null, "detached");
        Element other = new Element((Element) null, null, "other");
        assertThrows(IllegalStateException.class, () -> detached.replaceWith(other));
    }

    @Test
    void replaceWith_throws_when_replacement_is_ancestor()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element parent = root.element("parent");
        Element child = parent.element("child");
        // Replacing child with its ancestor (parent) would create a cycle
        assertThrows(IllegalArgumentException.class, () -> child.replaceWith(parent));
    }

    // --- insertBefore / insertAfter ---

    @Test
    void insertBefore_places_node_before_anchor()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element a = root.element("a");
        Element b = root.element("b");
        Element c = root.element("c");

        Element x = new Element((Element) null, null, "x");
        Document tmp = new Document();
        tmp.newRootElement("tmp").appendChild(x);

        b.insertBefore(x);

        List<Node> children = root.getChildren();
        assertEquals(4, children.size());
        assertSame(a, children.get(0));
        assertSame(x, children.get(1));
        assertSame(b, children.get(2));
        assertSame(c, children.get(3));
    }

    @Test
    void insertBefore_detaches_from_prior_parent()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element anchor = root.element("anchor");

        Document d2 = new Document();
        Element other = d2.newRootElement("other");
        Element mover = other.element("mover");

        anchor.insertBefore(mover);

        assertTrue(other.getChildren().isEmpty());
        assertEquals("root", mover.getContainer().getName());
    }

    @Test
    void insertBefore_works_with_non_element_anchor()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Text text = root.text("hello");
        Element after = root.element("after");

        Element inserted = new Element((Element) null, null, "span");
        Node result = text.insertBefore(inserted);

        assertSame(inserted, result);
        List<Node> children = root.getChildren();
        assertSame(inserted, children.get(0));
        assertSame(text,     children.get(1));
        assertSame(after,    children.get(2));
    }

    @Test
    void insertBefore_returns_inserted_node()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element anchor = root.element("anchor");
        Element fresh = new Element((Element) null, null, "fresh");

        Node returned = anchor.insertBefore(fresh);
        assertSame(fresh, returned);
    }

    @Test
    void insertBefore_throws_for_null()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element child = root.element("child");
        assertThrows(IllegalArgumentException.class, () -> child.insertBefore(null));
    }

    @Test
    void insertBefore_throws_for_self()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element child = root.element("child");
        assertThrows(IllegalArgumentException.class, () -> child.insertBefore(child));
    }

    @Test
    void insertBefore_throws_when_anchor_is_detached()
    {
        Element detached = new Element((Element) null, null, "detached");
        Element other = new Element((Element) null, null, "other");
        assertThrows(IllegalStateException.class, () -> detached.insertBefore(other));
    }

    @Test
    void insertBefore_throws_when_node_is_ancestor()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element parent = root.element("parent");
        Element child = parent.element("child");
        assertThrows(IllegalArgumentException.class, () -> child.insertBefore(parent));
    }

    @Test
    void insertAfter_places_node_after_anchor()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element a = root.element("a");
        Element b = root.element("b");
        Element c = root.element("c");

        Element x = new Element((Element) null, null, "x");

        b.insertAfter(x);

        List<Node> children = root.getChildren();
        assertEquals(4, children.size());
        assertSame(a, children.get(0));
        assertSame(b, children.get(1));
        assertSame(x, children.get(2));
        assertSame(c, children.get(3));
    }

    @Test
    void insertAfter_detaches_from_prior_parent()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element anchor = root.element("anchor");

        Document d2 = new Document();
        Element other = d2.newRootElement("other");
        Element mover = other.element("mover");

        anchor.insertAfter(mover);

        assertTrue(other.getChildren().isEmpty());
        assertEquals("root", mover.getContainer().getName());
    }

    @Test
    void insertAfter_works_with_non_element_anchor()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element before = root.element("before");
        Text text = root.text("hello");

        Element inserted = new Element((Element) null, null, "span");
        Node result = text.insertAfter(inserted);

        assertSame(inserted, result);
        List<Node> children = root.getChildren();
        assertSame(before,   children.get(0));
        assertSame(text,     children.get(1));
        assertSame(inserted, children.get(2));
    }

    @Test
    void insertAfter_returns_inserted_node()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element anchor = root.element("anchor");
        Element fresh = new Element((Element) null, null, "fresh");

        Node returned = anchor.insertAfter(fresh);
        assertSame(fresh, returned);
    }

    @Test
    void insertAfter_throws_when_anchor_is_detached()
    {
        Element detached = new Element((Element) null, null, "detached");
        Element other = new Element((Element) null, null, "other");
        assertThrows(IllegalStateException.class, () -> detached.insertAfter(other));
    }

    @Test
    void insertAfter_throws_when_node_is_ancestor()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element parent = root.element("parent");
        Element child = parent.element("child");
        assertThrows(IllegalArgumentException.class, () -> child.insertAfter(parent));
    }

    // --- prependChild / appendChild ---

    @Test
    void prependChild_inserts_as_first_child()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element a = root.element("a");
        Element b = root.element("b");

        Element x = new Element((Element) null, null, "x");
        root.prependChild(x);

        List<Node> children = root.getChildren();
        assertSame(x, children.get(0));
        assertSame(a, children.get(1));
        assertSame(b, children.get(2));
    }

    @Test
    void prependChild_detaches_from_prior_parent()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");

        Document d2 = new Document();
        Element other = d2.newRootElement("other");
        Element mover = other.element("mover");

        root.prependChild(mover);

        assertTrue(other.getChildren().isEmpty());
        assertSame(root, mover.getContainer());
    }

    @Test
    void prependChild_returns_element_for_chaining()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element x = new Element((Element) null, null, "x");
        Element y = new Element((Element) null, null, "y");

        Element returned = root.prependChild(x);
        assertSame(root, returned);

        root.prependChild(y); // y is now first
        assertSame(y, root.getChildren().get(0));
        assertSame(x, root.getChildren().get(1));
    }

    @Test
    void prependChild_throws_for_null()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        assertThrows(IllegalArgumentException.class, () -> root.prependChild(null));
    }

    @Test
    void prependChild_throws_when_node_is_ancestor()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element child = root.element("child");
        assertThrows(IllegalArgumentException.class, () -> child.prependChild(root));
    }

    @Test
    void appendChild_appends_as_last_child()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element a = root.element("a");
        Element b = root.element("b");

        Element x = new Element((Element) null, null, "x");
        root.appendChild(x);

        List<Node> children = root.getChildren();
        assertSame(a, children.get(0));
        assertSame(b, children.get(1));
        assertSame(x, children.get(2));
    }

    @Test
    void appendChild_detaches_from_prior_parent()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");

        Document d2 = new Document();
        Element other = d2.newRootElement("other");
        Element mover = other.element("mover");

        root.appendChild(mover);

        assertTrue(other.getChildren().isEmpty());
        assertSame(root, mover.getContainer());
    }

    @Test
    void appendChild_returns_element_for_chaining()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element x = new Element((Element) null, null, "x");

        Element returned = root.appendChild(x);
        assertSame(root, returned);
    }

    @Test
    void appendChild_throws_for_null()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        assertThrows(IllegalArgumentException.class, () -> root.appendChild(null));
    }

    @Test
    void appendChild_throws_when_node_is_this()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element child = root.element("child");
        assertThrows(IllegalArgumentException.class, () -> child.appendChild(child));
    }

    // --- Sibling navigation ---

    @Test
    void next_sibling_returns_following_node()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element first = root.element("first");
        Text text = root.text("between");
        Element second = root.element("second");

        assertSame(text,   first.getNextSibling());
        assertSame(second, text.getNextSibling());
        assertNull(second.getNextSibling());
    }

    @Test
    void next_sibling_returns_null_for_root_element()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");

        assertNull(root.getNextSibling());
    }

    @Test
    void next_sibling_returns_null_for_detached_node()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element child = root.element("child");
        child.remove();

        assertNull(child.getNextSibling());
    }

    @Test
    void previous_sibling_returns_preceding_node()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element first = root.element("first");
        Text text = root.text("between");
        Element second = root.element("second");

        assertNull(first.getPreviousSibling());
        assertSame(first, text.getPreviousSibling());
        assertSame(text,  second.getPreviousSibling());
    }

    @Test
    void previous_sibling_returns_null_for_root_element()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");

        assertNull(root.getPreviousSibling());
    }

    @Test
    void previous_sibling_returns_null_for_detached_node()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element child = root.element("child");
        child.remove();

        assertNull(child.getPreviousSibling());
    }

    @Test
    void next_sibling_element_skips_non_element_nodes()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element first = root.element("first");
        root.text("between");
        root.comment("also between");
        Element second = root.element("second");

        assertSame(second, first.getNextSiblingElement());
        assertNull(second.getNextSiblingElement());
    }

    @Test
    void next_sibling_element_returns_null_for_root_element()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");

        assertNull(root.getNextSiblingElement());
    }

    @Test
    void previous_sibling_element_skips_non_element_nodes()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");
        Element first = root.element("first");
        root.text("between");
        root.comment("also between");
        Element second = root.element("second");

        assertNull(first.getPreviousSiblingElement());
        assertSame(first, second.getPreviousSiblingElement());
    }

    @Test
    void previous_sibling_element_returns_null_for_root_element()
    {
        Document d = new Document();
        Element root = d.newRootElement("root");

        assertNull(root.getPreviousSiblingElement());
    }

    // --- Helper methods formerly from InternalBaseTestCase ---

    private String readFile(String file) throws IOException
    {
        InputStream is = getClass().getResourceAsStream(file);
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is)))
        {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}