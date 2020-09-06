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

package org.apache.tapestry5.test.ioc;

import org.testng.Assert;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Extra assertions on top of the standard set, packaged as a base class for easy referencing in tests. Also,
 * utilities for instantiation objects and setting and reading private fields of those objects.
 *
 * This class was originally in the tapestry-ioc module as was moved to tapestry-test; the package name was not changed
 * to ensure backwards compatibility.
 * 
 * @since 5.2.0
 * @deprecated In 5.4, with no replacement
 */
public class TestUtils extends Assert
{

    /**
     * Invoked from code that should not be reachable. For example, place a call to unreachable() after invoking a
     * method that is expected to throw an exception.
     */
    public static void unreachable()
    {
        fail("This code should not be reachable.");
    }

    /**
     * Asserts that the message property of the throwable contains each of the provided substrings.
     * 
     * @param t
     *            throwable to check
     * @param substrings
     *            some number of expected substrings
     */
    public static void assertMessageContains(Throwable t, String... substrings)
    {
        String message = t.getMessage();

        for (String substring : substrings)
            assertTrue(message.contains(substring), String.format("String '%s' not found in '%s'.", substring, message));
    }

    /**
     * Compares two lists for equality; first all the elements are individually compared for equality (if the lists are
     * of unequal length, only elements up to the shorter length are compared). Then the length of the lists are
     * compared. This generally gives
     * 
     * @param <T>
     *            type of objects to compare
     * @param actual
     *            actual values to check
     * @param expected
     *            expected values
     */
    public static <T> void assertListsEquals(List<T> actual, List<T> expected)
    {
        int count = Math.min(actual.size(), expected.size());

        try
        {
            for (int i = 0; i < count; i++)
            {
                assertEquals(actual.get(i), expected.get(i), String.format("Element #%d.", i));
            }

            assertEquals(actual.size(), expected.size(), "List size.");
        }
        catch (AssertionError ae)
        {
            showLists(actual, expected);

            throw ae;
        }
    }

    protected static <T> void showLists(List<T> actual, List<T> expected)
    {
        List<String> actualStrings = toStrings(actual);
        List<String> expectedStrings = toStrings(expected);

        String format = String
                .format("%%3d: [%%-%ds] [%%-%ds]\n", maxLength(actualStrings), maxLength(expectedStrings));

        int count = Math.max(actual.size(), expected.size());

        System.out.flush();
        System.err.flush();

        System.err.println("List results differ (actual  vs. expected):");

        for (int i = 0; i < count; i++)
        {
            System.err.printf(format, i, get(actualStrings, i), get(expectedStrings, i));
        }
    }

    private static String get(List<String> list, int index)
    {
        if (index < list.size())
            return list.get(index);

        return "";
    }

    private static int maxLength(List<String> list)
    {
        int result = 0;

        for (String s : list)
        {
            result = Math.max(result, s.length());
        }

        return result;
    }

    private static <T> List<String> toStrings(List<T> list)
    {
        List<String> result = new ArrayList<String>();

        for (T t : list)
        {
            result.add(String.valueOf(t));
        }

        return result;
    }

    /**
     * Convenience for {@link #assertListsEquals(List, List)}.
     * 
     * @param <T>
     *            type of objects to compare
     * @param actual
     *            actual values to check
     * @param expected
     *            expected values
     */
    public static <T> void assertListsEquals(List<T> actual, T... expected)
    {
        assertListsEquals(actual, Arrays.asList(expected));
    }

    /**
     * Convenience for {@link #assertListsEquals(List, List)}.
     * 
     * @param <T>
     *            type of objects to compare
     * @param actual
     *            actual values to check
     * @param expected
     *            expected values
     */
    public static <T> void assertArraysEqual(T[] actual, T... expected)
    {
        assertListsEquals(Arrays.asList(actual), expected);
    }

    /**
     * Initializes private fields (via reflection).
     * 
     * @param object
     *            object to be updated
     * @param fieldValues
     *            string field names and corresponding field values
     * @return the object
     */
    public static <T> T set(T object, Object... fieldValues)
    {
        assert object != null;
        Class objectClass = object.getClass();

        for (int i = 0; i < fieldValues.length; i += 2)
        {
            String fieldName = (String) fieldValues[i];
            Object fieldValue = fieldValues[i + 1];

            try
            {
                Field field = findField(objectClass, fieldName);

                field.setAccessible(true);

                field.set(object, fieldValue);
            }
            catch (Exception ex)
            {
                throw new RuntimeException(String.format("Unable to set field '%s' of %s to %s: %s", fieldName, object,
                        fieldValue, toMessage(ex)), ex);
            }
        }

        return object;
    }

    /**
     * Reads the content of a private field.
     * 
     * @param object
     *            to read the private field from
     * @param fieldName
     *            name of field to read
     * @return value stored in the field
     * @since 5.1.0.5
     */
    public static Object get(Object object, String fieldName)
    {
        assert object != null;

        try
        {
            Field field = findField(object.getClass(), fieldName);

            field.setAccessible(true);

            return field.get(object);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(String.format("Unable to read field '%s' of %s: %s", fieldName, object,
                    toMessage(ex)), ex);
        }
    }

    private static String toMessage(Throwable exception)
    {
        String message = exception.getMessage();

        if (message != null)
            return message;

        return exception.getClass().getName();
    }

    private static Field findField(Class objectClass, String fieldName)
    {

        Class cursor = objectClass;

        while (cursor != null)
        {
            try
            {
                return cursor.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException ex)
            {
                // Ignore.
            }

            cursor = cursor.getSuperclass();
        }

        throw new RuntimeException(String.format("Class %s does not contain a field named '%s'.",
                objectClass.getName(), fieldName));
    }

    /**
     * Creates a new instance of the object using its default constructor, and initializes it (via
     * {@link #set(Object, Object[])}).
     * 
     * @param objectType
     *            typeof object to instantiate
     * @param fieldValues
     *            string field names and corresponding field values
     * @return the initialized instance
     */
    public static <T> T create(Class<T> objectType, Object... fieldValues)
    {
        T result = null;

        try
        {
            result = objectType.newInstance();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(String.format("Unable to instantiate instance of %s: %s", objectType.getName(),
                    toMessage(ex)), ex);
        }

        return set(result, fieldValues);
    }

}
