// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.dom;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.Stack;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.util.PrintOutCollector;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.io.PrintWriter;
import java.util.*;

/**
 * An element that will render with a begin tag and attributes, a body, and an end tag. Also acts as a factory for
 * enclosed Element, Text and Comment nodes.
 *
 * TODO: Support for CDATA nodes. Do we need Entity nodes?
 */
public final class Element extends Node
{

    private final String name;

    Node firstChild;

    private Node lastChild;

    private Attribute firstAttribute;

    private final Document document;

    /**
     * URI of the namespace which contains the element. A quirk in XML is that the element may be in a namespace it
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
     * Adds an attribute to the element, but only if the attribute name does not already exist.
     * The "class" attribute is treated specially: the new value is appended, after a space, to the
     * existing value.
     *
     * @param name
     *         the name of the attribute to add
     * @param value
     *         the value for the attribute. A value of null is allowed, and no attribute will be added to the
     *         element.
     */
    public Element attribute(String name, String value)
    {
        return attribute(null, name, value);
    }

    /**
     * Adds a namespaced attribute to the element, but only if the attribute name does not already exist.
     * The "class" attribute of the default namespace is treated specially: the new value
     * is appended, after a space, to the existing value.
     *
     * @param namespace
     *         the namespace to contain the attribute, or null for the default namespace
     * @param name
     *         the name of the attribute to add
     * @param value
     *         the value for the attribute. A value of null is allowed, and no attribute will be added to the
     *         element.
     */
    public Element attribute(String namespace, String name, String value)
    {
        assert InternalUtils.isNonBlank(name);

        updateAttribute(namespace, name, value, false);

        return this;
    }

    private void updateAttribute(String namespace, String name, String value, boolean force)
    {
        if (!force && value == null)
        {
            return;
        }
        
        // TAP5-2660: handle empty namespace as the null one, since both represent no namespace
        if ("".equals(namespace))
        {
            namespace = null;
        }

        Attribute prior = null;
        Attribute cursor = firstAttribute;

        while (cursor != null)
        {
            if (cursor.matches(namespace, name))
            {
                boolean isClass = namespace == null && name.equals("class");

                if (!(force || isClass))
                {
                    return;
                }

                if (value != null)
                {
                    if (!force && isClass)
                    {
                        cursor.value += (" " + value);
                    } else
                    {
                        cursor.value = value;
                    }

                    return;
                }

                // Remove this Attribute node from the linked list

                if (prior == null)
                {
                    firstAttribute = cursor.nextAttribute;
                } else
                {
                    prior.nextAttribute = cursor.nextAttribute;
                }

                return;
            }

            prior = cursor;
            cursor = cursor.nextAttribute;
        }

        // Don't add an Attribute if the value is null.

        if (value != null)
        {
            firstAttribute = new Attribute(this, namespace, name, value, firstAttribute);
        }
    }

    /**
     * Convenience for invoking {@link #attribute(String, String)} multiple times.
     *
     * @param namesAndValues
     *         alternating attribute names and attribute values
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
     *
     * @param namesAndValues
     *         alternating attribute names and attribute values
     * @return this element
     */
    public Element forceAttributes(String... namesAndValues)
    {
        return forceAttributesNS(null, namesAndValues);
    }

    /**
     * Forces changes to a number of attributes in the global namespace. The new attributes <em>overwrite</em> previous
     * values (event for the "class" attribute). Overriding attribute's value to null will remove the attribute entirely.
     * TAP5-708: don't use element namespace for attributes
     *
     * @param namespace
     *         the namespace or null
     * @param namesAndValues
     *         alternating attribute name and value
     * @return this element
     */
    public Element forceAttributesNS(String namespace, String... namesAndValues)
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
     * @param name
     *         the name of the element to create
     * @param namesAndValues
     *         alternating attribute names and attribute values
     */
    public Element element(String name, String... namesAndValues)
    {
        assert InternalUtils.isNonBlank(name);
        Element child = newChild(new Element(this, null, name));

        child.attributes(namesAndValues);

        return child;
    }

    /**
     * Inserts a new element before this element.
     *
     * @param name
     *         element name
     * @param namesAndValues
     *         attribute names and values
     * @return the new element
     * @since 5.3
     */
    public Element elementBefore(String name, String... namesAndValues)
    {
        assert InternalUtils.isNonBlank(name);

        Element sibling = container.element(name, namesAndValues);

        sibling.moveBefore(this);

        return sibling;
    }


