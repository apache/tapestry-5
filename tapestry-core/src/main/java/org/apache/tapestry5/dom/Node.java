// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;

import org.apache.tapestry5.dom.xpath.XPath;
import org.apache.tapestry5.dom.xpath.XPathException;
import org.apache.tapestry5.internal.util.PrintOutCollector;

/**
 * A node within the DOM.
 */
public abstract class Node
{
    Element container;

    /**
     * Next node within containing element.
     */
    Node nextSibling;

    /**
     * Creates a new node, setting its container to the provided value. Container may also be null, but that is only
     * used for Document nodes (the topmost node of a DOM).
     *
     * @param container element containing this node
     */
    protected Node(Element container)
    {
        this.container = container;
    }

    /**
     * Returns the containing {@link org.apache.tapestry5.dom.Element} for this node, or null if this node is the root
     * element of the document.
     */
    public Element getContainer()
    {
        return container;
    }

    /**
     * Returns the owning {@link Document}, or {@code null} if this node is detached (not part of
     * any document tree).
     */
    public Document getDocument()
    {
        return container != null ? container.getDocument() : null;
    }

    /**
     * Invokes {@link #toMarkup(PrintWriter)}, collecting output in a string, which is returned.
     * When the node is detached from a document, {@link DefaultMarkupModel} is used as a fallback.
     */
    @Override
    public String toString()
    {
        PrintOutCollector collector = new PrintOutCollector();

        toMarkup(collector.getPrintWriter());

        return collector.getPrintOut();
    }

    /**
     * Renders this node to a {@code String} using the specified {@link MarkupModel}, without
     * requiring the node to be attached to a {@link Document}.
     * <p>
     * This is the preferred rendering method for detached nodes (such as those returned by
     * {@link #deepClone()}). For attached nodes, {@link #toString()} uses the document's
     * model automatically.
     *
     * @param model the markup model controlling encoding and tag-style decisions; must not be null
     * @return the rendered markup as a string
     * @since 5.10
     */
    public String toMarkup(MarkupModel model)
    {
        assert model != null;

        PrintOutCollector collector = new PrintOutCollector();

        toMarkup(new Document(model), collector.getPrintWriter(), getNamespaceURIToPrefix());

        return collector.getPrintOut();
    }

    /**
     * Writes the markup for this node to the writer, using {@link DefaultMarkupModel} as a
     * fallback when the node is not attached to a document.
     */
    public void toMarkup(PrintWriter writer)
    {
        Document doc = getDocument();

        toMarkup(doc != null ? doc : new Document(), writer, getNamespaceURIToPrefix());
    }

    protected Map<String, String> getNamespaceURIToPrefix()
    {
        // For non-Elements, the container (which should be an Element) will provide the mapping.
        // For detached nodes the container is null; return an empty map (no ancestor namespaces).

        return container != null ? container.getNamespaceURIToPrefix() : Collections.emptyMap();
    }

    /**
     * Implemented by each subclass, with the document passed in for efficiency.
     */
    abstract void toMarkup(Document document, PrintWriter writer, Map<String, String> namespaceURIToPrefix);

    /**
     * Moves this node so that it becomes a sibling of the element, ordered just before the element.
     *
     * @param element to move the node before
     * @return the node for further modification
     */
    public Node moveBefore(Element element)
    {
        validateElement(element);

        remove();

        element.container.insertChildBefore(element, this);

        return this;
    }


    /**
     * Moves this node so that it becomes a sibling of the element, ordered just after the element.
     *
     * @param element to move the node after
     * @return the node for further modification
     */
    public Node moveAfter(Element element)
    {
        validateElement(element);

        remove();

        element.container.insertChildAfter(element, this);

        return this;
    }

    /**
     * Moves this node so that it becomes this first child of the element, shifting existing elements forward.
     *
     * @param element to move the node inside
     * @return the node for further modification
     */
    public Node moveToTop(Element element)
    {
        validateElement(element);

        remove();

        element.insertChildAt(0, this);

        return this;
    }

