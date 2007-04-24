// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.OptionModel;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.Defense;

/** Shared utility methods used by various implementation classes. */
public class TapestryUtils
{
    private TapestryUtils()
    {
        // Prevent instantiation.
    }

    public static final void close(Closeable stream)
    {
        if (stream != null)
            try
            {
                stream.close();
            }
            catch (IOException ex)
            {
                // Ignore.
            }
    }

    /**
     * Converts the first character of a string to lowercase, leavining the rest of the string
     * unchanged.
     */
    public static String decapitalize(String input)
    {
        char first = input.charAt(0);

        return Character.toLowerCase(first) + input.substring(1);
    }

    /**
     * Capitalizes the string, and inserts a space before each upper case character (or sequence of
     * upper case characters). Thus "userId" becomes "User Id", etc.  Also, converts underscore
     * into space (and capitalizes the following word), thus "user_id" also becomes "User Id".
     */
    public static String toUserPresentable(String id)
    {
        StringBuilder builder = new StringBuilder(id.length() * 2);

        char[] chars = id.toCharArray();
        boolean postSpace = true;
        boolean upcaseNext = true;
        
        for (int i = 0; i < chars.length; i++)
        {
            char ch = chars[i];

            if (upcaseNext)
            {
                builder.append(Character.toUpperCase(ch));
                upcaseNext = false;
                
                continue;
            }
            
            if (ch == '_')
            {
                builder.append(' ');
                upcaseNext = true;
                continue;
            }

            boolean upperCase = Character.isUpperCase(ch);

            if (upperCase && !postSpace)
                builder.append(' ');

            builder.append(ch);

            postSpace = upperCase;
        }

        return builder.toString();
    }

    public static Map<String, String> mapFromKeysAndValues(String... keysAndValues)
    {
        Map<String, String> result = CollectionFactory.newMap();

        int i = 0;
        while (i < keysAndValues.length)
        {
            String key = keysAndValues[i++];
            String value = keysAndValues[i++];

            result.put(key, value);
        }

        return result;
    }

    /**
     * Converts a string to an {@link OptionModel}. The string is of the form "value=label". If the
     * equals sign is omitted, then the same value is used for both value and label.
     * 
     * @param input
     * @return
     */
    public static OptionModel toOptionModel(String input)
    {
        Defense.notNull(input, "input");

        int equalsx = input.indexOf('=');

        if (equalsx < 0)
            return new OptionModelImpl(input, false, input);

        String value = input.substring(0, equalsx);
        String label = input.substring(equalsx + 1);

        return new OptionModelImpl(label, false, value);
    }

    /**
     * Parses a string input into a series of value=label pairs compatible with
     * {@link #toOptionModel(String)}. Splits on commas. Ignores whitespace around commas.
     * 
     * @param input
     *            comma seperated list of terms
     * @return list of option models
     */
    public static List<OptionModel> toOptionModels(String input)
    {
        Defense.notNull(input, "input");

        List<OptionModel> result = newList();

        for (String term : input.split(","))
            result.add(toOptionModel(term.trim()));

        return result;
    }

    /**
     * Wraps the result of {@link #toOptionModels(String)} as a {@link SelectModel} (with no option
     * groups).
     * 
     * @param input
     * @return
     */
    public static SelectModel toSelectModel(String input)
    {
        List<OptionModel> options = toOptionModels(input);

        return new SelectModelImpl(null, options);
    }

    /**
     * Parses a key/value pair where the key and the value are seperated by an equals sign. The key
     * and value are trimmed of leading and trailing whitespace, and returned as a {@link KeyValue}.
     * 
     * @param input
     * @return
     */
    public static KeyValue parseKeyValue(String input)
    {
        int pos = input.indexOf('=');

        if (pos < 1)
            throw new IllegalArgumentException(InternalMessages.badKeyValue(input));

        String key = input.substring(0, pos);
        String value = input.substring(pos + 1);

        return new KeyValue(key.trim(), value.trim());
    }
}
