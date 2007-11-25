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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.*;
import org.apache.tapestry.ioc.internal.util.Defense;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

import java.io.PrintWriter;
import java.util.*;

/**
 * An element that will render with a begin tag and attributes, a body, and an end tag. Also acts as
 * a factory for enclosed Element, Text and Comment nodes.
 * <p/>
 * TODO: Support for CDATA nodes. Do we need Entity nodes?
 */
public final class Element extends Node
{
    private final String _name;

    private Map<String, String> _attributes;

    private Element _parent;

    private final Document _document;

    private static final String CLASS_ATTRIBUTE = "class";

    Element(Document container, String name)
    {
        super(container);

        _document = container;
        _name = name;
    }

    Element(Element parent, String name)
    {
        super(parent);

        _parent = parent;
        _name = name;

        _document = parent.getDocument();
    }

    public Document getDocument()
    {
        return _document;
    }

    /**
     * Returns the containing element for this element. This will be null for the root element of a
     * document.
     */
    public Element getParent()
    {
        return _parent;
    }

    /**
     * Adds an attribute to the element, but only if the attribute name does not already exist.
     *
     * @param name  the name of the attribute to add
     * @param value the value for the attribute. A value of null is allowed, and no attribute will be
     *              added to the element.
     */
    public void attribute(String name, String value)
    {
        notBlank(name, "name");

        if (value == null) return;

        if (_attributes == null) _attributes = newMap();

        if (!_attributes.containsKey(name)) _attributes.put(name, value);
    }

    /**
     * Convenience for invoking {@link #attribute(String, String)} multiple times.
     *
     * @param namesAndValues alternating attribute names and attribute values
     */
    public void attributes(String... namesAndValues)
    {
        int i = 0;
        while (i < namesAndValues.length)
        {
            String name = namesAndValues[i++];
            String value = namesAndValues[i++];

            attribute(name, value);
        }
    }

    /**
     * Forces changes to a number of attributes. The new attributes <em>overwrite</em> previous
     * values.
     */
    public void forceAttributes(String... namesAndValues)
    {
        if (_attributes == null) _attributes = newMap();

        int i = 0;

        while (i < namesAndValues.length)
        {
            String name = namesAndValues[i++];
            String value = namesAndValues[i++];

            if (value == null)
            {
                _attributes.remove(name);
                continue;
            }

            _attributes.put(name, value);
        }
    }

    /**
     * Creates and returns a new Element node as a child of this node.
     *
     * @param name           the name of the element to create
     * @param namesAndValues alternating attribute names and attribute values
     */
    public Element element(String name, String... namesAndValues)
    {
        notBlank(name, "name");

        Element child = newChild(new Element(this, name));

        child.attributes(namesAndValues);

        return child;
    }

    public Element elementAt(int index, String name, String... namesAndValues)
    {
        notBlank(name, "name");

        Element child = new Element(this, name);
        child.attributes(namesAndValues);

        insertChildAt(index, child);

        return child;
    }

    /**
     * Adds the comment and returns this element for further construction.
     */
    public Element comment(String text)
    {
        newChild(new Comment(this, text));

        return this;
    }

    /**
     * Adds the raw text and returns this element for further construction.
     */
    public Element raw(String text)
    {
        newChild(new Raw(this, text));

        return this;
    }

    /**
     * Adds and returns a new text node (the text node is returned so that
     * {@link Text#write(String)} or [@link {@link Text#writef(String, Object[])} may be invoked .
     *
     * @param text initial text for the node
     * @return the new Text node
     */
    public Text text(String text)
    {
        return newChild(new Text(this, _document, text));
    }

    private <T extends Node> T newChild(T child)
    {
        addChild(child);

        return child;
    }

    @Override
    public void toMarkup(PrintWriter writer)
    {
        StringBuilder buffer = new StringBuilder();

        Formatter formatter = new Formatter(buffer);

        formatter.format("<%s", _name);

        MarkupModel markupModel = _document.getMarkupModel();

        if (_attributes != null)
        {
            List<String> keys = newList(_attributes.keySet());
            Collections.sort(keys);

            for (String key : keys)
            {
                String value = _attributes.get(key);

                formatter.format(" %s=\"", key);

                markupModel.encodeQuoted(value, buffer);

                buffer.append('"');
            }
        }

        EndTagStyle style = markupModel.getEndTagStyle(_name);

        boolean hasChildren = hasChildren();

        String close = (!hasChildren && style == EndTagStyle.ABBREVIATE) ? "/>" : ">";

        formatter.format(close);

        writer.print(buffer.toString());

        if (hasChildren) writeChildMarkup(writer);

        // Dangerous -- perhaps it should be an error for a tag of type OMIT to even have children!
        // We'll certainly be writing out unbalanced markup in that case.

        if (style == EndTagStyle.OMIT) return;

        if (hasChildren || style == EndTagStyle.REQUIRE) writer.printf("</%s>", _name);
    }

    /**
     * Tries to find an element under this element (including itself) whose id is specified.
     * Performs a width-first search of the document tree.
     *
     * @param id the value of the id attribute of the element being looked for
     * @return the element if found. null if not found.
     */
    public Element getElementById(String id)
    {
        Defense.notNull(id, "id");

        LinkedList<Element> queue = newLinkedList();

        queue.add(this);

        while (!queue.isEmpty())
        {
            Element e = queue.removeFirst();

            String elementId = e.getAttribute("id");

            if (id.equals(elementId)) return e;

            for (Node n : e.getChildren())
            {
                Element child = n.asElement();

                if (child != null) queue.addLast(child);
            }
        }

        // Exhausted the entire tree

        return null;
    }

    /**
     * Searchs for a child element with a particular name below this element. The path parameter is
     * a slash separated series of element names.
     *
     * @param path
     * @return
     */
    public Element find(String path)
    {
        notBlank(path, "path");

        Element search = this;

        for (String name : path.split("/"))
        {
            search = search.findChildWithElementName(name);

            if (search == null) break;
        }

        return search;
    }

    private Element findChildWithElementName(String name)
    {
        for (Node node : getChildren())
        {
            Element child = node.asElement();

            if (child != null && child.getName().equals(name)) return child;
        }

        // Not found.

        return null;
    }

    public String getAttribute(String attributeName)
    {
        return InternalUtils.get(_attributes, attributeName);
    }

    public String getName()
    {
        return _name;
    }

    /**
     * All other implementations of Node return null except this one.
     */
    @Override
    Element asElement()
    {
        return this;
    }

    /**
     * Adds one or more CSS class names to the "class" attribute. No check
     * for duplicates is made. Note that CSS class names are case insensitive
     * on the client.
     *
     * @param className one or more CSS class names
     * @return the element for further configuration
     */
    public Element addClassName(String... className)
    {
        String classes = getAttribute(CLASS_ATTRIBUTE);

        StringBuilder builder = new StringBuilder();

        if (classes != null) builder.append(classes);

        for (String name : className)
        {
            if (builder.length() > 0) builder.append(" ");

            builder.append(name);
        }

        forceAttributes(CLASS_ATTRIBUTE, builder.toString());

        return this;
    }
}