    /**
     * Moves this node so that it the last child of the element.
     *
     * @param element to move the node inside
     * @return the node for further modification
     */
    public Node moveToBottom(Element element)
    {
        validateElement(element);

        remove();

        element.addChild(this);

        return this;
    }

    /**
     * Inserts {@code node} immediately before {@code this} in the parent's child list, detaching
     * it from any prior parent first.
     * <p>
     * This is the anchor-centric complement to {@link #moveBefore(Element)}; unlike
     * {@code moveBefore}, the anchor ({@code this}) may be any {@link Node} type, not just an
     * {@link Element}.
     *
     * @param node the node to insert; must not be {@code null} or {@code this}
     * @return the inserted {@code node}, now in position
     * @throws IllegalArgumentException if {@code node} is {@code null}, is {@code this}, or is an ancestor of {@code this}
     * @throws IllegalStateException    if {@code this} is detached (has no parent)
     * @since 5.10
     */
    public Node insertBefore(Node node)
    {
        if (node == null)
        {
            throw new IllegalArgumentException("node must not be null");
        }
        if (node == this)
        {
            throw new IllegalArgumentException("A node cannot be inserted relative to itself");
        }
        if (container == null)
        {
            throw new IllegalStateException("Cannot insert relative to a detached node");
        }

        Node search = container;
        while (search != null)
        {
            if (search == node)
            {
                throw new IllegalArgumentException("Cannot insert an ancestor node");
            }

            search = search.container;
        }

        node.detach();
        container.insertChildBefore(this, node);

        return node;
    }

    /**
     * Inserts {@code node} immediately after {@code this} in the parent's child list, detaching
     * it from any prior parent first.
     * <p>
     * This is the anchor-centric complement to {@link #moveAfter(Element)}; unlike
     * {@code moveAfter}, the anchor ({@code this}) may be any {@link Node} type, not just an
     * {@link Element}.
     *
     * @param node the node to insert; must not be {@code null} or {@code this}
     * @return the inserted {@code node}, now in position
     * @throws IllegalArgumentException if {@code node} is {@code null}, is {@code this}, or is an ancestor of {@code this}
     * @throws IllegalStateException    if {@code this} is detached (has no parent)
     * @since 5.10
     */
    public Node insertAfter(Node node)
    {
        if (node == null)
        {
            throw new IllegalArgumentException("node must not be null");
        }
        if (node == this)
        {
            throw new IllegalArgumentException("A node cannot be inserted relative to itself");
        }
        if (container == null)
        {
            throw new IllegalStateException("Cannot insert relative to a detached node");
        }

        Node search = container;
        while (search != null)
        {
            if (search == node)
            {
                throw new IllegalArgumentException("Cannot insert an ancestor node");
            }

            search = search.container;
        }

        node.detach();
        container.insertChildAfter(this, node);

        return node;
    }

    /**
     * Returns the next sibling node within the containing element, or {@code null}
     * if {@code this} is:
     * <ul>
     * <li>the last child</li>
     * <li>not part of a sibling list (root element, document-preamble node, or
     * detached node)</li>
     * </ul>
     *
     * @return the next sibling node, or {@code null}
     * @since 5.10
     */
    public Node getNextSibling()
    {
        return nextSibling;
    }

    /**
     * Returns the previous sibling node within the containing element, or {@code null}
     * if {@code this} is:
     * <ul>
     * <li>the first child</li>
     * <li>not part of a sibling list (root element, document-preamble node, or detached node).
     * </ul>
     *
     * @return the previous sibling node, or {@code null}
     * @since 5.10
     */
    public Node getPreviousSibling()
    {
        if (container == null)
        {
            return null;
        }

        Node previous = null;
        Node cursor = container.firstChild;

        while (cursor != null)
        {
            if (cursor == this)
            {
                return previous;
            }

            previous = cursor;
            cursor = cursor.nextSibling;
        }

        throw new IllegalStateException("Node is not a child of its own container.");
    }

