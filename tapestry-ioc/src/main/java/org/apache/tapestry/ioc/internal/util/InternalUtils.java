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

package org.apache.tapestry.ioc.internal.util;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.tapestry.ioc.Locatable;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.InjectService;

/**
 * Utilities used within various internal implemenations of Tapestry IOC and the rest of the
 * tapestry-core framework.
 */

public class InternalUtils
{
    /**
     * Leading punctiation on member names that is stripped off to form a property name or new
     * member name.
     */
    public static final String NAME_PREFIX = "_$";

    private InternalUtils()
    {
    }

    /**
     * Converts a method to a user presentable string consisting of the containing class name, the
     * method name, and the short form of the parameter list (the class name of each parameter type,
     * shorn of the package name portion).
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
            if (i > 0) buffer.append(", ");

            String name = method.getParameterTypes()[i].getSimpleName();

            int dotx = name.lastIndexOf('.');

            buffer.append(name.substring(dotx + 1));
        }

        return buffer.append(")").toString();
    }

    /** Returns the size of an object array, or null if the array is empty. */

    public static int size(Object[] array)
    {
        return array == null ? 0 : array.length;
    }

    /** Strips leading punctuation ("_" and "$") from the provided name. */
    public static String stripMemberPrefix(String memberName)
    {
        StringBuilder builder = new StringBuilder(memberName);

        // There may be other prefixes we want to strip off, at some point!

        // Strip off leading characters defined by NAME_PREFIX

        // This code is really ugly and needs to be fixed.

        while (true)
        {
            char ch = builder.charAt(0);

            if (InternalUtils.NAME_PREFIX.indexOf(ch) < 0) break;

            builder.deleteCharAt(0);
        }

        return builder.toString();
    }

    /**
     * Strips leading characters defined by {@link InternalUtils#NAME_PREFIX}, then adds the prefix
     * back in.
     */
    public static String createMemberName(String memberName)
    {
        return NAME_PREFIX + stripMemberPrefix(memberName);
    }

    /**
     * Converts an enumeration (of Strings) into a sorted list of Strings.
     */
    public static List<String> toList(Enumeration e)
    {
        List<String> result = newList();

        while (e.hasMoreElements())
        {
            String name = (String) e.nextElement();

            result.add(name);
        }

        Collections.sort(result);

        return result;
    }

    /**
     * Finds a specific annotation type within an array of annotations.
     * 
     * @param <T>
     * @param annotations
     *            to search
     * @param annotationClass
     *            to match
     * @return the annotation instance, if found, or null otherwise
     */
    public static <T extends Annotation> T findAnnotation(Annotation[] annotations,
            Class<T> annotationClass)
    {
        for (Annotation a : annotations)
        {
            if (annotationClass.isInstance(a)) return annotationClass.cast(a);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static Object calculateParameterValue(Class parameterType,
            Annotation[] parameterAnnotations, ServiceLocator locator,
            Map<Class, Object> parameterDefaults)
    {
        InjectService is = findAnnotation(parameterAnnotations, InjectService.class);

        if (is != null)
        {
            String serviceId = is.value();

            return locator.getService(serviceId, parameterType);
        }

        Inject i = findAnnotation(parameterAnnotations, Inject.class);

        if (i != null)
        {
            String reference = i.value();

            return locator.getObject(reference, parameterType);
        }

        // See if we have any "pre-determined" parameter type to object mappings

        Object result = parameterDefaults.get(parameterType);

        // This will return a non-null value, or throw an exception

        if (result == null) result = locator.getService(parameterType);

        // ... so the result is never null

        return result;
    }

    public static Object[] calculateParametersForMethod(Method method, ServiceLocator locator,
            Map<Class, Object> parameterDefaults)
    {
        Class[] parameterTypes = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();

        return InternalUtils.calculateParameters(
                locator,
                parameterDefaults,
                parameterTypes,
                annotations);
    }

    public static Object[] calculateParameters(ServiceLocator locator,
            Map<Class, Object> parameterDefaults, Class[] parameterTypes,
            Annotation[][] parameterAnnotations)
    {
        int parameterCount = parameterTypes.length;

        Object[] parameters = new Object[parameterCount];

        for (int i = 0; i < parameterCount; i++)
        {
            parameters[i] = calculateParameterValue(
                    parameterTypes[i],
                    parameterAnnotations[i],
                    locator,
                    parameterDefaults);
        }

        return parameters;
    }

    /** Joins together some number of elements to form a comma separated list. */
    public static String join(List elements)
    {
        StringBuilder buffer = new StringBuilder();
        boolean first = true;

        for (Object o : elements)
        {
            if (!first) buffer.append(", ");

            buffer.append(String.valueOf(o));

            first = false;
        }

        return buffer.toString();
    }

    /** Creates a sorted copy of the provided elements, then turns that into a comma separated list. */
    public static String joinSorted(Collection elements)
    {
        List<String> list = newList();

        for (Object o : elements)
            list.add(String.valueOf(o));

        Collections.sort(list);

        return join(list);
    }

    /**
     * Returns true if the input is null, or is a zero length string (excluding leading/trailing
     * whitespace).
     */

    public static boolean isBlank(String input)
    {
        return input == null || input.length() == 0 || input.trim().length() == 0;
    }

    public static boolean isNonBlank(String input)
    {
        return !isBlank(input);
    }

    /** Capitalizes a string, converting the first character to uppercase. */
    public static String capitalize(String input)
    {
        if (input.length() == 0) return input;

        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Sniffs the object to see if it is a {@link Location} or {@link Locatable}. Returns null if
     * null or not convertable to a location.
     */

    public static Location locationOf(Object location)
    {
        if (location == null) return null;

        if (location instanceof Location) return (Location) location;

        if (location instanceof Locatable) return ((Locatable) location).getLocation();

        return null;
    }

    /**
     * Extracts the string keys from a map and returns them in sorted order. The keys are converted
     * to strings.
     * 
     * @param map
     *            the map to extract keys from (may be null)
     * @return the sorted keys, or the empty set if map is null
     */

    public static List<String> sortedKeys(Map map)
    {
        if (map == null) return Collections.emptyList();

        List<String> keys = newList();

        for (Object o : map.keySet())
            keys.add(String.valueOf(o));

        Collections.sort(keys);

        return keys;
    }

    /**
     * Gets a value from a map (which may be null).
     * 
     * @param <K>
     * @param <V>
     * @param map
     *            the map to extract from (may be null)
     * @param key
     * @return the value from the map, or null if the map is null
     */

    public static <K, V> V get(Map<K, V> map, K key)
    {
        if (map == null) return null;

        return map.get(key);
    }

    /** Returns true if the method provided is a static method. */
    public static final boolean isStatic(Method method)
    {
        return Modifier.isStatic(method.getModifiers());
    }

    public static final <T> Iterator<T> reverseIterator(final List<T> list)
    {
        final ListIterator<T> normal = list.listIterator(list.size());

        return new Iterator<T>()
        {
            public boolean hasNext()
            {
                return normal.hasPrevious();
            }

            public T next()
            {
                // TODO Auto-generated method stub
                return normal.previous();
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }

        };
    }

    /** Return true if the input string contains the marker for symbols that must be expanded. */
    public static boolean containsSymbols(String input)
    {
        return input.contains("${");
    }
}
