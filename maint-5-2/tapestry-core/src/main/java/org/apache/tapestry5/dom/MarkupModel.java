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

/**
 * Used by a the DOM to determine how to produce markup. Delegates details about converted entities and some formatting
 * details.  This exists to handle the differences between traditional HTML output (which is SGML based, meaning there
 * can be elements that are valid without a close tag) and "modern" XML, such as XHTML.  Generally speaking, for XHTML
 * it is vital that a !DOCTYPE be included in the rendered response, or the browser will be unable to display the result
 * properly.
 */
public interface MarkupModel
{
    /**
     * Encodes the characters, converting control characters (such as '&lt;') into corresponding entities (such as
     * &amp;lt;).
     *
     * @param content to be filtered
     * @return the filtered content
     */
    String encode(String content);

    /**
     * Encodes the characters into the buffer for use in a quoted value (that is, an attribute value), converting
     * control characters (such as '&lt;') into corresponding entities (such as &amp;lt;). In addition, double quotes
     * must be quoted or otherwise escaped.
     *
     * @param content to be filtered
     * @param buffer  to receive the filtered content
     */
    void encodeQuoted(String content, StringBuilder buffer);

    /**
     * For a given element, determines how the end tag for the element should be rendered.
     */
    EndTagStyle getEndTagStyle(String element);

    /**
     * Returns true if the document markup is XML, which is used to determine the need for an XML declaration at the
     * start of the document, and whether CDATA sections are supported.
     *
     * @return true for XML output, false for HTML output
     */
    boolean isXML();

    /**
     * What character is used when generating quotes around attribute values? This will be either a single or double
     * quote.
     *
     * @return single (') or double (") quote
     * @since 5.1.0.0
     */
    char getAttributeQuote();
}