    private void validateElement(Element element)
    {
        assert element != null;

        Node search = element;
        while (search != null)
        {
            if (search.equals(this))
            {
                throw new IllegalArgumentException("Unable to move a node relative to itself.");
            }

            search = search.getContainer();
        }
    }

    /**
     * Removes a node from its container, setting its container property to null, and removing it from its container's
     * list of children.
     */
    public void remove()
    {
        container.remove(this);

        container = null;
    }

    /**
     * Replaces this node in the DOM with the given {@code replacement} node, detaching this node and
     * inserting the replacement in its place.
     * <p>
     * If {@code replacement} is currently attached to another parent, it is detached first.
     *
     * @param replacement the node to put in place of this node, must not be {@code null} or {@code this}
     * @return the replacement node, now in position
     * @throws IllegalArgumentException if {@code replacement} is {@code null}, is {@code this}, or is an ancestor of {@code this}
     * @throws IllegalStateException    if {@code this} is detached (has no parent)
     * @since 5.10
     */
    public Node replaceWith(Node replacement)
    {
        if (replacement == null)
        {
            throw new IllegalArgumentException("replacement must not be null");
        }
        if (replacement == this)
        {
            throw new IllegalArgumentException("A node cannot replace itself");
        }
        if (container == null)
        {
            throw new IllegalStateException("Cannot replace a detached node");
        }

        // Guard against ancestor cycles: replacement must not be an ancestor of this node.
        Node search = container;
        while (search != null)
        {
            if (search == replacement)
            {
                throw new IllegalArgumentException("Cannot replace a node with one of its ancestors");
            }

            search = search.container;
        }

        Element parent = container;
        replacement.detach();
        parent.insertChildBefore(this, replacement);
        remove();

        return replacement;
    }

    /**
     * Detaches this node from its containing element and returns it.
     * <p>
     * If the node is already detached (no container), this method is a no-op.
     *
     * @return this node, now detached
     * @since 5.10
     */
    public Node detach()
    {
        if (container != null)
            remove();

        return this;
    }

    /**
     * Wraps a node inside a new element.  The new element is created before the node, then the node is moved inside the
     * new element.
     *
     * @param elementName    name of new element to create
     * @param namesAndValues to set attributes of new element
     * @return the created element
     */
    public Element wrap(String elementName, String... namesAndValues)
    {
        int index = container.indexOfNode(this);

        // Insert the new element just before this node.
        Element element = container.elementAt(index, elementName, namesAndValues);

        // Move this node inside the new element.
        moveToTop(element);

        return element;
    }

    /**
     * Returns a deep copy of this node and its entire subtree, detached from any parent or
     * document.
     * <p>
     * Detached nodes can be still be rendered via {@link #toString()} (which falls back to
     * {@link DefaultMarkupModel}) or with an explicit model via {@link #toMarkup(MarkupModel)}.
     *
     * @return a fully independent deep copy of this node
     * @since 5.10
     */
    public abstract Node deepClone();

    /**
     * Creates a {@link BoundXPath} for the given XPath expression with this node as the context,
     * allowing fluent queries without repeating the context node:
     * <pre>
     * List&lt;Element&gt; items = element.xpath("ul/li").elements();
     * </pre>
     *
     * @param expression XPath expression; must not be null or empty
     * @return a {@link BoundXPath} bound to this node
     * @throws IllegalArgumentException if {@code expression} is null or empty
     * @throws XPathException if the expression cannot be parsed
     * @since 5.10
     */
    public BoundXPath xpath(String expression)
    {
        if (expression == null || expression.isEmpty())
        {
            throw new IllegalArgumentException("XPath expression must not be null or empty");
        }

        return new BoundXPath(XPath.of(expression), this);
    }
}
