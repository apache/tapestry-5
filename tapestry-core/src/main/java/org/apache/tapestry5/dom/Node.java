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

import org.apache.tapestry5.internal.util.PrintOutCollector;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * A node within the DOM.
 */
public abstract class Node
{
    private Node container;

    /**
     * Child notes; only Element actually has children.  Attributes are modeled differently in this DOM than in
     * traditional W3C Dom (where Attributes are another type of child node).
     */
    private List<Node> children;

    /**
     * Creates a new node, setting its container to the provided value. Container may also be null, but that is only
     * used for Document nodes (the topmost node of a DOM).
     *
     * @param container
     */
    protected Node(Node container)
    {
        this.container = container;
    }

    /**
     * Returns the containing node for this node, or null if this node is the root element of the document.
     */
    public Node getContainer()
    {
        return container;
    }

    public Document getDocument()
    {
        return container.getDocument();
    }

    /**
     * Returns the node as an {@link org.apache.tapestry5.dom.Element}, if it is an element. Returns null otherwise.
     */
    Element asElement()
    {
        return null;
    }

    void addChild(Node child)
    {
        ensureChildren();

        children.add(child);

        child.container = this;
    }

    private void ensureChildren()
    {
        if (children == null) children = CollectionFactory.newList();
    }

    void insertChildAt(int index, Node child)
    {
        ensureChildren();

        children.add(index, child);

        child.container = this;
    }

    boolean hasChildren()
    {
        return children != null && !children.isEmpty();
    }

    void writeChildMarkup(Document document, PrintWriter writer)
    {
        if (children == null) return;

        for (Node child : children)
            child.toMarkup(writer);
    }

    /**
     * @return the concatenation of the String representations {@link #toString()} of its children.
     */
    public final String getChildMarkup()
    {
        PrintOutCollector collector = new PrintOutCollector();

        writeChildMarkup(getDocument(), collector.getPrintWriter());

        return collector.getPrintOut();
    }

    /**
     * Invokes {@link #toMarkup(PrintWriter)}, collecting output in a string, which is returned.
     */
    @Override
    public String toString()
    {
        PrintOutCollector collector = new PrintOutCollector();

        toMarkup(collector.getPrintWriter());

        return collector.getPrintOut();
    }

    /**
     * Returns an unmodifiable list of children for this node. Only {@link org.apache.tapestry5.dom.Element}s will have
     * children.  Also, note that unlike W3C DOM, attributes are not represented as {@link
     * org.apache.tapestry5.dom.Node}s.
     *
     * @return unmodifiable list of children nodes
     */
    @SuppressWarnings("unchecked")
    public List<Node> getChildren()
    {
        return children == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(children);
    }

    /**
     * Writes the markup for this node to the writer.
     */
    public void toMarkup(PrintWriter writer)
    {
        toMarkup(getDocument(), writer);
    }

    /**
     * Implemented by each subclass, with the document passed in for efficiency.
     */
    abstract void toMarkup(Document document, PrintWriter writer);

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

        element.getContainer().insertChildBefore(element, this);

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

        element.getContainer().insertChildAfter(element, this);

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

    private void validateElement(Element element)
    {
        Defense.notNull(element, "element");

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
        getContainer().remove(this);

        container = null;
    }


    /**
     * Removes a child node from this node.
     */
    void remove(Node node)
    {
        if (children == null || !children.remove(node))
            throw new IllegalArgumentException("Node to remove was not present as a child of this node.");
    }

    private void insertChildBefore(Node existing, Node node)
    {
        int index = indexOfNode(existing);

        children.add(index, node);

        node.container = this;
    }

    private void insertChildAfter(Node existing, Node node)
    {
        int index = indexOfNode(existing);

        children.add(index + 1, node);

        node.container = this;
    }


    int indexOfNode(Node node)
    {
        ensureChildren();

        int index = children.indexOf(node);

        if (index < 0) throw new IllegalArgumentException("Existing element not a child of this node.");

        return index;
    }

    void clearChildren()
    {
        children = null;
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
        Element containerElement = container.asElement();

        int index = containerElement.indexOfNode(this);

        // Insert the new element just before this node.
        Element element =
                containerElement.elementAt(index, elementName, namesAndValues);

        // Move this node inside the new element.
        moveToTop(element);

        return element;
    }
}
