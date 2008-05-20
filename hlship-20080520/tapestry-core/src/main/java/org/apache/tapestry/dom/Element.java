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

package org.apache.tapestry.dom;

import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newLinkedList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry.ioc.internal.util.Defense;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import org.apache.tapestry.ioc.internal.util.InternalUtils;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * An element that will render with a begin tag and attributes, a body, and an end tag. Also acts as a factory for
 * enclosed Element, Text and Comment nodes.
 * <p/>
 * TODO: Support for CDATA nodes. Do we need Entity nodes?
 */
public final class Element extends Node
{
    class Attribute
    {
        private final String namespace;
        private final String name;
        private final String value;

        public Attribute(String namespace, String name, String value)
        {
            this.namespace = namespace;
            this.name = name;
            this.value = value;
        }

        public String getValue()
        {
            return value;
        }

        void render(MarkupModel model, StringBuilder builder)
        {
            builder.append(" ");
            builder.append(toPrefixedName(namespace, name));
            builder.append("=\"");
            model.encodeQuoted(value, builder);
            builder.append('"');
        }
    }

    private final String name;

    private Map<String, Attribute> attributes;

    private Element parent;

    private final Document document;

    private static final String CLASS_ATTRIBUTE = "class";

    /**
     * URI of the namespace which contains the element.  A quirk in XML is that the element may be in a namespace it
     * defines itself, so resolving the namespace to a prefix must wait until render time (since the Element is created
     * before the namespaces for it are defined).
     */
    private final String namespace;

    private Map<String, String> namespaceToPrefix;

    /**
     * Constructor for a root element.
     */
    Element(Document container, String namespace, String name)
    {
        super(container);

        document = container;
        this.namespace = namespace;
        this.name = name;
    }

    /**
     * Constructor for a nested element.
     */
    Element(Element parent, String namespace, String name)
    {
        super(parent);

        this.parent = parent;
        this.namespace = namespace;
        this.name = name;

        document = parent.getDocument();
    }

    public Document getDocument()
    {
        return document;
    }

    /**
     * Returns the containing element for this element. This will be null for the root element of a document.
     */
    public Element getParent()
    {
        return parent;
    }

    /**
     * Adds an attribute to the element, but only if the attribute name does not already exist.
     *
     * @param name  the name of the attribute to add
     * @param value the value for the attribute. A value of null is allowed, and no attribute will be added to the
     *              element.
     */
    public void attribute(String name, String value)
    {
        attribute(null, name, value);
    }

