//  Copyright 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.internal.util.Defense;

public abstract class AbstractMarkupModel implements MarkupModel
{
    private final boolean useApostropheForAttributes;

    protected AbstractMarkupModel(boolean useApostropheForAttributes)
    {
        this.useApostropheForAttributes = useApostropheForAttributes;
    }

    public char getAttributeQuote()
    {
        return useApostropheForAttributes ? '\'' : '"';
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
        Defense.notNull(content, "content");

        int length = content.length();

        for (int i = 0; i < length; i++)
        {
            char ch = content.charAt(i);

            switch (ch)
            {
                case '<':

                    builder.append("&lt;");
                    continue;

                case '>':

                    builder.append("&gt;");
                    continue;

                case '&':

                    builder.append("&amp;");
                    continue;

                case '"':

                    if (!useApostropheForAttributes)
                    {
                        builder.append("&quot;");
                        continue;
                    }

                    builder.append(ch);
                    continue;

                case '\'':

                    if (useApostropheForAttributes)
                    {
                        builder.append("&apos;");
                        continue;
                    }


                    // Fall through

                default:

                    builder.append(ch);
            }
        }
    }
}
