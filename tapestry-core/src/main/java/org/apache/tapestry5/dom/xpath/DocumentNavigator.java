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

import java.util.Iterator;

import org.apache.tapestry5.dom.Attribute;
import org.apache.tapestry5.dom.CData;
import org.apache.tapestry5.dom.Comment;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.Node;
import org.apache.tapestry5.dom.Text;
import org.jaxen.BaseXPath;
import org.jaxen.DefaultNavigator;
import org.jaxen.JaxenConstants;
import org.jaxen.JaxenException;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.saxpath.SAXPathException;
import org.jaxen.util.SingleObjectIterator;

/**
 * Jaxen {@link org.jaxen.Navigator} implementation that adapts Tapestry's DOM
 * nodes.
 *
 * <p><strong>Intentionally unsupported:</strong> namespace nodes and processing
 * instructions, because Tapestry's DOM does not model either.</p>
 *
 * @since 5.10
 * @see XPath
 */
class DocumentNavigator extends DefaultNavigator
{
    public static final DocumentNavigator INSTANCE = new DocumentNavigator();

    private static final int COMMENT_PREFIX_LENGTH = "<!--".length();
    private static final int COMMENT_SUFFIX_LENGTH = "-->".length();

    /**
     * Returns an iterator over the attributes of {@code contextNode}.
     * <p>
     * Only {@link Element} nodes have attributes, all other node types
     * yield an empty iterator.
     *
     * @param contextNode the node whose attribute axis is requested
     * @return iterator of {@link Attribute} objects, or an empty iterator
     */
    @Override
    public Iterator<?> getAttributeAxisIterator(Object contextNode)
    {
        if (contextNode instanceof Element)
        {
            return ((Element) contextNode).getAttributes().iterator();
        }

        return JaxenConstants.EMPTY_ITERATOR;
    }

    /**
     * Returns an iterator over the children of {@code contextNode}.
     * <ul>
     * <li>{@link Document}: its single root {@link Element}.</li>
     * <li>{@link Element}: all child {@link Node}s in document order.</li>
     * <li>Other node types: empty iterator (leaf nodes).</li>
     * </ul>
     *
     * @param contextNode the node whose child axis is requested
     * @return iterator of child nodes, or an empty iterator
     */
    @Override
    public Iterator<?> getChildAxisIterator(Object contextNode)
    {
        if (contextNode instanceof Document)
        {
            return new SingleObjectIterator(((Document) contextNode).getRootElement());
        }

        if (contextNode instanceof Element)
        {
            return ((Element) contextNode).getChildren().iterator();
        }

        return JaxenConstants.EMPTY_ITERATOR;
    }

    /**
     * Returns an iterator over the parent of {@code contextNode}.
     * <ul>
     * <li>Non-root {@link Element}: the containing {@link Element}.</li>
     * <li>Root {@link Element}: the owning {@link Document}.</li>
     * <li>{@link Text}, {@link Comment}, {@link CData}, {@link org.apache.tapestry5.dom.Raw}:
     *     the containing {@link Element}.</li>
     * <li>{@link Document} and anything else: delegates to the default
     *     implementation (returns empty).</li>
     * </ul>
     *
     * @param contextNode the node whose parent axis is requested
     * @return single-element iterator containing the parent node, or an empty iterator
     * @throws UnsupportedAxisException if the axis is not supported for the node type
     */
    @Override
    public Iterator<?> getParentAxisIterator(Object contextNode) throws UnsupportedAxisException
    {
        if (contextNode instanceof Element)
        {
            Element e = (Element) contextNode;
            Element parent = e.getContainer();
            // Root element's logical parent in XPath is the Document, not null
            return new SingleObjectIterator(parent != null ? parent : e.getDocument());
        }

        if (contextNode instanceof Node)
        {
            // Text, Comment, CData, Raw: parent is always their containing Element
            Element container = ((Node) contextNode).getContainer();
            return container != null
                    ? new SingleObjectIterator(container)
                    : JaxenConstants.EMPTY_ITERATOR;
        }

        return super.getParentAxisIterator(contextNode);
    }

    /**
     * Returns the {@link Document} that owns {@code contextNode}.
     *
     * @param contextNode any {@link Node} in the document
     * @return the owning {@link Document}; never {@code null}
     */
    @Override
    public Object getDocumentNode(Object contextNode)
    {
        return ((Node) contextNode).getDocument();
    }

    /**
     * Looks up an element by its {@code id} attribute within the document that
     * contains {@code contextNode}.
     *
     * @param contextNode any {@link Node} used to locate the owning {@link Document}
     * @param elementId   the value of the {@code id} attribute to search for
     * @return the matching {@link Element}, or {@code null} if not found
     */
    @Override
    public Object getElementById(Object contextNode, String elementId)
    {
        return ((Node) contextNode).getDocument().getElementById(elementId);
    }

    /**
     * Namespace nodes are not supported.
     * Always returns {@code null}.
     *
     * @param contextNode ignored
     * @return {@code null}
     */
    @Override
    public String getNamespacePrefix(Object contextNode)
    {
        return null;
    }

    /**
     * Namespace nodes are not supported.
     * Always returns {@code null}.
     *
     * @param contextNode ignored
     * @return {@code null}
     */
    @Override
    public String getNamespaceStringValue(Object contextNode)
    {
        return null;
    }

