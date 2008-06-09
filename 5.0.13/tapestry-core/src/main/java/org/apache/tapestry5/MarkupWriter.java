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

package org.apache.tapestry5;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.dom.MarkupModel;
import org.apache.tapestry5.dom.Raw;

import java.io.PrintWriter;

/**
 * An interface used by objects, such as Tapestry components, that need to render themselves as some form of XML markup.
 * A markup writer maintains the idea of a current element. Attributes are added to the current element, and new text
 * and elements are placed inside the current element. In this way, the markup writer maintains a facade that XML markup
 * is generated as a stream, even though the implementation builds a kind of DOM tree. The DOM tree can be also be
 * manipulated. This solves a number of problems from Tapestry 4 (and earlier) where random access to the DOM was
 * desired and had to be simulated through complex buffering.
 */
public interface MarkupWriter
{
    /**
     * Begins a new element as a child of the current element. The new element becomes the current element. The new
     * Element is returned and can be directly manipulated (possibly at a later date). Optionally, attributes for the
     * new element can be specified directly.
     * <p/>
     *
     * @param name       the name of the element to create
     * @param attributes an even number of values, alternating names and values
     * @return the new DOM Element node
     * @see #attributes(Object[])
     */
    Element element(String name, Object... attributes);

    /**
     * Ends the current element. The new current element will be the parent element. Returns the new current element
     * (which may be null when ending the root element for the document).
     */

    Element end();

    /**
     * Writes the text as a child of the current element.
     */

    void write(String text);

    /**
     * Writes a formatted string.
     */
    void writef(String format, Object... args);

    /**
     * Writes <em>raw</em> text, text with existing markup that should be passed through the client without change. This
     * can be useful when the markup is read from an external source (a file or a database) and is simply to be
     * included.
     *
     * @param text
     * @see Raw
     */
    void writeRaw(String text);

    /**
     * Adds an XML comment. The text should be just the comment content, the comment delimiters will be provided.
     */
    void comment(String text);


    /**
     * Adds parsed character content. This will be enclosed in a CDATA block if supported.  When not supported, this is
     * the same as {@link #write(String)}.
     *
     * @param content pre-parsed content
     */
    void cdata(String content);

    /**
     * Adds a series of attributes and values. Null values are quietly skipped. If a name already has a value, then the
     * new value is <em>ignored</em>.
     */
    void attributes(Object... namesAndValues);

    /**
     * Converts the collected markup into an markup stream (according to rules provided by the {@link Document}'s {@link
     * MarkupModel}). The markup stream is sent to the writer.
     */
    void toMarkup(PrintWriter writer);

    /**
     * Returns the Document into which this writer creates elements or other nodes.
     */
    Document getDocument();

    /**
     * Returns the currently active element.
     */
    Element getElement();

    /**
     * Defines a namespace for the currently active element. The namespace URI will be mapped to the provided namespace
     * prefix within the Element.
     *
     * @param namespace       the namespace URI
     * @param namespacePrefix the prefix for elements and attributes associated with the namespace    (may be the empty
     *                        string for the default namespace)
     * @return the currently active element
     */
    Element defineNamespace(String namespace, String namespacePrefix);

    /**
     * Starts an element within the given namespace. The correct namespace prefix will be identified and used. Must be
     * balanced by a call to {@link #end()}.
     *
     * @param namespace   URI containing the element
     * @param elementName name of the element within the namespace
     * @return the new Element
     */
    Element elementNS(String namespace, String elementName);

    /**
     * Creates an attribute within the namespace for the current element.
     *
     * @param namespace      URI containing the element
     * @param attributeName  name of the attribute within the namespace
     * @param attributeValue the value for the attribute
     * @return the currently active element
     */
    Element attributeNS(String namespace, String attributeName, String attributeValue);

    /**
     * Adds a markup writer listener that will be notified as elements are started and ended.
     */
    void addListener(MarkupWriterListener listener);

    /**
     * Removes a previously added listener.
     */
    void removeListener(MarkupWriterListener listener);
}
