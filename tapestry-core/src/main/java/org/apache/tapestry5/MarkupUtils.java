// Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.Collections;
import java.util.List;

/**
 * Utility methods related to generating markup.
 */
public class MarkupUtils
{
    static final char APOS = '\'';
    static final char QUOTE = '"';
    static final char SLASH = '\\';

    /**
     * Quotes the provided value as a JavaScript string literal. The input value is surrounded by single quotes and any
     * interior backslash, single or double quotes are escaped (a preceding backslash is added).
     *
     * @param text
     * @return quoted text
     */
    public static String quote(String text)
    {
        StringBuilder result = new StringBuilder(text.length() * 2);

        result.append(APOS);

        for (char ch : text.toCharArray())
        {
            switch (ch)
            {
                case APOS:
                case QUOTE:
                case SLASH:

                    result.append(SLASH);

                default:
                    result.append(ch);
                    break;
            }
        }

        result.append(APOS);

        return result.toString();
    }

    /**
     * Joins together several strings, sorting them alphabetically and separating them with spaces. This is often used
     * when setting the CSS class attribute of an element.
     */
    public static String join(String... values)
    {
        List<String> list = CollectionFactory.newList(values);

        return sortAndJoin(list);
    }

    /**
     * Joins together several strings, sorting them alphabetically and separating them with spaces. This is often used
     * when setting the CSS class attribute of an element.
     */
    public static String join(List<String> values)
    {
        List<String> copy = CollectionFactory.newList(values);

        return sortAndJoin(copy);
    }

    static String sortAndJoin(List<String> list)
    {
        Collections.sort(list);

        StringBuilder builder = new StringBuilder(10 * list.size());

        String sep = "";

        for (String name : list)
        {
            builder.append(sep);
            builder.append(name);

            sep = " ";
        }

        return builder.toString();
    }
}
