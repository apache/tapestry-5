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

package org.apache.tapestry5.dom.xpath;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.dom.Attribute;
import org.apache.tapestry5.dom.BoundXPath;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Node;
import org.jaxen.BaseXPath;
import org.jaxen.JaxenException;
import org.jaxen.function.StringFunction;

/**
 * XPath engine to navigate Tapestry's DOM.
 * <p>
 * Use the factory method {@link #of(String)} to create instances, then call one of
 * the {@code select*} methods with a {@link Document} or {@link Node} as the context.
 * <p>
 * For a node-first, fluent style without repeating the context, obtain a
 * {@link BoundXPath} via {@link Node#xpath(String)} instead.
 *
 * @since 5.10
 * @see Node#xpath(String)
 * @see BoundXPath
 */
public class XPath
{
    private final BaseXPath delegate;

    private static final XPath TEXT_DESCENDANTS = XPath.of("descendant::text()");

    private XPath(String expression) throws JaxenException
    {
        this.delegate = new BaseXPath(expression, DocumentNavigator.INSTANCE);
    }

    /**
     * Creates a new {@code XPath} for the given {@code expression}.
     *
     * @param expression XPath expression to compile; must not be {@code null} or empty
     * @return an {@code XPath} instance
     * @throws IllegalArgumentException if {@code expression} is {@code null} or empty
     * @throws XPathException if the expression cannot be parsed
     */
    public static XPath of(String expression)
    {
        if (expression == null || expression.isEmpty())
        {
            throw new IllegalArgumentException("XPath expression must not be null or empty");
        }

        try
        {
            return new XPath(expression);
        }
        catch (JaxenException e)
        {
            throw new XPathException("Invalid XPath expression: " + expression, e);
        }
    }

    /**
     * Evaluates this instance against {@code node} and returns all matching DOM
     * objects as Tapestry {@link Node} instances.
     *
     * @param node context {@link Document} or {@link Node} to evaluate against
     * @return list of matching nodes; never {@code null}
     * @throws XPathException if evaluation fails
     */
    @SuppressWarnings("unchecked")
    public List<Node> selectNodes(Object node)
    {
        try
        {
            return (List<Node>) delegate.selectNodes(node);
        }
        catch (JaxenException e)
        {
            throw new XPathException("XPath evaluation failed", e);
        }
    }

    /**
     * Uses {@link #selectNodes(Object)} but assumes every result is an {@link Element}.
     *
     * @param node context {@link Document} or {@link Node}
     * @return list of matched {@link Element}s, never {@code null}
     * @throws XPathException if evaluation fails
     */
    @SuppressWarnings("unchecked")
    public List<Element> selectElements(Object node)
    {
        try
        {
            return (List<Element>) delegate.selectNodes(node);
        }
        catch (JaxenException e)
        {
            throw new XPathException("XPath evaluation failed", e);
        }
    }

    /**
     * Uses {@link #selectNodes(Object)} but assumes every result is an {@link Attribute}.
     *
     * @param node context {@link Document} or {@link Node}
     * @return list of matched {@link Attribute}s, never {@code null}
     * @throws XPathException if evaluation fails
     */
    @SuppressWarnings("unchecked")
    public List<Attribute> selectAttributes(Object node)
    {
        try
        {
            return (List<Attribute>) delegate.selectNodes(node);
        }
        catch (JaxenException e)
        {
            throw new XPathException("XPath evaluation failed", e);
        }
    }

    /**
     * Evaluates this instance and converts each matched node to its XPath string-value.
     *
     * @param node context {@link Document} or {@link Node}
     * @return list of string values; never {@code null}
     * @throws XPathException if evaluation fails
     * @see <a href="http://www.w3.org/TR/xpath#dt-string-value">XPath Spec: string-value</a>
     * @see #normalizedDescendantText(Object)
     */
    public List<String> stringValuesOf(Object node)
    {
        try
        {
            List<?> resultNodes = delegate.selectNodes(node);

            List<String> resultStrings = new ArrayList<String>(resultNodes.size());

            for (Object result : resultNodes)
            {
                resultStrings.add(StringFunction.evaluate(result, DocumentNavigator.INSTANCE));
            }

            return resultStrings;
        }
        catch (JaxenException e)
        {
            throw new XPathException("XPath evaluation failed", e);
        }
    }

    /**
     * Returns {@link Element#getChildMarkup()} for each element matched by this instance.
     *
     * @param node context {@link Document} or {@link Node}
     * @return list of child-markup strings; never {@code null}
     * @throws XPathException if evaluation fails
     */
    public List<String> selectElementsChildMarkup(Object node)
    {
        List<Element> selectedNodes = selectElements(node);

        List<String> childMarkup = new ArrayList<String>(selectedNodes.size());

        for (Element element : selectedNodes)
        {
            childMarkup.add(element.getChildMarkup());
        }

        return childMarkup;
    }

