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

package org.apache.tapestry5.internal;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.tapestry5.OptionModel;
import org.apache.tapestry5.SelectModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry5.ioc.internal.util.Defense;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Shared utility methods used by various implementation classes.
 */
public class TapestryInternalUtils
{
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^\\w]");

    private static final URLCodec CODEC = new URLCodec()
    {

        private BitSet contextSafe = (BitSet) WWW_FORM_URL.clone();

        {
            // Servlet container does not decode '+' in path to ' ',
            // so we encode ' ' to %20, not to '+'.
            contextSafe.clear(' ');
        }

        @Override
        public byte[] encode(byte[] bytes)
        {
            return encodeUrl(contextSafe, bytes);
        }
    };

    private TapestryInternalUtils()
    {
        // Prevent instantiation.
    }

    /**
     * Capitalizes the string, and inserts a space before each upper case character (or sequence of upper case
     * characters). Thus "userId" becomes "User Id", etc. Also, converts underscore into space (and capitalizes the
     * following word), thus "user_id" also becomes "User Id".
     */
    public static String toUserPresentable(String id)
    {
        StringBuilder builder = new StringBuilder(id.length() * 2);

        char[] chars = id.toCharArray();
        boolean postSpace = true;
        boolean upcaseNext = true;

        for (char ch : chars)
        {
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

            if (upperCase && !postSpace) builder.append(' ');

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
     * Converts a string to an {@link OptionModel}. The string is of the form "value=label". If the equals sign is
     * omitted, then the same value is used for both value and label.
     *
     * @param input
     * @return
     */
    public static OptionModel toOptionModel(String input)
    {
        Defense.notNull(input, "input");

        int equalsx = input.indexOf('=');

        if (equalsx < 0) return new OptionModelImpl(input);

        String value = input.substring(0, equalsx);
        String label = input.substring(equalsx + 1);

        return new OptionModelImpl(label, value);
    }

    /**
     * Parses a string input into a series of value=label pairs compatible with {@link #toOptionModel(String)}. Splits
     * on commas. Ignores whitespace around commas.
     *
     * @param input comma seperated list of terms
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
     * Wraps the result of {@link #toOptionModels(String)} as a {@link SelectModel} (with no option groups).
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
     * Converts a map entry to an {@link OptionModel}.
     *
     * @param input
     * @return
     */
    public static OptionModel toOptionModel(Map.Entry input)
    {
        notNull(input, "input");

        String label = input.getValue() != null ? String.valueOf(input.getValue()) : "";

        return new OptionModelImpl(label, input.getKey());
    }

    /**
     * Processes a map input into a series of map entries compatible with {@link #toOptionModel(Map.Entry)}.
     *
     * @param input map of elements
     * @return list of option models
     */
    public static <K, V> List<OptionModel> toOptionModels(Map<K, V> input)
    {
        Defense.notNull(input, "input");

        List<OptionModel> result = newList();

        for (Map.Entry entry : input.entrySet())
            result.add(toOptionModel(entry));

        return result;
    }

    /**
     * Wraps the result of {@link #toOptionModels(Map)} as a {@link SelectModel} (with no option groups).
     *
     * @param input
     * @return
     */
    public static <K, V> SelectModel toSelectModel(Map<K, V> input)
    {
        List<OptionModel> options = toOptionModels(input);

        return new SelectModelImpl(null, options);
    }

    /**
     * Converts an object to an {@link OptionModel}.
     *
     * @param input
     * @return
     */
    public static OptionModel toOptionModel(Object input)
    {
        String label = (input != null ? String.valueOf(input) : "");

        return new OptionModelImpl(label, input);
    }

    /**
     * Processes a list input into a series of objects compatible with {@link #toOptionModel(Object)}.
     *
     * @param input list of elements
     * @return list of option models
     */
    public static <E> List<OptionModel> toOptionModels(List<E> input)
    {
        Defense.notNull(input, "input");

        List<OptionModel> result = newList();

        for (E element : input)
            result.add(toOptionModel(element));

        return result;
    }

    /**
     * Wraps the result of {@link #toOptionModels(List)} as a {@link SelectModel} (with no option groups).
     *
     * @param input
     * @return
     */
    public static <E> SelectModel toSelectModel(List<E> input)
    {
        List<OptionModel> options = toOptionModels(input);

        return new SelectModelImpl(null, options);
    }

    /**
     * Parses a key/value pair where the key and the value are seperated by an equals sign. The key and value are
     * trimmed of leading and trailing whitespace, and returned as a {@link KeyValue}.
     *
     * @param input
     * @return
     */
    public static KeyValue parseKeyValue(String input)
    {
        int pos = input.indexOf('=');

        if (pos < 1) throw new IllegalArgumentException(InternalMessages.badKeyValue(input));

        String key = input.substring(0, pos);
        String value = input.substring(pos + 1);

        return new KeyValue(key.trim(), value.trim());
    }


    /**
     * Used to convert a property expression into a key that can be used to locate various resources (Blocks, messages,
     * etc.). Strips out any punctuation characters, leaving just words characters (letters, number and the
     * underscore).
     *
     * @param expression a property expression
     * @return the expression with punctuation removed
     */
    public static String extractIdFromPropertyExpression(String expression)
    {
        return replace(expression, NON_WORD_PATTERN, "");
    }

    /**
     * Looks for a label within the messages based on the id. If found, it is used, otherwise the name is converted to a
     * user presentable form.
     */
    public static String defaultLabel(String id, Messages messages, String propertyExpression)
    {
        String key = id + "-label";

        if (messages.contains(key)) return messages.get(key);

        return toUserPresentable(extractIdFromPropertyExpression(lastTerm(propertyExpression)));
    }

    /**
     * Strips a dotted sequence (such as a property expression, or a qualified class name) down to the last term of that
     * expression, by locating the last period ('.') in the string.
     */
    public static String lastTerm(String input)
    {
        int dotx = input.lastIndexOf('.');

        return input.substring(dotx + 1);
    }

    /**
     * Converts an list of strings into a space-separated string combining them all, suitable for use as an HTML class
     * attribute value.
     *
     * @param classes classes to combine
     * @return the joined classes, or null if classes is empty
     */
    public static String toClassAttributeValue(List<String> classes)
    {
        if (classes.isEmpty()) return null;

        return InternalUtils.join(classes, " ");
    }


    /**
     * Converts an enum to a label string, allowing for overrides from a message catalog.
     * <p/>
     * <ul> <li>As key <em>prefix</em>.<em>name</em> if present.  Ex: "ElementType.LOCAL_VARIABLE" <li>As key
     * <em>name</em> if present, i.e., "LOCAL_VARIABLE". <li>As a user-presentable version of the name, i.e., "Local
     * Variable". </ul>
     *
     * @param messages the messages to search for the label
     * @param prefix
     * @param value    to get a label for
     * @return the label
     */
    public static String getLabelForEnum(Messages messages, String prefix, Enum value)
    {
        String name = value.name();

        String key = prefix + "." + name;

        if (messages.contains(key)) return messages.get(key);

        if (messages.contains(name)) return messages.get(name);

        return toUserPresentable(name.toLowerCase());
    }

    public static String getLabelForEnum(Messages messages, Enum value)
    {
        String prefix = lastTerm(value.getClass().getName());

        return getLabelForEnum(messages, prefix, value);
    }

    /**
     * Encodes a string for inclusion in a URL.  Slashes and percents are converted to "%25" and "%2F" respectively,
     * then the entire string is  URL encoded.
     *
     * @param input string to include, may not be blank
     * @return encoded input
     */
    public static String encodeContext(String input)
    {
        Defense.notBlank(input, "input");

        try
        {
            return CODEC.encode(escapePercentAndSlash(input));
        }
        catch (EncoderException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private static final String PERCENT = "%";
    private static final Pattern PERCENT_PATTERN = Pattern.compile(PERCENT);
    private static final String ENCODED_PERCENT = "%25";
    private static final Pattern ENCODED_PERCENT_PATTERN = Pattern.compile(ENCODED_PERCENT);

    private static final String SLASH = "/";
    private static final Pattern SLASH_PATTERN = Pattern.compile(SLASH);
    private static final String ENCODED_SLASH = "%2F";
    private static final Pattern ENCODED_SLASH_PATTERN = Pattern.compile(ENCODED_SLASH, Pattern.CASE_INSENSITIVE);

    /**
     * Encodes percent and slash characters in the string for later decoding via {@link
     * #unescapePercentAndSlash(String)}.
     *
     * @param input string to encode
     * @return modified string
     */
    public static String escapePercentAndSlash(String input)
    {
        return replace(replace(input, PERCENT_PATTERN, ENCODED_PERCENT), SLASH_PATTERN, ENCODED_SLASH);
    }

    /**
     * Used to decode certain escaped characters that are replaced when using {@link #encodeContext(String)}}.
     *
     * @param input a previously encoded string
     * @return the string with slash and percent characters restored
     */
    public static String unescapePercentAndSlash(String input)
    {
        return replace(replace(input, ENCODED_SLASH_PATTERN, SLASH), ENCODED_PERCENT_PATTERN, PERCENT);
    }

    private static String replace(String input, Pattern pattern, String replacement)
    {
        return pattern.matcher(input).replaceAll(replacement);
    }

    /**
     * Determines if the two values are equal. They are equal if they are the exact same value (including if they are
     * both null). Otherwise standard equals() comparison is used.
     *
     * @param <T>
     * @param left  value to compare, possibly null
     * @param right value to compare, possibly null
     * @return true if same value, both null, or equal
     */
    public static <T> boolean isEqual(T left, T right)
    {
        if (left == right) return true;

        if (left == null) return right == null;

        return left.equals(right);
    }


    /**
     * Splits a path at each slash.
     */
    public static String[] splitPath(String path)
    {
        return SLASH_PATTERN.split(path);
    }
}
