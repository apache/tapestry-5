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

package org.apache.tapestry5.ioc.internal.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String-related utilities used within various internal implementations of the Apache Tapestry subprojects.
 * Broken off Tapestry-IoC's InternalUtils.
 */
@SuppressWarnings("all")
public class InternalStringUtils
{
	
    /**
     * Pattern used to eliminate leading and trailing underscores and dollar signs.
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("^[_|$]*([\\p{javaJavaIdentifierPart}]+?)[_|$]*$",
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
        buffer.append(".");
        buffer.append(method.getName());
        buffer.append("(");

        for (int i = 0; i < method.getParameterTypes().length; i++)
        {
            if (i > 0)
                buffer.append(", ");

            String name = method.getParameterTypes()[i].getSimpleName();

            buffer.append(name);
        }

        return buffer.append(")").toString();
    }

    /**
     * Strips leading "_" and "$" and trailing "_" from the name.
     */
    public static String stripMemberName(String memberName)
    {
        assert isNonBlank(memberName);
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
        return join(elements, ", ");
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
                return elements.get(0).toString();

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
     * Returns true if the input is null, or is a zero length string (excluding leading/trailing whitespace).
     */

    public static boolean isBlank(String input)
    {
        return input == null || input.length() == 0 || input.trim().length() == 0;
    }

    public static boolean isNonBlank(String input)
    {
        return !isBlank(input);
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

}
