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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.util.Set;

/**
 * Default implementation of {@link org.apache.tapestry.dom.MarkupModel} that is appropriate for traditional HTML
 * markup. This conforms to the SGML HTML definition, including some things that are not well formed XML-style markup.
 * Assumes that all tags are lower-case.
 */
public class DefaultMarkupModel implements MarkupModel
{
    private final Set<String> EMPTY_ELEMENTS = newSet("base", "br", "col", "frame", "hr", "img", "input", "link",
                                                      "meta", "option", "param");

    /**
     * Passes all characters but '&lt;', '&gt;' and '&amp;' through unchanged.
     */
    public void encode(String content, StringBuilder buffer)
    {
        encode(content, false, buffer);
    }

    public void encodeQuoted(String content, StringBuilder buffer)
    {
        encode(content, true, buffer);
    }

    private void encode(String content, boolean encodeQuotes, StringBuilder buffer)
    {
        char[] array = content.toCharArray();

        for (char ch : array)
        {
            switch (ch)
            {
                case '<':
                    buffer.append("&lt;");
                    continue;

                case '>':
                    buffer.append("&gt;");
                    continue;

                case '&':
                    buffer.append("&amp;");
                    continue;

                case '"':
                    if (encodeQuotes)
                    {
                        buffer.append("&quot;");
                        continue;
                    }

                default:
                    buffer.append(ch);
            }
        }
    }

    public EndTagStyle getEndTagStyle(String element)
    {
        boolean isEmpty = EMPTY_ELEMENTS.contains(element);

        return isEmpty ? EndTagStyle.OMIT : EndTagStyle.REQUIRE;
    }

    /**
     * Returns false.
     */
    public boolean isXML()
    {
        return false;
    }
}