    /**
     * Returns the local name of {@code element}.
     *
     * @param element an {@link Element} node
     * @return the element's local name; never {@code null}
     */
    @Override
    public String getElementName(Object element)
    {
        return ((Element) element).getName();
    }

    /**
     * Namespace URIs are not supported.
     * Always returns {@code null}.
     *
     * @param arg0 ignored
     * @return {@code null}
     */
    @Override
    public String getElementNamespaceUri(Object arg0)
    {
        return null;
    }

    /**
     * Qualified names are not supported.
     * Always returns {@code null}.
     *
     * @param contextNode ignored
     * @return {@code null}
     */
    @Override
    public String getElementQName(Object contextNode)
    {
        return null;
    }

    /**
     * Returns the XPath string-value of {@code element}, which is the concatenated
     * text of the element's rendered child markup.
     *
     * @param element an {@link Element} node
     * @return the element's child markup string, never {@code null}
     */
    @Override
    public String getElementStringValue(Object element)
    {
        return ((Element) element).getChildMarkup();
    }

    /**
     * Returns the local name of {@code attr}.
     *
     * @param attr an {@link Attribute}
     * @return the attribute's name, never {@code null}
     */
    @Override
    public String getAttributeName(Object attr)
    {
        return ((Attribute) attr).getName();
    }

    /**
     * Namespace URIs are not supported.
     * Always returns {@code null}.
     *
     * @param contextNode ignored
     * @return {@code null}
     */
    @Override
    public String getAttributeNamespaceUri(Object contextNode)
    {
        return null;
    }

    /**
     * Qualified names are not supported.
     * Always returns {@code null}.
     *
     * @param contextNode ignored
     * @return {@code null}
     */
    @Override
    public String getAttributeQName(Object contextNode)
    {
        return null;
    }

    /**
     * Returns the value of {@code attr}.
     *
     * @param attr an {@link Attribute}
     * @return the attribute's value; never {@code null}
     */
    @Override
    public String getAttributeStringValue(Object attr)
    {
        return ((Attribute) attr).getValue();
    }

    /**
     * Returns the text content of {@code comment}.
     * The comment string gets stripped of its {@code <!--} prefix and
     * {@code -->} suffix.
     *
     * @param comment a {@link Comment} node
     * @return the comment's inner text, including any surrounding whitespace
     */
    @Override
    public String getCommentStringValue(Object comment)
    {
        String fullComment = ((Comment) comment).toString();
        return fullComment.substring(COMMENT_PREFIX_LENGTH, fullComment.length() - COMMENT_SUFFIX_LENGTH);
    }

    /**
     * Returns the string value of a text or CDATA node.
     * Both {@link Text} and {@link CData} are treated as XPath text nodes.
     *
     * @param text a {@link Text} or {@link CData} node
     * @return the node's text content, never {@code null}
     */
    @Override
    public String getTextStringValue(Object text)
    {
        if (text instanceof CData)
        {
            return ((CData) text).getContent();
        }

        return ((Text) text).toString();
    }

    /**
     * Returns whether the given object is an attribute node.
     *
     * @param object the object to test
     * @return {@code true} if {@code object} is an {@link Attribute}
     */
    @Override
    public boolean isAttribute(Object object)
    {
        return object instanceof Attribute;
    }

    /**
     * Returns whether the given object is a comment node.
     *
     * @param object the object to test
     * @return {@code true} if {@code object} is a {@link Comment}
     */
    @Override
    public boolean isComment(Object object)
    {
        return object instanceof Comment;
    }

    /**
     * Returns whether the given object is a document node.
     *
     * @param object the object to test
     * @return {@code true} if {@code object} is a {@link Document}
     */
    @Override
    public boolean isDocument(Object object)
    {
        return object instanceof Document;
    }

    /**
     * Returns whether the given object is an element node.
     *
     * @param object the object to test
     * @return {@code true} if {@code object} is an {@link Element}
     */
    @Override
    public boolean isElement(Object object)
    {
        return object instanceof Element;
    }

    /**
     * Namespace nodes are not supported.
     * Always returns {@code false}.
     *
     * @param object ignored
     * @return {@code false}
     */
    @Override
    public boolean isNamespace(Object object)
    {
        return false;
    }

    /**
     * Processing instructions are not supported.
     * Always returns {@code false}.
     *
     * @param object ignored
     * @return {@code false}
     */
    @Override
    public boolean isProcessingInstruction(Object object)
    {
        return false;
    }

    /**
     * Returns {@code true} for {@link Text} and {@link CData} nodes.
     * CDATA sections are text nodes in the XPath data model.
     *
     * @param object the object to test
     * @return {@code true} if {@code object} is a {@link Text} or {@link CData}
     */
    @Override
    public boolean isText(Object object)
    {
        return object instanceof Text || object instanceof CData;
    }

    /**
     * Parses {@code xpath} and returns a Jaxen {@link org.jaxen.XPath} instance backed
     * by this navigator. Used internally by Jaxen to evaluate sub-expressions such as
     * those inside predicates.
     *
     * @param xpath XPath expression string
     * @return compiled Jaxen {@link org.jaxen.XPath}
     * @throws SAXPathException if the expression cannot be parsed
     */
    @Override
    public org.jaxen.XPath parseXPath(String xpath) throws SAXPathException
    {
        try
        {
            return new BaseXPath(xpath, INSTANCE);
        }
        catch (JaxenException e)
        {
            throw new SAXPathException(e.getMessage());
        }
    }
}
