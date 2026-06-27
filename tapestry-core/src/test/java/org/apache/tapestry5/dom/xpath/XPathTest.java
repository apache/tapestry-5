// Copyright 2026 The Apache Software Foundation
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

package org.apache.tapestry5.dom.xpath;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.dom.Attribute;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Node;
import org.apache.tapestry5.test.PageTester;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class XPathTest {

    private static Document page;

    @BeforeAll
    static void setup()
    {
        PageTester pageTester = new PageTester("org.apache.tapestry5.dom", "app");
        page = pageTester.renderPage("XPathTest");
        pageTester.shutdown();
    }

    @Nested
    @DisplayName("Element selection")
    class ElementSelection
    {
        @Test
        void testCanFindElementsById()
        {
            XPath xpath = XPath.of("id('single-div')");

            List<Element> container = xpath.selectElements(page);

            assertEquals(1, container.size());
            assertEquals("Single Div", container.get(0).getChildMarkup());
        }

        @Test
        void testCanFindElements()
        {
            XPath xpath = XPath.of("id('child-elements')/*");

            List<Element> children = xpath.selectElements(page);

            assertEquals(2, children.size());
            assertEquals("Div Child", children.get(0).getChildMarkup());
            assertEquals("Span Child", children.get(1).getChildMarkup());
        }

        @Test
        void testCanFindElementsByName()
        {
            XPath xpath = XPath.of("id('child-elements')/span");

            List<Element> children = xpath.selectElements(page);

            assertEquals(1, children.size());
            assertEquals("Span Child", children.get(0).getChildMarkup());
        }

        @Test
        void testCanFindElementsByContents()
        {
            XPath xpath = XPath.of("//*[.='Single Div']");

            List<Element> elements = xpath.selectElements(page);

            assertEquals(1, elements.size());
            assertEquals("single-div", elements.get(0).getAttribute("id"));
        }

        @Test
        void testCanFindChildren()
        {
            List<Element> children = XPath.of("id('tree')/*").selectElements(page);

            assertEquals(2, children.size());
            assertEquals("branch-a", children.get(0).getAttribute("id"));
            assertEquals("branch-b", children.get(1).getAttribute("id"));
        }

        @Test
        void testCanFindDescendents()
        {
            XPath xpath = XPath.of("id('tree')//*");

            List<Element> descendents = xpath.selectElements(page);

            assertEquals(5, descendents.size());
            assertEquals("branch-a",          descendents.get(0).getAttribute("id"));
            assertEquals("branch-a-leaf",     descendents.get(1).getAttribute("id"));
            assertEquals("branch-b",          descendents.get(2).getAttribute("id"));
            assertEquals("branch-b-leaf-one", descendents.get(3).getAttribute("id"));
            assertEquals("branch-b-leaf-two", descendents.get(4).getAttribute("id"));
        }

        @Test
        void testCanFindParents()
        {
            XPath xpath = XPath.of("id('branch-a-leaf')/parent::*");

            List<Element> parents = xpath.selectElements(page);

            assertEquals(1, parents.size());
            assertEquals("branch-a", parents.get(0).getAttribute("id"));
        }

        @Test
        void testSelectSingleElement()
        {
            XPath xpath = XPath.of("id('child-elements')/span");

            Element span = xpath.selectSingleElement(page);

            assertEquals("Span Child", span.getChildMarkup());
        }

        @Test
        void testSelectSingleElementReturnsNullWhenNotFound()
        {
            XPath xpath = XPath.of("id('does-not-exist')");

            Element element = xpath.selectSingleElement(page);

            assertNull(element);
        }
    }


    @Nested
    @DisplayName("Node selection")
    class NodeSelection
    {
        @Test
        void testCanFindDocument()
        {
            XPath xpath = XPath.of("/");

            List<Node> nodes = xpath.selectNodes(page);

            assertEquals(Arrays.asList(page), nodes);
        }

        @Test
        void testCanFindByTextNodeValue()
        {
            XPath xpath = XPath.of("//text()[.='Before Comment']");

            List<Node> nodes = xpath.selectNodes(page);

            assertEquals(1, nodes.size());
            assertEquals("comment-parent", nodes.get(0).getContainer().getAttribute("id"));
        }

        @Test
        void testCanFindComments()
        {
            XPath xpath = XPath.of("id('comment-parent')//comment()");

            List<Node> nodes = xpath.selectNodes(page);

            assertEquals(2, nodes.size());
            assertEquals("<!-- First -->",  nodes.get(0).toString());
            assertEquals("<!-- Second -->", nodes.get(1).toString());
        }

        @Test
        void testCanFindCommentByValue()
        {
            XPath xpath = XPath.of("id('comment-parent')//comment()[.=' First ']");

            List<Node> nodes = xpath.selectNodes(page);

            assertEquals(1, nodes.size());
            assertEquals("<!-- First -->", nodes.get(0).toString());
        }

        @Test
        void testCanFindTextNodesFromDocumentContext()
        {
            Document doc = new Document();
            Element root = doc.newRootElement("root");
            root.text("hello");
            root.comment("a comment");

            List<Node> textFromDoc     = XPath.of("//text()").selectNodes(doc);
            List<Node> commentFromDoc  = XPath.of("//comment()").selectNodes(doc);
            List<Node> textFromElem    = XPath.of("//text()").selectNodes(root);
            List<Node> commentFromElem = XPath.of("//comment()").selectNodes(root);

            assertEquals(1, textFromDoc.size(),    "//text() from doc");
            assertEquals(1, commentFromDoc.size(), "//comment() from doc");
            assertEquals(1, textFromElem.size(),   "//text() from element");
            assertEquals(1, commentFromElem.size(),"//comment() from element");
        }
    }

    @Nested
    @DisplayName("Attribute selection")
    class AttributeSelection
    {
        @Test
        void testCanFindAttributes()
        {
            XPath xpath = XPath.of("id('attr-with-id')/@*");

            List<Attribute> attributes = xpath.selectAttributes(page);

            // Slightly brittle test: XPath will return the attributes in document order.
            // Tapestry seems to output attributes in reverse order of the template
            // (probably because of its "chain of attributes" implementation).
            // This test will break if Tapestry starts outputting attributes in a different order.
            assertEquals(3, attributes.size());
            assertAttributeEquals("title", "First",       attributes.get(0));
            assertAttributeEquals("class", "cls-first",   attributes.get(1));
            assertAttributeEquals("id",    "attr-with-id", attributes.get(2));
        }

        @Test
        void testCanFindAttributesByName()
        {
            XPath xpath = XPath.of("id('attr-parent')//@class");

            List<Attribute> classes = xpath.selectAttributes(page);

            assertEquals(2, classes.size());
            assertAttributeEquals("class", "cls-first",  classes.get(0));
            assertAttributeEquals("class", "cls-second", classes.get(1));
        }

        @Test
        void testCanFindByAttributeValue()
        {
            XPath xpath = XPath.of("id('attr-parent')//@*[.='cls-first']");

            List<Attribute> elements = xpath.selectAttributes(page);

            assertEquals(1, elements.size());
            assertAttributeEquals("class", "cls-first", elements.get(0));
        }

        @Test
        void testSelectElementsAttribute()
        {
            XPath xpath = XPath.of("id('attr-parent')/*");

            List<String> ids = xpath.selectElementsAttribute(page, "id");

            assertEquals(Arrays.asList("attr-with-id", null), ids);
        }
    }

    @Nested
    @DisplayName("String/text methods")
    class StringTextMethods
    {
        @Test
        void testStringValuesOf()
        {
            XPath xpath = XPath.of("id('attr-parent')//@class");

            List<String> classes = xpath.stringValuesOf(page);

            assertIterableEquals(Arrays.asList("cls-first", "cls-second"), classes);
        }

        @Test
        void testSelectElementsChildMarkup()
        {
            XPath xpath = XPath.of("id('child-elements')/*");

            List<String> childMarkup = xpath.selectElementsChildMarkup(page);

            assertIterableEquals(Arrays.asList("Div Child", "Span Child"), childMarkup);
        }

        @Test
        void testSingleNormalizedDescendantText()
        {
            XPath xpath = XPath.of("id('mixed-text')");

            // The span contains a deliberate double-space that normalization collapses.
            String text = xpath.singleNormalizedDescendantText(page);

            assertEquals("before inner text after", text);
        }

        @Test
        void testNormalizedDescendantText()
        {
            XPath xpath = XPath.of("id('multi-text')/div");

            List<String> text = xpath.normalizedDescendantText(page);

            assertIterableEquals(Arrays.asList("left center right", "top middle bottom"), text);
        }

        @Test
        void testNormalizedTextTreatsNbspAsSpace()
        {
            Element element = page.getElementById("nbsp-text");

            assertEquals("", XPath.of("/").normalizeText(element.getChildMarkup()));
        }

        @Test
        void testNormalizedText()
        {
            XPath xpath = XPath.of("/");

            assertEquals("",    xpath.normalizeText(""));
            assertEquals("",    xpath.normalizeText(" "));
            assertEquals("a",   xpath.normalizeText("a"));
            assertEquals("a",   xpath.normalizeText(" a "));
            assertEquals("a b", xpath.normalizeText("a  b"));
        }
    }

    @Nested
    @DisplayName("Parent axis for non-Element nodes")
    class ParentAxisNonElementNodes
    {
        @Test
        void testParentOfTextNode()
        {
            XPath xpath = XPath.of("//text()[.='Before Comment']/parent::*");

            List<Element> parents = xpath.selectElements(page);

            assertEquals(1, parents.size());
            assertEquals("comment-parent", parents.get(0).getAttribute("id"));
        }

        @Test
        void testParentOfCommentNode() {
            // Both comments share the same parent div.
            // XPath node-sets deduplicate, so one result.
            XPath xpath = XPath.of("id('comment-parent')//comment()/parent::*");

            List<Element> parents = xpath.selectElements(page);

            assertEquals(1, parents.size());
            assertEquals("comment-parent", parents.get(0).getAttribute("id"));
        }

        @Test
        void testAncestorFromTextNode() {
            XPath xpath = XPath.of("//text()[.='Before Comment']/ancestor::body");

            List<Element> ancestors = xpath.selectElements(page);

            assertEquals(1, ancestors.size());
            assertEquals("body", ancestors.get(0).getName());
        }

        @Test
        void testParentOfRootElementIsDocument() {
            // The parent axis of the root <html> element must return the Document.
            XPath xpath = XPath.of("/html/parent::node()");

            List<Node> parents = xpath.selectNodes(page);

            assertEquals(1, parents.size());
            assertEquals(page, parents.get(0));
        }
    }

    @Nested
    @DisplayName("CData as text node")
    class CDataTextNode
    {
        @Test
        void testCDataIsVisibleAsTextNode()
        {
            Document doc = new Document();
            Element root = doc.newRootElement("root");
            root.cdata("hello cdata");
            root.text(" plain");

            XPath xpath = XPath.of("//text()");

            List<Node> textNodes = xpath.selectNodes(doc);

            assertEquals(2, textNodes.size());
        }

        @Test
        void testCDataTextValueIsMatchable()
        {
            Document doc = new Document();
            Element root = doc.newRootElement("root");
            root.cdata("cdata content");

            XPath xpath = XPath.of("//text()[.='cdata content']");

            List<Node> matched = xpath.selectNodes(doc);

            assertEquals(1, matched.size());
        }
    }

    @Nested
    @DisplayName("Validation/Exceptions")
    class ValidationExceptions
    {
        @Test
        void testXPathOfNullThrowsIllegalArgument()
        {
            assertThrows(IllegalArgumentException.class, () -> {
                XPath.of(null);
            });
        }

        @Test
        void testXPathOfEmptyThrowsIllegalArgument()
        {
            assertThrows(IllegalArgumentException.class, () -> {
                    XPath.of("");
            });
        }

        @Test
        void testSelectElementsAttributeNullNameThrowsIllegalArgument()
        {
            assertThrows(IllegalArgumentException.class, () -> {
                XPath.of("id('attr-parent')/*").selectElementsAttribute(page, null);
            });
        }

        @Test
        void testSelectElementsAttributeEmptyNameThrowsIllegalArgument() {
            assertThrows(IllegalArgumentException.class, () -> {
                XPath.of("id('attr-parent')/*").selectElementsAttribute(page, "");
            });
        }
    }

    private void assertAttributeEquals(String expectedName, String expectedValue, Attribute attribute)
    {
        assertEquals(expectedName, attribute.getName());
        assertEquals(expectedValue, attribute.getValue());
    }
}
