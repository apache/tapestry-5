// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.util.PrintOutCollector;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.Stack;

import java.io.PrintWriter;
import java.util.*;

/**
 * An element that will render with a begin tag and attributes, a body, and an end tag. Also acts as a factory for
 * enclosed Element, Text and Comment nodes.
 * <p/>
 * TODO: Support for CDATA nodes. Do we need Entity nodes?
 */
public final class Element extends Node
{

    private final String name;

    private Node firstChild;

    private Node lastChild;

    private Attribute firstAttribute;

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
        super(null);

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

        this.namespace = namespace;
        this.name = name;

        document = null;
    }

    @Override
    public Document getDocument()
    {
        return document != null ? document : super.getDocument();
    }

    /**
     * Returns the containing element for this element. This will be null for the root element of a document.
     *
     * @deprecated since 5.1.0.1, use {@link Node#getContainer()} instead
     */
    public Element getParent()
    {
        return container;
    }

    /**
     * Adds an attribute to the element, but only if the attribute name does not already exist.
     *
     * @param name  the name of the attribute to add
     * @param value the value for the attribute. A value of null is allowed, and no attribute will be added to the
     *              element.
     */
    public Element attribute(String name, String value)
    {
        return attribute(null, name, value);
    }

    /**
     * Adds a namespaced attribute to the element, but only if the attribute name does not already exist.
     *
     * @param namespace the namespace to contain the attribute, or null
     * @param name      the name of the attribute to add
     * @param value     the value for the attribute. A value of null is allowed, and no attribute will be added to the
     *                  element.
     */
    public Element attribute(String namespace, String name, String value)
    {
        Defense.notBlank(name, "name");

        updateAttribute(namespace, name, value, false);

        return this;
    }

    private void updateAttribute(String namespace, String name, String value, boolean force)
    {
        if (!force && value == null) return;

        Attribute prior = null;
        Attribute cursor = firstAttribute;

        while (cursor != null)
        {
            if (cursor.matches(namespace, name))
            {
                if (!force) return;

                if (value != null)
                {
                    cursor.value = value;
                    return;
                }

                // Remove this Attribute node from the linked list

                if (prior == null)
                    firstAttribute = cursor.nextAttribute;
                else
                    prior.nextAttribute = cursor.nextAttribute;

                return;
            }

            prior = cursor;
            cursor = cursor.nextAttribute;
        }

        //  Don't add a Attribute if the value is null.

        if (value == null) return;

        firstAttribute = new Attribute(this, namespace, name, value, firstAttribute);
    }


    /**
     * Convenience for invoking {@link #attribute(String, String)} multiple times.
     *
     * @param namesAndValues alternating attribute names and attribute values
     */
    public Element attributes(String... namesAndValues)
    {
        int i = 0;
        while (i < namesAndValues.length)
        {
            String name = namesAndValues[i++];
            String value = namesAndValues[i++];

            attribute(name, value);
        }

        return this;
    }

    /**
     * Forces changes to a number of attributes. The new attributes <em>overwrite</em> previous values. Overriding an
     * attribute's value to null will remove the attribute entirely.
     */
    public Element forceAttributes(String... namesAndValues)
    {
        int i = 0;

        while (i < namesAndValues.length)
        {
            String name = namesAndValues[i++];
            String value = namesAndValues[i++];

            updateAttribute(namespace, name, value, true);
        }

        return this;
    }

    /**
     * Creates and returns a new Element node as a child of this node.
     *
     * @param name           the name of the element to create
     * @param namesAndValues alternating attribute names and attribute values
     */
    public Element element(String name, String... namesAndValues)
    {
        Defense.notBlank(name, "name");

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
        Defense.notBlank(name, "name");

        return newChild(new Element(this, namespace, name));
    }

    public Element elementAt(int index, String name, String... namesAndValues)
    {
        Defense.notBlank(name, "name");

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
        return newChild(new Text(this, text));
    }

    /**
     * Adds and returns a new CDATA node.
     *
     * @param content the content to be rendered by the node
     * @return the newly created node
     */
    public CData cdata(String content)
    {
        return newChild(new CData(this, content));
    }


    private <T extends Node> T newChild(T child)
    {
        addChild(child);

        return child;
    }

    @Override
    void toMarkup(Document document, PrintWriter writer, Map<String, String> containerNamespacePrefixToURI)
    {
        Map<String, String> localNamespacePrefixToURI = createNamespaceURIToPrefix(containerNamespacePrefixToURI);

        MarkupModel markupModel = document.getMarkupModel();

        StringBuilder builder = new StringBuilder();

        String prefixedElementName = toPrefixedName(localNamespacePrefixToURI, namespace, name);

        builder.append("<").append(prefixedElementName);

        // Output order used to be alpha sorted, but now it tends to be the inverse
        // of the order in which attributes were added.

        for (Attribute attr = firstAttribute; attr != null; attr = attr.nextAttribute)
        {
            attr.render(markupModel, builder, localNamespacePrefixToURI);
        }

        // Next, emit namespace declarations for each namespace.

        List<String> namespaces = InternalUtils.sortedKeys(namespaceToPrefix);

        for (String namespace : namespaces)
        {
            if (namespace.equals(Document.XML_NAMESPACE_URI)) continue;

            String prefix = namespaceToPrefix.get(namespace);

            builder.append(" xmlns");

            if (!prefix.equals(""))
            {
                builder.append(":").append(prefix);
            }

            builder.append("=");
            builder.append(markupModel.getAttributeQuote());

            markupModel.encodeQuoted(namespace, builder);

            builder.append(markupModel.getAttributeQuote());
        }

        EndTagStyle style = markupModel.getEndTagStyle(name);

        boolean hasChildren = hasChildren();

        String close = (!hasChildren && style == EndTagStyle.ABBREVIATE) ? "/>" : ">";

        builder.append(close);

        writer.print(builder.toString());

        if (hasChildren) writeChildMarkup(document, writer, localNamespacePrefixToURI);

        // Dangerous -- perhaps it should be an error for a tag of type OMIT to even have children!
        // We'll certainly be writing out unbalanced markup in that case.

        if (style == EndTagStyle.OMIT) return;

        if (hasChildren || style == EndTagStyle.REQUIRE)
        {
            // TAP5-471: Avoid use of printf().
            writer.print("</");
            writer.print(prefixedElementName);
            writer.print(">");
        }
    }

    String toPrefixedName(Map<String, String> namespaceURIToPrefix, String namespace, String name)
    {
        if (namespace == null || namespace.equals("")) return name;

        if (namespace.equals(Document.XML_NAMESPACE_URI)) return "xml:" + name;

        String prefix = namespaceURIToPrefix.get(namespace);

        // This should never happen, because namespaces are automatically defined as needed.

        if (prefix == null)
            throw new IllegalArgumentException(
                    String.format("No prefix has been defined for namespace '%s'.", namespace));

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

        LinkedList<Element> queue = CollectionFactory.newLinkedList();

        queue.add(this);

        while (!queue.isEmpty())
        {
            Element e = queue.removeFirst();

            String elementId = e.getAttribute("id");

            if (id.equals(elementId)) return e;

            for (Element child : e.childElements())
            {
                queue.addLast(child);
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
        Defense.notBlank(path, "path");

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
        for (Element child : childElements())
        {
            if (child.getName().equals(name))
                return child;
        }

        // Not found.

        return null;
    }

    private Iterable<Element> childElements()
    {
        return new Iterable<Element>()
        {
            public Iterator<Element> iterator()
            {
                return new Iterator<Element>()
                {
                    private Node cursor = firstChild;

                    {
                        advance();
                    }

                    private void advance()
                    {
                        while (cursor != null)
                        {
                            if (cursor instanceof Element) return;

                            cursor = cursor.nextSibling;
                        }
                    }

                    public boolean hasNext()
                    {
                        return cursor != null;
                    }

                    public Element next()
                    {
                        Element result = (Element) cursor;

                        cursor = cursor.nextSibling;

                        advance();

                        return result;
                    }

                    public void remove()
                    {
                        throw new UnsupportedOperationException("remove() not supported.");
                    }
                };
            }
        };
    }

    public String getAttribute(String attributeName)
    {
        for (Attribute attr = firstAttribute; attr != null; attr = attr.nextAttribute)
        {
            if (attr.getName().equalsIgnoreCase(attributeName))
                return attr.value;
        }

        return null;
    }

    public String getName()
    {
        return name;
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

        // Don't allow an override of the XML namespace URI, per
        // http://www.w3.org/TR/2006/REC-xml-names-20060816/#xmlReserved

        if (namespace.equals(Document.XML_NAMESPACE_URI))
            return this;

        if (namespaceToPrefix == null)
            namespaceToPrefix = CollectionFactory.newMap();

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

    /**
     * Removes an element; the element's children take the place of the node within its container.
     */
    public void pop()
    {
        // Have to be careful because we'll be  modifying the underlying list of children
        // as we work, so we need a copy of the children.

        List<Node> childrenCopy = CollectionFactory.newList(getChildren());

        for (Node child : childrenCopy)
        {
            child.moveBefore(this);
        }

        remove();
    }

    /**
     * Removes all children from this element.
     *
     * @return the element, for method chaining
     */
    public Element removeChildren()
    {
        firstChild = null;
        lastChild = null;

        return this;
    }

    /**
     * Creates the URI to namespace prefix map for this element, which reflects namespace mappings from containing
     * elements. In addition, automatic namespaces are defined for any URIs that are not explicitly mapped (this occurs
     * sometimes in Ajax partial render scenarios).
     *
     * @return a mapping from namespace URI to namespace prefix
     */
    private Map<String, String> createNamespaceURIToPrefix(Map<String, String> containerNamespaceURIToPrefix)
    {
        MapHolder holder = new MapHolder(containerNamespaceURIToPrefix);

        holder.putAll(namespaceToPrefix);


        // result now contains all the mappings, including this element's.

        // Add a mapping for the element's namespace.

        if (InternalUtils.isNonBlank(namespace))
        {

            // Add the namespace for the element as the default namespace.

            if (!holder.getResult().containsKey(namespace))
            {
                defineNamespace(namespace, "");
                holder.put(namespace, "");
            }
        }

        // And for any attributes that have a namespace.

        for (Attribute attr = firstAttribute; attr != null; attr = attr.nextAttribute)
            addMappingIfNeeded(holder, attr.getNamespace());

        return holder.getResult();
    }

    private void addMappingIfNeeded(MapHolder holder, String namespace)
    {
        if (InternalUtils.isBlank(namespace)) return;

        Map<String, String> current = holder.getResult();

        if (current.containsKey(namespace)) return;

        // A missing namespace.

        Set<String> prefixes = CollectionFactory.newSet(holder.getResult().values());

        // A clumsy way to find a unique id for the new namespace.

        int i = 0;
        while (true)
        {
            String prefix = "ns" + i;

            if (!prefixes.contains(prefix))
            {
                defineNamespace(namespace, prefix);
                holder.put(namespace, prefix);
                return;
            }

            i++;
        }
    }

    @Override
    protected Map<String, String> getNamespaceURIToPrefix()
    {
        MapHolder holder = new MapHolder();

        List<Element> elements = CollectionFactory.newList(this);

        Element cursor = container;

        while (cursor != null)
        {
            elements.add(cursor);
            cursor = cursor.container;
        }

        // Reverse the list, so that later elements will overwrite earlier ones.

        Collections.reverse(elements);

        for (Element e : elements)
            holder.putAll(e.namespaceToPrefix);

        return holder.getResult();
    }

    /**
     * Returns true if the element has no children, or has only text children that contain only whitespace.
     *
     * @since 5.1.0.0
     */
    public boolean isEmpty()
    {
        List<Node> children = getChildren();

        if (children.isEmpty()) return true;

        for (Node n : children)
        {
            if (n instanceof Text)
            {
                Text t = (Text) n;

                if (t.isEmpty()) continue;
            }

            // Not a text node, or a non-empty text node, then the element isn't empty.
            return false;
        }

        return true;
    }

    /**
     * Depth-first visitor traversal of this Element and its Element children. The traversal order is the same as render
     * order.
     *
     * @param visitor callback
     * @since 5.1.0.0
     */
    public void visit(Visitor visitor)
    {
        Stack<Element> queue = CollectionFactory.newStack();

        queue.push(this);

        while (!queue.isEmpty())
        {
            Element e = queue.pop();

            visitor.visit(e);

            e.queueChildren(queue);
        }
    }


    private void queueChildren(Stack<Element> queue)
    {
        if (firstChild == null) return;

        List<Element> childElements = CollectionFactory.newList();

        for (Node cursor = firstChild; cursor != null; cursor = cursor.nextSibling)
        {
            if (cursor instanceof Element)
                childElements.add((Element) cursor);
        }

        Collections.reverse(childElements);

        for (Element e : childElements)
            queue.push(e);
    }

    void addChild(Node child)
    {
        child.container = this;

        if (lastChild == null)
        {
            firstChild = child;
            lastChild = child;
            return;
        }

        lastChild.nextSibling = child;
        lastChild = child;
    }

    void insertChildAt(int index, Node newChild)
    {
        newChild.container = this;

        if (index < 1)
        {
            newChild.nextSibling = firstChild;
            firstChild = newChild;
        }
        else
        {
            Node cursor = firstChild;
            for (int i = 1; i < index; i++)
            {
                cursor = cursor.nextSibling;
            }


            newChild.nextSibling = cursor.nextSibling;
            cursor.nextSibling = newChild;
        }

        if (index < 1)
            firstChild = newChild;

        if (newChild.nextSibling == null)
            lastChild = newChild;
    }

    boolean hasChildren()
    {
        return firstChild != null;
    }

    void writeChildMarkup(Document document, PrintWriter writer, Map<String, String> namespaceURIToPrefix)
    {
        Node cursor = firstChild;

        while (cursor != null)
        {
            cursor.toMarkup(document, writer, namespaceURIToPrefix);

            cursor = cursor.nextSibling;
        }
    }

    /**
     * @return the concatenation of the String representations {@link #toString()} of its children.
     */
    public final String getChildMarkup()
    {
        PrintOutCollector collector = new PrintOutCollector();

        writeChildMarkup(getDocument(), collector.getPrintWriter(), null);

        return collector.getPrintOut();
    }

    /**
     * Returns an unmodifiable list of children for this element. Only {@link org.apache.tapestry5.dom.Element}s will
     * have children.  Also, note that unlike W3C DOM, attributes are not represented as {@link
     * org.apache.tapestry5.dom.Node}s.
     *
     * @return unmodifiable list of children nodes
     */
    @SuppressWarnings("unchecked")
    public List<Node> getChildren()
    {
        List<Node> result = CollectionFactory.newList();
        Node cursor = firstChild;

        while (cursor != null)
        {
            result.add(cursor);
            cursor = cursor.nextSibling;
        }

        return result;
    }

    void remove(Node node)
    {
        Node prior = null;
        Node cursor = firstChild;

        while (cursor != null)
        {
            if (cursor == node)
            {
                Node afterNode = node.nextSibling;

                if (prior != null)
                    prior.nextSibling = afterNode;
                else
                    firstChild = afterNode;

                // If node was the final node in the element then handle deletion.
                // It's even possible node was the only node in the container.

                if (lastChild == node)
                {
                    lastChild = prior != null ? prior : null;
                }

                return;
            }

            prior = cursor;
            cursor = cursor.nextSibling;
        }

        throw new IllegalArgumentException("Node to remove was not present as a child of this element.");
    }

    void insertChildBefore(Node existing, Node node)
    {
        int index = indexOfNode(existing);

        node.container = this;

        insertChildAt(index, node);
    }

    void insertChildAfter(Node existing, Node node)
    {
        Node oldAfter = existing.nextSibling;

        existing.nextSibling = node;
        node.nextSibling = oldAfter;

        if (oldAfter == null)
            lastChild = node;

        node.container = this;
    }

    int indexOfNode(Node node)
    {
        int index = 0;
        Node cursor = firstChild;

        while (cursor != null)
        {
            if (node == cursor) return index;

            cursor = cursor.nextSibling;
            index++;
        }

        throw new IllegalArgumentException("Node not a child of this element.");
    }

    /**
     * Returns the attributes for this Element as a (often empty) collection of {@link
     * org.apache.tapestry5.dom.Attribute}s. The order of the attributes within the collection is not specified.
     * Modifying the collection will not affect the attributes (use {@link #forceAttributes(String[])} to change
     * existing attribute values, and {@link #attribute(String, String, String)} to add new attribute values.
     *
     * @return attribute collection
     */
    public Collection<Attribute> getAttributes()
    {
        Collection<Attribute> result = CollectionFactory.newList();

        for (Attribute a = firstAttribute; a != null; a = a.nextAttribute)
        {
            result.add(a);
        }

        return result;
    }
}
