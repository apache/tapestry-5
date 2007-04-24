// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.dom;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for a number of DOM node classes, including {@link org.apache.tapestry.dom.Element} and
 * {@link org.apache.tapestry.dom.Document}.
 */
public class DOMTest extends Assert
{
    /**
     * Reads the content of a file into a string. Each line is trimmed of line separators and
     * leading/trailing whitespace.
     */
    private String readFile(String file) throws Exception
    {
        InputStream is = getClass().getResourceAsStream(file);
        is = new BufferedInputStream(is);
        Reader reader = new BufferedReader(new InputStreamReader(is));
        LineNumberReader in = new LineNumberReader(reader);

        StringBuilder buffer = new StringBuilder();

        while (true)
        {
            String line = in.readLine();

            if (line == null)
                break;

            buffer.append(line.trim());
        }

        in.close();

        return buffer.toString();
    }

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

        assertEquals(d.toString(), "<empty/>");
    }

    /** Also demonstrates that attributes are provided in alphabetical order. */
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
        assertEquals(e1.getChildText(), "123");
        assertEquals(e0.getChildText(), "<e1>123</e1>");
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

        assertEquals(
                d.toString(),
                "<fred><start/><one><tiny/></one><two><bubbles/></two><end/></fred>");
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
}