    /**
     * Returns {@link Element#getAttribute(String)} for each element matched by this XPath.
     * <p>
     * Unlike using XPath attribute selection, elements that lack the attribute contribute a
     * {@code null} entry, so the result list is always the same size as the matched element list.
     *
     * @param node          context {@link Document} or {@link Node}
     * @param attributeName name of the attribute to retrieve from each matched element; must not be {@code null} or empty
     * @return list of attribute values, with {@code null} for missing attributes; never {@code null}
     * @throws IllegalArgumentException if {@code attributeName} is {@code null} or empty
     * @throws XPathException if evaluation fails
     */
    public List<String> selectElementsAttribute(Object node, String attributeName)
    {
        if (attributeName == null || attributeName.isEmpty())
        {
            throw new IllegalArgumentException("attributeName must not be null or empty");
        }

        List<Element> selectedNodes = selectElements(node);

        List<String> attributeValues = new ArrayList<String>(selectedNodes.size());

        for (Element element : selectedNodes)
        {
            attributeValues.add(element.getAttribute(attributeName));
        }

        return attributeValues;
    }

    /**
     * Uses {@link #selectSingleNode(Object)} but assumes the result is an {@link Element}.
     *
     * @param node context {@link Document} or {@link Node}
     * @return the first matched {@link Element}, or {@code null} if there is no match
     * @throws XPathException if evaluation fails
     */
    public Element selectSingleElement(Object node)
    {
        return (Element) selectSingleNode(node);
    }

    /**
     * Returns the first node matched by this instance, or {@code null} if there is no match.
     *
     * @param node context {@link Document} or {@link Node}
     * @return the first matched node, or {@code null}
     * @throws XPathException if evaluation fails
     */
    public Object selectSingleNode(Object node)
    {
        try
        {
            return delegate.selectSingleNode(node);
        }
        catch (JaxenException e)
        {
            throw new XPathException("XPath evaluation failed", e);
        }
    }

    /**
     * Selects a single node and concatenates all its descendant text nodes, then
     * normalizes whitespace via {@link #normalizeText(String)}.
     * <p>
     * Useful for getting an approximation of the text a browser would display.
     *
     * @param node context {@link Document} or {@link Node}
     * @return normalised descendant text; never {@code null}
     * @throws XPathException if evaluation fails
     * @see #normalizeText(String)
     */
    public String singleNormalizedDescendantText(Object node)
    {
        Object foundNode = selectSingleNode(node);

        List<String> textNodeValues = TEXT_DESCENDANTS.stringValuesOf(foundNode);

        return normalizeText(concatenate(textNodeValues));
    }

    /**
     * Xoncatenates all descendant text nodes for each node matched by this XPath
     * and normalizes whitespace via {@link #normalizeText(String)}.
     * <p>
     * Useful for getting an approximation of the text a browser would display.
     *
     * @param node context {@link Document} or {@link Node}
     * @return list of normalised text strings, one per matched node; never {@code null}
     * @throws XPathException if evaluation fails
     * @see #normalizeText(String)
     */
    public List<String> normalizedDescendantText(Object node)
    {
        List<Node> nodes = selectNodes(node);
     
        List<String> results = new ArrayList<String>(nodes.size());
     
        for (Node foundNode : nodes)
        {
            List<String> textNodeValues = TEXT_DESCENDANTS.stringValuesOf(foundNode);
            results.add(normalizeText(concatenate(textNodeValues)));
        }
        return results;
    }

    /**
     * Normalizes text from the DOM to approximate what a browser would display for
     * Latin scripts: all whitespace characters (including non-breaking space) are
     * replaced with a plain space, consecutive spaces are collapsed to one, and
     * leading/trailing spaces are removed.
     * <p>
     * This method is package-private so it can be tested.
     *
     * @param text raw text to normalise; may be {@code null} or empty
     * @return normalised text, or the original value if it was {@code null} or empty
     */
    String normalizeText(String text)
    {
        if (text == null || text.isEmpty())
        {
            return text;
        }

        int len = text.length();
        int start = 0;

        // Skip leading whitespace without allocating anything
        while (start < len && isWhitespace(text.charAt(start)))
        {
            start++;
        }

        if (start == len)
        {
            return "";
        }

        // Find trailing whitespace to reduce scope
        int end = len - 1;
        while (end >= start && isWhitespace(text.charAt(end)))
        {
            end--;
        }

        StringBuilder builder = new StringBuilder(end - start + 1);
        boolean inWhitespace = false;

        for (int i = start; i <= end; i++)
        {
            char c = text.charAt(i);
            if (isWhitespace(c))
                {
                if (!inWhitespace)
                    {
                    builder.append(' ');
                    inWhitespace = true;
                }
            } else
            {
                builder.append(c);
                inWhitespace = false;
            }
        }

        return builder.toString();
    }

    // Private helper (JIT compiler will likely inline this)
    private static boolean isWhitespace(char c) {
        // 0x100002600L is a bitmask where bits 9, 10, 13, and 32 are set to 1
        // Checks for: c == ' ' || c == '\t' || c == '\r' || c == '\n'
        return (c <= 32 && (0x100002600L & (1L << c)) != 0) || c == '\u00A0';
    }

    private String concatenate(List<String> strings)
    {
        if (strings.isEmpty())
        {
            return "";
        }

        if (strings.size() == 1)
        {
            return strings.get(0);
        }

        int totalLength = 0;
        for (String s : strings)
        {
            if (s != null) totalLength += s.length();
        }

        StringBuilder builder = new StringBuilder(totalLength);

        for (String string : strings)
        {
            builder.append(string);
        }

        return builder.toString();
    }
}
