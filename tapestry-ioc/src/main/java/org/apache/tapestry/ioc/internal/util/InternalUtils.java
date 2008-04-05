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

package org.apache.tapestry.ioc.internal.util;

import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.Locatable;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.InjectService;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import org.apache.tapestry.ioc.services.ClassFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Utilities used within various internal implemenations of Tapestry IOC and the rest of the tapestry-core framework.
 */

public class InternalUtils
{
    /**
     * Leading punctiation on member names that is stripped off to form a property name or new member name.
     */
    private static final String NAME_PREFIX = "_$";

    /**
     * Converts a method to a user presentable string using a {@link ClassFactory} to obtain a {@link Location} (where
     * possible). {@link #asString(Method)} is used under the covers, to present a detailed, but not excessive,
     * description of the class, method and parameters.
     *
     * @param method       method to convert to a string
     * @param classFactory used to obtain the {@link Location}
     * @return the method formatted for presentation to the user
     */
    public static String asString(Method method, ClassFactory classFactory)
    {
        Location location = classFactory.getMethodLocation(method);

        return location != null ? location.toString() : asString(method);
    }

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
            if (i > 0) buffer.append(", ");

            String name = method.getParameterTypes()[i].getSimpleName();

            buffer.append(name);
        }

        return buffer.append(")").toString();
    }

    /**
     * Returns the size of an object array, or null if the array is empty.
     */

    public static int size(Object[] array)
    {
        return array == null ? 0 : array.length;
    }

    /**
     * Strips leading punctuation ("_" and "$") from the provided name.
     */
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
     * Strips leading characters defined by {@link InternalUtils#NAME_PREFIX}, then adds the prefix back in.
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
     * @param annotations     to search
     * @param annotationClass to match
     * @return the annotation instance, if found, or null otherwise
     */
    public static <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<T> annotationClass)
    {
        for (Annotation a : annotations)
        {
            if (annotationClass.isInstance(a)) return annotationClass.cast(a);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static Object calculateParameterValue(Class parameterType, final Annotation[] parameterAnnotations,
                                                  ObjectLocator locator, Map<Class, Object> parameterDefaults)
    {
        AnnotationProvider provider = new AnnotationProvider()
        {
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return findAnnotation(parameterAnnotations, annotationClass);
            }

        };

        // At some point, it would be nice to eliminate InjectService, and rely
        // entirely on service interface type and point-of-injection markers.

        InjectService is = provider.getAnnotation(InjectService.class);

        if (is != null)
        {
            String serviceId = is.value();

            return locator.getService(serviceId, parameterType);
        }

        // In the absence of @InjectService, try some autowiring. First, does the
        // parameter type match on of the resources (the parameter defaults)?

        if (provider.getAnnotation(Inject.class) == null)
        {
            Object result = parameterDefaults.get(parameterType);

            if (result != null) return result;
        }

        // Otherwise, make use of the MasterObjectProvider service to resolve this type (plus
        // any other information gleaned from additional annotations) into the correct object.

        return locator.getObject(parameterType, provider);
    }

    public static Object[] calculateParametersForMethod(Method method, ObjectLocator locator,
                                                        Map<Class, Object> parameterDefaults)
    {
        Class[] parameterTypes = method.getParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();

        return calculateParameters(locator, parameterDefaults, parameterTypes, annotations);
    }

    public static Object[] calculateParametersForConstructor(Constructor constructor, ObjectLocator locator,
                                                             Map<Class, Object> parameterDefaults)
    {
        Class[] parameterTypes = constructor.getParameterTypes();
        Annotation[][] annotations = constructor.getParameterAnnotations();

        return calculateParameters(locator, parameterDefaults, parameterTypes, annotations);
    }

    public static Object[] calculateParameters(ObjectLocator locator, Map<Class, Object> parameterDefaults,
                                               Class[] parameterTypes, Annotation[][] parameterAnnotations)
    {
        int parameterCount = parameterTypes.length;

        Object[] parameters = new Object[parameterCount];

        for (int i = 0; i < parameterCount; i++)
        {
            parameters[i] = calculateParameterValue(parameterTypes[i], parameterAnnotations[i], locator,
                                                    parameterDefaults);
        }

        return parameters;
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
     * @param elements  objects to be joined together
     * @param separator used between elements when joining
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
                    if (!first) buffer.append(separator);

                    String string = String.valueOf(o);

                    if (string.equals("")) string = "(blank)";

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
        if (elements == null || elements.isEmpty()) return "(none)";

        List<String> list = newList();

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
        if (input.length() == 0) return input;

        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }

    /**
     * Sniffs the object to see if it is a {@link Location} or {@link Locatable}. Returns null if null or not
     * convertable to a location.
     */

    public static Location locationOf(Object location)
    {
        if (location == null) return null;

        if (location instanceof Location) return (Location) location;

        if (location instanceof Locatable) return ((Locatable) location).getLocation();

        return null;
    }

    /**
     * Extracts the string keys from a map and returns them in sorted order. The keys are converted to strings.
     *
     * @param map the map to extract keys from (may be null)
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
     * @param map the map to extract from (may be null)
     * @param key
     * @return the value from the map, or null if the map is null
     */

    public static <K, V> V get(Map<K, V> map, K key)
    {
        if (map == null) return null;

        return map.get(key);
    }

    /**
     * Returns true if the method provided is a static method.
     */
    public static boolean isStatic(Method method)
    {
        return Modifier.isStatic(method.getModifiers());
    }

    public static <T> Iterator<T> reverseIterator(final List<T> list)
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
        notBlank(input, "input");

        int dotx = input.lastIndexOf('.');

        if (dotx < 0) return input;

        return input.substring(dotx + 1);
    }

    /**
     * Searches a class for the "best" constructor, the public constructor with the most parameters. Returns null if
     * there are no public constructors. If there is more than one constructor with the maximum number of parameters, it
     * is not determined which will be returned (don't build a class like that!). In addition, if a constructor is
     * annotated with {@link org.apache.tapestry.ioc.annotations.Inject}, it will be used (no check for multiple such
     * constructors is made, only at most a single constructor should have the annotation).
     *
     * @param clazz to search for a constructor for
     * @return the constructor to be used to instantiate the class, or null if no appropriate constructor was found
     */
    public static Constructor findAutobuildConstructor(Class clazz)
    {
        Constructor[] constructors = clazz.getConstructors();

        switch (constructors.length)
        {
            case 1:

                return constructors[0];

            case 0:

                return null;

            default:
                break;
        }

        for (Constructor c : constructors)
        {
            if (c.getAnnotation(Inject.class) != null) return c;
        }

        // Choose a constructor with the most parameters.

        Comparator<Constructor> comparator = new Comparator<Constructor>()
        {
            public int compare(Constructor o1, Constructor o2)
            {
                return o2.getParameterTypes().length - o1.getParameterTypes().length;
            }
        };

        Arrays.sort(constructors, comparator);

        return constructors[0];
    }

    /**
     * Adds a value to a specially organized map where the values are lists of objects. This somewhat simulates a map
     * that allows mutiple values for the same key.
     *
     * @param map   to store value into
     * @param key   for which a value is added
     * @param value to add
     * @param <K>   the type of key
     * @param <V>   the type of the list
     */
    public static <K, V> void addToMapList(Map<K, List<V>> map, K key, V value)
    {
        List<V> list = map.get(key);

        if (list == null)
        {
            list = newList();
            map.put(key, list);
        }

        list.add(value);
    }

    /**
     * Validates that the marker annotation class had a retention policy of runtime.
     *
     * @param markerClass the marker annotation class
     */
    public static void validateMarkerAnnotation(Class markerClass)
    {
        Retention policy = (Retention) markerClass.getAnnotation(Retention.class);

        if (policy != null && policy.value() == RetentionPolicy.RUNTIME) return;

        throw new IllegalArgumentException(UtilMessages.badMarkerAnnotation(markerClass));
    }

    public static void validateMarkerAnnotations(Class[] markerClasses)
    {
        for (Class markerClass : markerClasses) validateMarkerAnnotation(markerClass);
    }

    public static void close(Closeable stream)
    {
        if (stream != null) try
        {
            stream.close();
        }
        catch (IOException ex)
        {
            // Ignore.
        }
    }
}