    /**
     * Adds a namespaced attribute to the element, but only if the attribute name does not already exist.
     *
     * @param namespace the namespace to contain the attribute, or null
     * @param name      the name of the attribute to add
     * @param value     the value for the attribute. A value of null is allowed, and no attribute will be added to the
     *                  element.
     */
    public void attribute(String namespace, String name, String value)
    {
        notBlank(name, "name");

        if (value == null) return;

        if (attributes == null) attributes = newMap();

        if (!attributes.containsKey(name)) attributes.put(name, new Attribute(namespace, name, value));
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
     * Forces changes to a number of attributes. The new attributes <em>overwrite</em> previous values.
     */
    public void forceAttributes(String... namesAndValues)
    {
        if (attributes == null) attributes = newMap();

        int i = 0;

        while (i < namesAndValues.length)
        {
            String name = namesAndValues[i++];
            String value = namesAndValues[i++];

            if (value == null)
            {
                attributes.remove(name);
                continue;
            }

            attributes.put(name, new Attribute(null, name, value));
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

        Element child = newChild(new Element(this, null, name));

        child.attributes(namesAndValues);

        return child;
    }

    /**
     * Creates and returns a new Element within a namespace as a child of this node.
     *
     * @param namespace namespace to contain the element, or null
     * @param name      element name to create within the namespace
     * @return the newly created element
     */
    public Element elementNS(String namespace, String name)
    {
        notBlank(name, "name");

        return newChild(new Element(this, namespace, name));
    }

    public Element elementAt(int index, String name, String... namesAndValues)
    {
        notBlank(name, "name");

        Element child = new Element(this, null, name);
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
     * Adds and returns a new text node (the text node is returned so that {@link Text#write(String)} or [@link {@link
     * Text#writef(String, Object[])} may be invoked .
     *
     * @param text initial text for the node
     * @return the new Text node
     */
    public Text text(String text)
    {
        return newChild(new Text(this, document, text));
    }

    /**
     * Adds an returns a new CDATA node.
     *
     * @param content the content to be rendered by the node
     * @return the newly created node
     */
    public CData cdata(String content)
    {
        return newChild(new CData(this, document, content));
    }


    private <T extends Node> T newChild(T child)
    {
        addChild(child);

        return child;
    }

    @Override
    public void toMarkup(PrintWriter writer)
    {
        StringBuilder builder = new StringBuilder();

        String prefixedElementName = toPrefixedName(namespace, name);

        builder.append("<").append(prefixedElementName);

        MarkupModel markupModel = document.getMarkupModel();

        if (attributes != null)
        {
            List<String> keys = InternalUtils.sortedKeys(attributes);

            for (String key : keys)
            {
                Attribute attribute = attributes.get(key);

                attribute.render(markupModel, builder);
            }
        }

        // Next, emit namespace declarations for each namespace.

        if (namespaceToPrefix != null)
        {
            List<String> namespaces = InternalUtils.sortedKeys(namespaceToPrefix);

            for (String namespace : namespaces)
            {
                String prefix = namespaceToPrefix.get(namespace);

                builder.append(" xmlns");

                if (!prefix.equals(""))
                {
                    builder.append(":").append(prefix);
                }

                builder.append("=\"");

                markupModel.encodeQuoted(namespace, builder);

                builder.append('"');
            }
        }

        EndTagStyle style = markupModel.getEndTagStyle(name);

        boolean hasChildren = hasChildren();

        String close = (!hasChildren && style == EndTagStyle.ABBREVIATE) ? "/>" : ">";

        builder.append(close);

        writer.print(builder.toString());

        if (hasChildren) writeChildMarkup(writer);

        // Dangerous -- perhaps it should be an error for a tag of type OMIT to even have children!
        // We'll certainly be writing out unbalanced markup in that case.

        if (style == EndTagStyle.OMIT) return;

        if (hasChildren || style == EndTagStyle.REQUIRE) writer.printf("</%s>", prefixedElementName);
    }

    private String toPrefixedName(String namespace, String name)
    {
        if (namespace == null || namespace.equals("")) return name;

        String prefix = toNamespacePrefix(namespace);

        // The empty string indicates the default namespace which doesn't use a prefix.

        if (prefix.equals("")) return name;

        return prefix + ":" + name;
    }

    /**
     * Tries to find an element under this element (including itself) whose id is specified. Performs a width-first
     * search of the document tree.
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
     * Searchs for a child element with a particular name below this element. The path parameter is a slash separated
     * series of element names.
     *
     * @param path
     * @return
     */
    public Element find(String path)
    {
        notBlank(path, "path");

        Element search = this;

        for (String name : TapestryInternalUtils.splitPath(path))
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
        Attribute attribute = InternalUtils.get(attributes, attributeName);

        return attribute == null ? null : attribute.getValue();
    }

    public String getName()
    {
        return name;
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
     * Adds one or more CSS class names to the "class" attribute. No check for duplicates is made. Note that CSS class
     * names are case insensitive on the client.
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

    String toNamespacePrefix(String namespaceURI)
    {
        String prefix = InternalUtils.get(namespaceToPrefix, namespaceURI);

        if (prefix != null) return prefix;

        if (parent == null) throw new RuntimeException(DomMessages.namespaceURINotMappedToPrefix(namespaceURI));

        return parent.toNamespacePrefix(namespaceURI);
    }

    /**
     * Defines a namespace for this element, mapping a URI to a prefix.   This will affect how namespaced elements and
     * attributes nested within the element are rendered, and will also cause <code>xmlns:</code> attributes (to define
     * the namespace and prefix) to be rendered.
     *
     * @param namespace       URI of the namespace
     * @param namespacePrefix prefix
     * @return this element
     */
    public Element defineNamespace(String namespace, String namespacePrefix)
    {
        Defense.notNull(namespace, "namespace");
        Defense.notNull(namespacePrefix, "namespacePrefix");

        if (namespaceToPrefix == null) namespaceToPrefix = CollectionFactory.newMap();

        namespaceToPrefix.put(namespace, namespacePrefix);

        return this;
    }

    /**
     * Returns the namespace for this element (which is typically a URL). The namespace may be null.
     */
    public String getNamespace()
    {
        return namespace;
    }
}
