//  Copyright 2008 The Apache Software Foundation
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
    /**
     * Passes all characters but '&lt;', '&gt;' and '&amp;' through unchanged.
     */
    public void encode(String content, StringBuilder buffer)
    {
        encode(content, false, buffer);
    }

    public String encode(String content)
    {
        StringBuilder buffer = new StringBuilder(content.length() * 2);

        encode(content, false, buffer);

        return buffer.toString();
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
}
