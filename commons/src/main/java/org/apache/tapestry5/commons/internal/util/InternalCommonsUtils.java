// Copyright 2006-2014 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.commons.internal.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.Locatable;
import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.commons.internal.NullAnnotationProvider;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.CommonsUtils;

/**
 * Utility methods class for the Commons package.
 */
public class InternalCommonsUtils {

    /**
     * @since 5.3
     */
    public final static AnnotationProvider NULL_ANNOTATION_PROVIDER = new NullAnnotationProvider();
    private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^\\w]");

    /**
     * Adds a value to a specially organized map where the values are lists of objects. This somewhat simulates a map
     * that allows multiple values for the same key.
     *
     * @param map
     *         to store value into
     * @param key
     *         for which a value is added
     * @param value
     *         to add
     * @param <K>
     *         the type of key
     * @param <V>
     *         the type of the list
     */
    public static <K, V> void addToMapList(Map<K, List<V>> map, K key, V value)
    {
        List<V> list = map.get(key);

        if (list == null)
        {
            list = CollectionFactory.newList();
            map.put(key, list);
        }

        list.add(value);
    }

    /**
     * Sniffs the object to see if it is a {@link Location} or {@link Locatable}. Returns null if null or not
     * convertable to a location.
     */
    
    public static Location locationOf(Object location)
    {
        if (location == null)
            return null;
    
        if (location instanceof Location)
            return (Location) location;
    
        if (location instanceof Locatable)
            return ((Locatable) location).getLocation();
    
        return null;
    }

    public static AnnotationProvider toAnnotationProvider(final Method element)
    {
        if (element == null)
            return NULL_ANNOTATION_PROVIDER;
    
        return new AnnotationProvider()
        {
            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return element.getAnnotation(annotationClass);
            }
        };
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

    public static String replace(String input, Pattern pattern, String replacement)
    {
        return pattern.matcher(input).replaceAll(replacement);
    }

    /**
     * Looks for a label within the messages based on the id. If found, it is used, otherwise the name is converted to a
     * user presentable form.
     */
    public static String defaultLabel(String id, Messages messages, String propertyExpression)
    {
        String key = id + "-label";
    
        if (messages.contains(key))
            return messages.get(key);
    
        return toUserPresentable(extractIdFromPropertyExpression(InternalCommonsUtils.lastTerm(propertyExpression)));
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
    
            if (upperCase && !postSpace)
                builder.append(' ');
    
            builder.append(ch);
    
            postSpace = upperCase;
        }
    
        return builder.toString();
    }

    /**
     * @since 5.3
     */
    public static AnnotationProvider toAnnotationProvider(final Class element)
    {
        return new AnnotationProvider()
        {
            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return annotationClass.cast(element.getAnnotation(annotationClass));
            }
        };
    }

    /**
     * Pattern used to eliminate leading and trailing underscores and dollar signs.
     */
    static final Pattern NAME_PATTERN = Pattern.compile("^[_|$]*([\\p{javaJavaIdentifierPart}]+?)[_|$]*$",
            Pattern.CASE_INSENSITIVE);

    /**
     * Converts a method to a user presentable string consisting of the containing class name, the method name, and the
     * short form of the parameter list (the class name of each parameter type, shorn of the package name portion).
     *
     * @param method
     * @return short string representation
     */
    public static String asString(Method method)
    {
        StringBuilder buffer = new StringBuilder();
    
        buffer.append(method.getDeclaringClass().getName());
        buffer.append('.');
        buffer.append(method.getName());
        buffer.append('(');
    
        for (int i = 0; i < method.getParameterTypes().length; i++)
        {
            if (i > 0)
                buffer.append(", ");
    
            String name = method.getParameterTypes()[i].getSimpleName();
    
            buffer.append(name);
        }
    
        return buffer.append(')').toString();
    }

    /**
     * Strips leading "_" and "$" and trailing "_" from the name.
     */
    public static String stripMemberName(String memberName)
    {
        assert InternalCommonsUtils.isNonBlank(memberName);
        Matcher matcher = NAME_PATTERN.matcher(memberName);
    
        if (!matcher.matches())
            throw new IllegalArgumentException(String.format("Input '%s' is not a valid Java identifier.", memberName));
    
        return matcher.group(1);
    }

    /**
     * Joins together some number of elements to form a comma separated list.
     */
    public static String join(List elements)
    {
        return InternalCommonsUtils.join(elements, ", ");
    }

    /**
     * Joins together some number of elements. If a value in the list is the empty string, it is replaced with the
     * string "(blank)".
     *
     * @param elements
     *         objects to be joined together
     * @param separator
     *         used between elements when joining
     */
    public static String join(List elements, String separator)
    {
        switch (elements.size())
        {
            case 0:
                return "";
    
            case 1:
                return String.valueOf(elements.get(0));
    
            default:
    
                StringBuilder buffer = new StringBuilder();
                boolean first = true;
    
                for (Object o : elements)
                {
                    if (!first)
                        buffer.append(separator);
    
                    String string = String.valueOf(o);
    
                    if (string.equals(""))
                        string = "(blank)";
    
                    buffer.append(string);
    
                    first = false;
                }
    
                return buffer.toString();
        }
    }

    /**
     * Creates a sorted copy of the provided elements, then turns that into a comma separated list.
     *
     * @return the elements converted to strings, sorted, joined with comma ... or "(none)" if the elements are null or
     *         empty
     */
    public static String joinSorted(Collection elements)
    {
        if (elements == null || elements.isEmpty())
            return "(none)";
    
        List<String> list = CollectionFactory.newList();
    
        for (Object o : elements)
            list.add(String.valueOf(o));
    
        Collections.sort(list);
    
        return join(list);
    }

    /**
     * Capitalizes a string, converting the first character to uppercase.
     */
    public static String capitalize(String input)
    {
        if (input.length() == 0)
            return input;
    
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    public static boolean isNonBlank(String input)
    {
        return !CommonsUtils.isBlank(input);
    }

    /**
     * Return true if the input string contains the marker for symbols that must be expanded.
     */
    public static boolean containsSymbols(String input)
    {
        return input.contains("${");
    }

    /**
     * Searches the string for the final period ('.') character and returns everything after that. The input string is
     * generally a fully qualified class name, though tapestry-core also uses this method for the occasional property
     * expression (which is also dot separated). Returns the input string unchanged if it does not contain a period
     * character.
     */
    public static String lastTerm(String input)
    {
        assert isNonBlank(input);
        int dotx = input.lastIndexOf('.');
    
        if (dotx < 0)
            return input;
    
        return input.substring(dotx + 1);
    }

    /**
     * Extracts the string keys from a map and returns them in sorted order. The keys are converted to strings.
     *
     * @param map
     *         the map to extract keys from (may be null)
     * @return the sorted keys, or the empty set if map is null
     */
    
    public static List<String> sortedKeys(Map map)
    {
        if (map == null)
            return Collections.emptyList();
    
        List<String> keys = CollectionFactory.newList();
    
        for (Object o : map.keySet())
            keys.add(String.valueOf(o));
    
        Collections.sort(keys);
    
        return keys;
    }

}
