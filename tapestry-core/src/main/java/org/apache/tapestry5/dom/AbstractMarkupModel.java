//  Copyright 2008, 2009, 2010 The Apache Software Foundation
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

public abstract class AbstractMarkupModel implements MarkupModel
{
    private final boolean useApostropheForAttributes;
    private final char attributeQuoteChar;

    protected AbstractMarkupModel(boolean useApostropheForAttributes)
    {
        this.useApostropheForAttributes = useApostropheForAttributes;
        this.attributeQuoteChar = useApostropheForAttributes ? '\'' : '"';
    }

    public char getAttributeQuote()
    {
        return attributeQuoteChar;
    }

    /**
     * Passes all characters but '&lt;', '&gt;' and '&amp;' through unchanged.
     */
    public String encode(String content)
    {
        int length = content.length();

        StringBuilder builder = null;

        for (int i = 0; i < length; i++)
        {
            char ch = content.charAt(i);

            switch (ch)
            {
                case '<':

                    if (builder == null)
                    {
                        builder = new StringBuilder(2 * length);

                        builder.append(content.substring(0, i));
                    }

                    builder.append("&lt;");
                    continue;

                case '>':

                    if (builder == null)
                    {
                        builder = new StringBuilder(2 * length);

                        builder.append(content.substring(0, i));
                    }

                    builder.append("&gt;");
                    continue;

                case '&':

                    if (builder == null)
                    {
                        builder = new StringBuilder(2 * length);

                        builder.append(content.substring(0, i));
                    }

                    builder.append("&amp;");
                    continue;

                default:

                    if (builder != null)
                        builder.append(ch);
            }
        }

        return builder == null ? content : builder.toString();
    }

    public void encodeQuoted(String content, StringBuilder builder)
    {
        assert content != null;
        int length = content.length(), tokenStart = 0, i = 0;
        builder.ensureCapacity(builder.length() + length);
        String delimiter;
        for (; i < length; i++)
        {
            char ch = content.charAt(i);

            switch (ch)
            {
                case '<':

                    delimiter = "&lt;";
                    break;

                case '>':

                    delimiter = "&gt;";
                    break;

                case '&':

                    delimiter = "&amp;";
                    break;

                case '"':

                    if (!useApostropheForAttributes)
                    {
                        delimiter = "&quot;";
                        break;
                    }

                    continue;

                case '\'':

                    if (useApostropheForAttributes)
                    {
                        //TAP5-714
                        delimiter = "&#39;";
                        break;
                    }


                    continue;


                default:

                    continue;
            }

            if (tokenStart != i)
            {
                builder.append(content.subSequence(tokenStart, i));
            }

            builder.append(delimiter);

            tokenStart = i + 1;
        }

        if (tokenStart != length)
        {
            builder.append(content.subSequence(tokenStart, length));
        }
    }
}
