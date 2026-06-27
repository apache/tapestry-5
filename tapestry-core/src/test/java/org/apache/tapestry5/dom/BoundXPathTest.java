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

package org.apache.tapestry5.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.apache.tapestry5.dom.xpath.XPathException;
import org.apache.tapestry5.test.PageTester;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BoundXPathTest {

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
        void selectsAllMatchingElements()
        {
            List<Element> children = page.xpath("id('child-elements')/*").elements();

            assertEquals(2, children.size());
            assertEquals("Div Child", children.get(0).getChildMarkup());
            assertEquals("Span Child", children.get(1).getChildMarkup());
        }

        @Test
        void selectsSingleElement()
        {
            Element div = page.xpath("id('single-div')").singleElement();

            assertEquals("single-div", div.getAttribute("id"));
        }

        @Test
        void selectsSingleElementFromElementContext()
        {
            Element childElements = page.xpath("id('child-elements')").singleElement();
            Element span = childElements.xpath("span").singleElement();

            assertEquals("Span Child", span.getChildMarkup());
        }

        @Test
        void selectsSingleElementReturnsNullWhenNotFound()
        {
            Element element = page.xpath("id('does-not-exist')").singleElement();

            assertNull(element);
        }
    }

    @Nested
    @DisplayName("Node selection")
    class NodeSelection
    {
        @Test
        void selectsCommentNodes()
        {
            List<Node> comments = page.xpath("id('comment-parent')//comment()").nodes();

            assertEquals(2, comments.size());
            assertEquals("<!-- First -->", comments.get(0).toString());
            assertEquals("<!-- Second -->", comments.get(1).toString());
        }

        @Test
        void selectsSingleNode()
        {
            Object node = page.xpath("id('single-div')").singleNode();

            assertEquals("single-div", ((Element) node).getAttribute("id"));
        }

        @Test
        void selectsSingleNodeReturnsNullWhenNotFound()
        {
            Object node = page.xpath("id('does-not-exist')").singleNode();

            assertNull(node);
        }
    }

    @Nested
    @DisplayName("Attribute selection")
    class AttributeSelection
    {
        @Test
        void selectsAllAttributesOfElement()
        {
            // Tapestry outputs attributes in reverse template order (chain-of-attributes implementation),
            // so the XPath attribute order here is: title, class, id.
            List<Attribute> attrs = page.xpath("id('attr-with-id')/@*").attributes();

            assertEquals(3, attrs.size());
            assertAttributeEquals("title", "First", attrs.get(0));
            assertAttributeEquals("class", "cls-first", attrs.get(1));
            assertAttributeEquals("id", "attr-with-id", attrs.get(2));
        }

        @Test
        void selectsAttributeValuePerElement()
        {
            // Elements without the requested attribute contribute null, keeping list size == element count.
            List<String> ids = page.xpath("id('attr-parent')/*").elementsAttribute("id");

            assertEquals(Arrays.asList("attr-with-id", null), ids);
        }
    }

    @Nested
    @DisplayName("String and text methods")
    class StringAndTextMethods
    {
        @Test
        void returnsStringValuesOfAttributes()
        {
            List<String> classes = page.xpath("id('attr-parent')//@class").stringValuesOf();

            assertIterableEquals(Arrays.asList("cls-first", "cls-second"), classes);
        }

        @Test
        void returnsChildMarkupPerElement()
        {
            List<String> markup = page.xpath("id('child-elements')/*").elementsChildMarkup();

            assertIterableEquals(Arrays.asList("Div Child", "Span Child"), markup);
        }

        @Test
        void returnsSingleNormalizedDescendantText()
        {
            // The span contains a deliberate double-space that normalization collapses.
            String text = page.xpath("id('mixed-text')").singleNormalizedDescendantText();

            assertEquals("before inner text after", text);
        }

        @Test
        void returnsNormalizedDescendantTextForMultipleNodes()
        {
            List<String> text = page.xpath("id('multi-text')/div").normalizedDescendantText();

            assertIterableEquals(Arrays.asList("left center right", "top middle bottom"), text);
        }
    }

    @Nested
    @DisplayName("Validation and exceptions")
    class ValidationAndExceptions
    {
        @Test
        void invalidExpressionThrowsXPathException()
        {
            assertThrows(XPathException.class, () -> page.xpath("^^^"));
        }

        @Test
        void nullExpressionThrowsIllegalArgumentException()
        {
            assertThrows(IllegalArgumentException.class, () -> page.xpath(null));
        }

        @Test
        void emptyExpressionThrowsIllegalArgumentException()
        {
            assertThrows(IllegalArgumentException.class, () -> page.xpath(""));
        }

        @Test
        void nullAttributeNameThrowsIllegalArgumentException()
        {
            assertThrows(IllegalArgumentException.class, () ->
                page.xpath("id('attr-parent')/*").elementsAttribute(null));
        }

        @Test
        void emptyAttributeNameThrowsIllegalArgumentException()
        {
            assertThrows(IllegalArgumentException.class, () ->
                page.xpath("id('attr-parent')/*").elementsAttribute(""));
        }
    }

    private void assertAttributeEquals(String expectedName, String expectedValue, Attribute attribute)
    {
        assertEquals(expectedName, attribute.getName());
        assertEquals(expectedValue, attribute.getValue());
    }
}
