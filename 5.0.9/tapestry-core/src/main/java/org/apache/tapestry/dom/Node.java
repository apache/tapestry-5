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

import org.apache.tapestry.internal.util.PrintOutCollector;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * A node within the DOM.
 */
public abstract class Node
{
    private Node _container;

    private List<Node> _children;

    /**
     * Creates a new node, setting its container to the provided value. Container may also be null,
     * but that is only used for Document nodes (the topmost node of a DOM).
     *
     * @param container
     */
    protected Node(Node container)
    {
        _container = container;
    }

    public Node getContainer()
    {
        return _container;
    }

    /**
     * Returns the node as an {@link Element}, if it is an element. Returns null otherwise.
     */
    Element asElement()
    {
        return null;
    }

    void addChild(Node child)
    {
        if (_children == null) _children = newList();

        _children.add(child);
    }

    void insertChildAt(int index, Node child)
    {
        if (_children == null) _children = newList();

        _children.add(index, child);
    }

    boolean hasChildren()
    {
        return _children != null && !_children.isEmpty();
    }

    void writeChildMarkup(PrintWriter writer)
    {
        if (_children == null) return;

        for (Node child : _children)
            child.toMarkup(writer);
    }

    /**
     * @return the concatenation of the String representations {@link #toString()} of its children.
     */
    public final String getChildMarkup()
    {
        PrintOutCollector collector = new PrintOutCollector();

        writeChildMarkup(collector.getPrintWriter());

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

    @SuppressWarnings("unchecked")
    public List<Node> getChildren()
    {
        return _children == null ? Collections.EMPTY_LIST : _children;
    }

    /**
     * Writes the markup for this node to the writer.
     */
    public abstract void toMarkup(PrintWriter writer);
}