    /**
     * Creates and returns a new Element within a namespace as a child of this node.
     *
     * @param namespace
     *         namespace to contain the element, or null
     * @param name
     *         element name to create within the namespace
     * @return the newly created element
     */
    public Element elementNS(String namespace, String name)
    {
        assert InternalUtils.isNonBlank(name);
        return newChild(new Element(this, namespace, name));
    }

    /**
     * Creates a new element, as a child of the current index, at the indicated index.
     *
     * @param index
     *         to insert at
     * @param name
     *         element name
     * @param namesAndValues
     *         attribute name / attribute value pairs
     * @return the new element
     */
    public Element elementAt(int index, String name, String... namesAndValues)
    {
        assert InternalUtils.isNonBlank(name);
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
     * Adds and returns a new text node (the text node is returned so that {@link Text#write(String)} or [@link
     * {@link Text#writef(String, Object[])} may be invoked .
     *
     * @param text
     *         initial text for the node
     * @return the new Text node
     */
    public Text text(String text)
    {
        return newChild(new Text(this, text));
    }

    /**
     * Adds and returns a new CDATA node.
     *
     * @param content
     *         the content to be rendered by the node
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

        builder.append('<').append(prefixedElementName);

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
            if (namespace.equals(Document.XML_NAMESPACE_URI))
                continue;

            String prefix = namespaceToPrefix.get(namespace);

            builder.append(" xmlns");

            if (!prefix.equals(""))
            {
                builder.append(':').append(prefix);
            }

            builder.append('=');
            builder.append(markupModel.getAttributeQuote());

            markupModel.encodeQuoted(namespace, builder);

            builder.append(markupModel.getAttributeQuote());
        }

        EndTagStyle style = markupModel.getEndTagStyle(name);

        boolean hasChildren = hasChildren();

        String close = (!hasChildren && style == EndTagStyle.ABBREVIATE) ? "/>" : ">";

        builder.append(close);

        writer.print(builder.toString());

        if (hasChildren)
            writeChildMarkup(document, writer, localNamespacePrefixToURI);

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
        if (namespace == null || namespace.equals(""))
            return name;

        if (namespace.equals(Document.XML_NAMESPACE_URI))
            return "xml:" + name;

        String prefix = namespaceURIToPrefix.get(namespace);

        // This should never happen, because namespaces are automatically defined as needed.

        if (prefix == null)
            throw new IllegalArgumentException(String.format("No prefix has been defined for namespace '%s'.",
                    namespace));

        // The empty string indicates the default namespace which doesn't use a prefix.

        if (prefix.equals(""))
            return name;

        return prefix + ":" + name;
    }

    /**
     * Tries to find an element under this element (including itself) whose id is specified.
     * Performs a width-first
     * search of the document tree.
     *
     * @param id
     *         the value of the id attribute of the element being looked for
     * @return the element if found. null if not found.
     */
    public Element getElementById(final String id)
    {
        return getElementByAttributeValue("id", id);
    }

    /**
     * Tries to find an element under this element (including itself) whose given attribute has a given value.
     *
     * @param attributeName
     *         the name of the attribute of the element being looked for
     * @param attributeValue
     *         the value of the attribute of the element being looked for
     * @return the element if found. null if not found.
     * @since 5.2.3
     */
    public Element getElementByAttributeValue(final String attributeName, final String attributeValue)
    {
        assert attributeName != null;
        assert attributeValue != null;

        return getElement(new Predicate<Element>()
        {
            public boolean accept(Element e)
            {
                String elementId = e.getAttribute(attributeName);
                return attributeValue.equals(elementId);
            }
        });
    }

    /**
     * Tries to find an element under this element (including itself) accepted by the given predicate.
     *
     * @param predicate
     *         Predicate to accept the element
     * @return the element if found. null if not found.
     * @since 5.2.3
     */
    public Element getElement(Predicate<Element> predicate)
    {
        LinkedList<Element> queue = CollectionFactory.newLinkedList();

        queue.add(this);

        while (!queue.isEmpty())
        {
            Element e = queue.removeFirst();

            if (predicate.accept(e))
                return e;

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
     */
    public Element find(String path)
    {
        assert InternalUtils.isNonBlank(path);
        Element search = this;

        for (String name : TapestryInternalUtils.splitPath(path))
        {
            search = search.findChildWithElementName(name);

            if (search == null)
                break;
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
                            if (cursor instanceof Element)
                                return;

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
     * Adds one or more CSS class names to the "class" attribute.
     *
     * @param classNames
     *         one or more CSS class names
     * @return the element for further configuration
     * @deprecated Deprecated in 5.4, as this is now special behavior for the "class" attribute.
     */
    @Deprecated
    public Element addClassName(String... classNames)
    {
        for (String name : classNames)
        {
            attribute("class", name);
        }

        return this;
    }

    /**
     * Defines a namespace for this element, mapping a URI to a prefix. This will affect how namespaced elements and
     * attributes nested within the element are rendered, and will also cause <code>xmlns:</code> attributes (to define
     * the namespace and prefix) to be rendered.
     *
     * @param namespace
     *         URI of the namespace
     * @param namespacePrefix
     *         prefix
     * @return this element
     */
    public Element defineNamespace(String namespace, String namespacePrefix)
    {
        assert namespace != null;
        assert namespacePrefix != null;
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
        // Have to be careful because we'll be modifying the underlying list of children
        // as we work, so we need a copy of the children.

        List<Node> childrenCopy = CollectionFactory.newList(getChildren());

        for (Node child : childrenCopy)
        {
            child.moveBefore(this);
        }

        remove();
    }

    /**
     * Inserts {@code node} as the first child of this element, shifting any existing children
     * forward. If {@code node} is currently attached to another parent, it is detached first.
     * <p>
     * This is the anchor-centric complement to {@link Node#moveToTop(Element)}.
     *
     * @param node the node to prepend; must not be {@code null} or an ancestor of this element
     * @return this element, for method chaining
     * @throws IllegalArgumentException if {@code node} is {@code null} or is this element or an ancestor of it
     * @since 5.10
     */
    public Element prependChild(Node node)
    {
        if (node == null)
            throw new IllegalArgumentException("node must not be null");

        Node search = this;
        while (search != null)
        {
            if (search == node)
                throw new IllegalArgumentException("Cannot prepend a node to itself or one of its ancestors");
            search = search.container;
        }

        node.detach();
        insertChildAt(0, node);
        return this;
    }

    /**
     * Appends {@code node} as the last child of this element. If {@code node} is currently
     * attached to another parent, it is detached first.
     * <p>
     * This is the anchor-centric complement to {@link Node#moveToBottom(Element)}.
     *
     * @param node the node to append; must not be {@code null} or an ancestor of this element
     * @return this element, for method chaining
     * @throws IllegalArgumentException if {@code node} is {@code null} or is this element or an ancestor of it
     * @since 5.10
     */
    public Element appendChild(Node node)
    {
        if (node == null)
            throw new IllegalArgumentException("node must not be null");

        Node search = this;
        while (search != null)
        {
            if (search == node)
                throw new IllegalArgumentException("Cannot append a node to itself or one of its ancestors");
            search = search.container;
        }

        node.detach();
        addChild(node);
        return this;
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
        if (InternalUtils.isBlank(namespace))
            return;

        Map<String, String> current = holder.getResult();

        if (current.containsKey(namespace))
            return;

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

        if (children.isEmpty())
            return true;

        for (Node n : children)
        {
            if (n instanceof Text)
            {
                Text t = (Text) n;

                if (t.isEmpty())
                    continue;
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
     * @param visitor
     *         callback
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

    /**
     * Depth-first visitor traversal of this Element and <em>all</em> its descendant nodes,
     * including {@link Text}, {@link Comment}, {@link CData}, and {@link Raw} nodes. The traversal
     * order is the same as render order (pre-order).
     * <p>
     * All {@link NodeVisitor} methods have default no-op implementations, so implementors only
     * need to override the node types they are interested in.
     *
     * @param visitor callback
     * @since 5.10
     */
    public void visit(NodeVisitor visitor)
    {
        Stack<Node> queue = CollectionFactory.newStack();

        queue.push(this);

        while (!queue.isEmpty())
        {
            Node n = queue.pop();

            if (n instanceof Element)
            {
                Element e = (Element) n;
                visitor.visit(e);

                List<Node> children = CollectionFactory.newList();
                for (Node cursor = e.firstChild; cursor != null; cursor = cursor.nextSibling)
                    children.add(cursor);
                Collections.reverse(children);
                for (Node child : children)
                    queue.push(child);
            }
            else if (n instanceof Text)    visitor.visit((Text) n);
            else if (n instanceof Comment) visitor.visit((Comment) n);
            else if (n instanceof CData)   visitor.visit((CData) n);
            else if (n instanceof Raw)     visitor.visit((Raw) n);
        }
    }

    private void queueChildren(Stack<Element> queue)
    {
        if (firstChild == null)
            return;

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
        } else
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
     * Uses {@link DefaultMarkupModel} as a fallback when the element is not attached to a document.
     */
    public final String getChildMarkup()
    {
        PrintOutCollector collector = new PrintOutCollector();

        Document doc = getDocument();

        writeChildMarkup(doc != null ? doc : new Document(), collector.getPrintWriter(), null);

        return collector.getPrintOut();
    }

    /**
     * Returns an unmodifiable list of children for this element. Only {@link org.apache.tapestry5.dom.Element}s will
     * have children. Also, note that unlike W3C DOM, attributes are not represented as
     * {@link org.apache.tapestry5.dom.Node}s.
     *
     * @return unmodifiable list of children nodes
     */
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

    /**
     * Returns a deep copy of this element and its entire subtree, detached from any parent or
     * document.
     * <p>
     * The clone has no {@link #getContainer() container} and, for elements, no owning
     * {@link Document}. It needs to be attached to a tree before calling rendering or
     * document-traversal methods.
     *
     * @return a fully independent deep copy of this element
     * @since 5.10
     */
    @Override
    public Element deepClone()
    {
        Element clone = new Element((Element) null, namespace, name);
        copyInto(clone);
        return clone;
    }

    /**
     * Detaches this element from its containing element and returns it as an {@link Element},
     * allowing fluent use without a cast. If already detached, this is a no-op.
     *
     * @return this element, now detached
     * @since 5.10
     */
    @Override
    public Element detach()
    {
        return (Element) super.detach();
    }

    /**
     * Copies this element's namespace mappings, attributes, and children (recursively deep-cloned)
     * into {@code target}. Used internally by {@link #deepClone()} and
     * {@link Document#deepClone()}.
     */
    void copyInto(Element target)
    {
        if (namespaceToPrefix != null)
        {
            target.namespaceToPrefix = CollectionFactory.newMap();
            target.namespaceToPrefix.putAll(namespaceToPrefix);
        }

        // Rebuild the attribute linked-list in the same order.
        Attribute srcAttr = firstAttribute;
        Attribute prevCloneAttr = null;
        Attribute firstCloneAttr = null;

        while (srcAttr != null)
        {
            Attribute cloneAttr = new Attribute(target,
                    srcAttr.getNamespace(), srcAttr.getName(), srcAttr.getValue(), null);

            if (prevCloneAttr == null)
                firstCloneAttr = cloneAttr;
            else
                prevCloneAttr.nextAttribute = cloneAttr;

            prevCloneAttr = cloneAttr;
            srcAttr = srcAttr.nextAttribute;
        }

        target.firstAttribute = firstCloneAttr;

        // Recursively clone children.
        Node cursor = firstChild;
        while (cursor != null)
        {
            target.addChild(cursor.deepClone());
            cursor = cursor.nextSibling;
        }
    }

    /**
     * Returns the next sibling of this element that is itself an {@link Element}, skipping over
     * any intervening text, comment, or other non-element nodes. Returns {@code null} if no such
     * sibling exists, or if this element has no container (root element or detached).
     *
     * @return the next sibling element, or {@code null}
     * @since 5.10
     */
    public Element getNextSiblingElement()
    {
        Node cursor = nextSibling;

        while (cursor != null)
        {
            if (cursor instanceof Element)
                return (Element) cursor;

            cursor = cursor.nextSibling;
        }

        return null;
    }

    /**
     * Returns the previous sibling of this element that is itself an {@link Element}, skipping
     * over any intervening text, comment, or other non-element nodes. Returns {@code null} if no
     * such sibling exists, or if this element has no container (root element or detached).
     * <p>
     * Because the DOM uses a singly-linked sibling list, this method walks forward from the
     * parent's first child; it is O(n) in the number of siblings.
     *
     * @return the previous sibling element, or {@code null}
     * @since 5.10
     */
    public Element getPreviousSiblingElement()
    {
        if (container == null)
            return null;

        Element previousElement = null;
        Node cursor = container.firstChild;

        while (cursor != null)
        {
            if (cursor == this)
                return previousElement;

            if (cursor instanceof Element)
                previousElement = (Element) cursor;

            cursor = cursor.nextSibling;
        }

        throw new IllegalStateException("Element is not a child of its own container.");
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

                node.nextSibling = null;

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
            if (node == cursor)
                return index;

            cursor = cursor.nextSibling;
            index++;
        }

        throw new IllegalArgumentException("Node not a child of this element.");
    }

    /**
     * Returns the attributes for this Element as a (often empty) collection of
     * {@link org.apache.tapestry5.dom.Attribute}s. The order of the attributes within the collection is not specified.
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
