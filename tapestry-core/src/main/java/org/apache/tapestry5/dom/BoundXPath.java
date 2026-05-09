//  Copyright 2026 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry5.dom.xpath.XPath;
import org.apache.tapestry5.dom.xpath.XPathException;

/**
 * An {@link XPath} pre-bound to a context {@link Node}.
 * <p>
 * This allows fluent, node-first queries without repeating the context node:
 * <pre>
 * List&lt;Element&gt; items = element.xpath("ul/li").elements();
 * String text = document.xpath("id('header')").singleNormalizedDescendantText();
 * </pre>
 *
 * @since 5.10
 * @see Node#xpath(String)
 * @see XPath
 */
public class BoundXPath
{
    private final XPath xpath;

    private final Object contextNode;

    BoundXPath(XPath xpath, Object contextNode)
    {
        assert xpath != null;
        assert contextNode != null;

        this.xpath = xpath;
        this.contextNode = contextNode;
    }

    /**
     * Evaluates the XPath and returns all matching DOM nodes.
     *
     * @return list of matching {@link Node}s; never {@code null}
     * @throws XPathException if evaluation fails
     * @see XPath#selectNodes(Object)
     */
    public List<Node> nodes()
    {
        return xpath.selectNodes(contextNode);
    }

    /**
     * As {@link #nodes()} but assumes every result is an {@link Element}.
     *
     * @return list of matched {@link Element}s; never {@code null}
     * @throws XPathException if evaluation fails
     * @see XPath#selectElements(Object)
     */
    public List<Element> elements()
    {
        return xpath.selectElements(contextNode);
    }

    /**
     * As {@link #nodes()} but assumes every result is an {@link Attribute}.
     *
     * @return list of matched {@link Attribute}s; never {@code null}
     * @throws XPathException if evaluation fails
     * @see XPath#selectAttributes(Object)
     */
    public List<Attribute> attributes()
    {
        return xpath.selectAttributes(contextNode);
    }

    /**
     * Returns the first matching node, or {@code null} if there is no match.
     *
     * @return the first matched node, or {@code null}
     * @throws XPathException if evaluation fails
     * @see XPath#selectSingleNode(Object)
     */
    public Object singleNode()
    {
        return xpath.selectSingleNode(contextNode);
    }

    /**
     * As {@link #singleNode()} but assumes the result is an {@link Element}.
     *
     * @return the first matched {@link Element}, or {@code null}
     * @throws XPathException if evaluation fails
     * @see XPath#selectSingleElement(Object)
     */
    public Element singleElement()
    {
        return xpath.selectSingleElement(contextNode);
    }

    /**
     * Returns the XPath string-value of each matched node.
     *
     * @return list of string values; never {@code null}
     * @throws XPathException if evaluation fails
     * @see XPath#stringValuesOf(Object)
     */
    public List<String> stringValuesOf()
    {
        return xpath.stringValuesOf(contextNode);
    }

    /**
     * Returns {@link Element#getChildMarkup()} for each matched element.
     *
     * @return list of child-markup strings; never {@code null}
     * @throws XPathException if evaluation fails
     * @see XPath#selectElementsChildMarkup(Object)
     */
    public List<String> elementsChildMarkup()
    {
        return xpath.selectElementsChildMarkup(contextNode);
    }

    /**
     * Returns {@link Element#getAttribute(String)} for each matched element.
     * <p>
     * Unlike XPath attribute selection, elements that lack the attribute contribute
     * a {@code null} entry so the result list is always the same size as the matched
     * element list.
     *
     * @param attributeName name of the attribute to retrieve; must not be {@code null} or empty
     * @return list of attribute values, with {@code null} for missing attributes; never {@code null}
     * @throws IllegalArgumentException if {@code attributeName} is {@code null} or empty
     * @throws XPathException if evaluation fails
     * @see XPath#selectElementsAttribute(Object, String)
     */
    public List<String> elementsAttribute(String attributeName)
    {
        if (attributeName == null || attributeName.isEmpty())
        {
            throw new IllegalArgumentException("attributeName must not be null or empty");
        }

        return xpath.selectElementsAttribute(contextNode, attributeName);
    }

    /**
     * Selects a single node and concatenates all its descendant text, then normalises
     * whitespace via {@link XPath#normalizeText(String)}.
     *
     * @return normalised text string; never {@code null}
     * @throws XPathException if evaluation fails
     * @see XPath#singleNormalizedDescendantText(Object)
     */
    public String singleNormalizedDescendantText()
    {
        return xpath.singleNormalizedDescendantText(contextNode);
    }

    /**
     * For each matched node, concatenates all descendant text and normalises
     * whitespace via {@link XPath#normalizeText(String)}.
     *
     * @return list of normalised text strings, one per matched node; never {@code null}
     * @throws XPathException if evaluation fails
     * @see XPath#normalizedDescendantText(Object)
     */
    public List<String> normalizedDescendantText()
    {
        return xpath.normalizedDescendantText(contextNode);
    }
}
