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

package org.apache.tapestry5.ioc.test;

import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.easymock.*;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Manages a set of EasyMock mock objects. Used as a base class for test cases.
 * <p/>
 * Extends from {@link org.testng.Assert} to bring in all the public static assert methods without requiring extra
 * imports.
 * <p/>
 * Provides a common mock factory method, {@link #newMock(Class)}. A single <em>standard</em> mock control is used for
 * all mock objects. Standard mocks do not care about the exact order in which methods are invoked, though they are as
 * rigourous as strict mocks when checking that parameters are the correct values.
 * <p/>
 * This base class is created with the intention of use within a TestNG test suite; if using JUnit, you can get the same
 * functionality using {@link MockTester}.
 * <p/>
 * This class is thread safe (it uses a thread local to store the mock control). In theory, this should allow TestNG to
 * execute tests in parallel.
 *
 * @see org.easymock.EasyMock#createControl()
 * @see org.apache.tapestry5.ioc.test.MockTester
 */
public class TestBase extends Assert
{
    private static class ThreadLocalControl extends ThreadLocal<IMocksControl>
    {
        @Override
        protected IMocksControl initialValue()
        {
            return EasyMock.createControl();
        }
    }

    private final MockTester tester = new MockTester();

    /**
     * Returns the {@link IMocksControl} for this thread.
     */
    protected final IMocksControl getMocksControl()
    {
        return tester.getMocksControl();
    }

    /**
     * Discards any mock objects created during the test.
     */
    @AfterMethod(alwaysRun = true)
    public final void discardMockControl()
    {
        tester.cleanup();
    }

    /**
     * Creates a new mock object of the indicated type. The shared mock control does <strong>not</strong> check order,
     * but does fail on any unexpected method invocations.
     *
     * @param <T>       the type of the mock object
     * @param mockClass the class to mock
     * @return the mock object, ready for training
     */
    protected final <T> T newMock(Class<T> mockClass)
    {
        return tester.newMock(mockClass);
    }

    /**
     * Switches each mock object created by {@link #newMock(Class)} into replay mode (out of the initial training
     * mode).
     */
    protected final void replay()
    {
        tester.replay();
    }

    /**
     * Verifies that all trained methods have been invoked on all mock objects (created by {@link #newMock(Class)}, then
     * switches each mock object back to training mode.
     */
    protected final void verify()
    {
        tester.verify();
    }

    /**
     * Convienience for {@link EasyMock#expectLastCall()} with {@link IExpectationSetters#andThrow(Throwable)}.
     *
     * @param throwable the exception to be thrown by the most recent method call on any mock
     */
    protected static void setThrowable(Throwable throwable)
    {
        EasyMock.expectLastCall().andThrow(throwable);
    }

    /**
     * Convienience for {@link EasyMock#expectLastCall()} with {@link IExpectationSetters#andAnswer(org.easymock.IAnswer)}.
     *
     * @param answer callback for the most recent method invocation
     */
    protected static void setAnswer(IAnswer answer)
    {
        EasyMock.expectLastCall().andAnswer(answer);
    }

    /**
     * Invoked from code that should not be reachable. For example, place a call to unreachable() after invoking a
     * method that is expected to throw an exception.
     */

    protected static void unreachable()
    {
        fail("This code should not be reachable.");
    }

    /**
     * Convienience for {@link EasyMock#expect(Object)}.
     *
     * @param <T>
     * @param value
     * @return expectation setter, for setting return value, etc.
     */
    @SuppressWarnings("unchecked")
    protected static <T> IExpectationSetters<T> expect(T value)
    {
        return EasyMock.expect(value);
    }

    /**
     * Asserts that the message property of the throwable contains each of the provided substrings.
     *
     * @param t          throwable to check
     * @param substrings some number of expected substrings
     */
    protected static void assertMessageContains(Throwable t, String... substrings)
    {
        String message = t.getMessage();

        for (String substring : substrings)
            assertTrue(message.contains(substring),
                       String.format("String '%s' not found in '%s'.", substring, message));
    }

    /**
     * Compares two lists for equality; first all the elements are individually compared for equality (if the lists are
     * of unequal length, only elements up to the shorter length are compared). Then the length of the lists are
     * compared. This generally gives
     *
     * @param <T>      type of objects to compare
     * @param actual   actual values to check
     * @param expected expected values
     */
    protected static <T> void assertListsEquals(List<T> actual, List<T> expected)
    {
        int count = Math.min(actual.size(), expected.size());

        for (int i = 0; i < count; i++)
        {
            assertEquals(actual.get(i), expected.get(i), String.format("Element #%d.", i));
        }

        assertEquals(actual.size(), expected.size(), "List size.");
    }

    /**
     * Convenience for {@link #assertListsEquals(List, List)}.
     *
     * @param <T>      type of objects to compare
     * @param actual   actual values to check
     * @param expected expected values
     */
    protected static <T> void assertListsEquals(List<T> actual, T... expected)
    {
        assertListsEquals(actual, Arrays.asList(expected));
    }

    /**
     * Convenience for {@link #assertListsEquals(List, List)}.
     *
     * @param <T>      type of objects to compare
     * @param actual   actual values to check
     * @param expected expected values
     */
    protected static <T> void assertArraysEqual(T[] actual, T... expected)
    {
        assertListsEquals(Arrays.asList(actual), expected);
    }

    /**
     * A factory method to create EasyMock Capture objects.
     */
    protected static <T> Capture<T> newCapture()
    {
        return new Capture<T>();
    }

    /**
     * Creates a new instance of the object using its default constructor, and initializes it (via {@link #set(Object,
     * Object[])}).
     *
     * @param objectType  typeof object to instantiate
     * @param fieldValues string field names and corresponding field values
     * @return the initialized instance
     */
    protected static <T> T create(Class<T> objectType, Object... fieldValues)
    {
        T result = null;

        try
        {
            result = objectType.newInstance();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(String.format("Unable to instantiate instance of %s: %s",
                                                     objectType.getName(), InternalUtils.toMessage(ex)), ex);
        }

        return set(result, fieldValues);
    }

    /**
     * Initializes private fields (via reflection).
     *
     * @param object      object to be updated
     * @param fieldValues string field names and corresponding field values
     * @return the object
     */
    protected static <T> T set(T object, Object... fieldValues)
    {
        Defense.notNull(object, "object");

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
                throw new RuntimeException(String.format("Unable to set field '%s' of %s to %s: %s",
                                                         fieldName, object, fieldValue,
                                                         InternalUtils.toMessage(ex)), ex);
            }
        }

        return object;
    }

    /**
     * Reads the content of a private field.
     *
     * @param object    to read the private field from
     * @param fieldName name of field to read
     * @return value stored in the field
     * @since 5.1.0.5
     */
    protected static Object get(Object object, String fieldName)
    {
        Defense.notNull(object, "object");
        Defense.notBlank(fieldName, "fieldName");

        try
        {
            Field field = findField(object.getClass(), fieldName);

            field.setAccessible(true);

            return field.get(object);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(String.format("Unable to read field '%s' of %s: %s",
                                                     fieldName, object,
                                                     InternalUtils.toMessage(ex)), ex);
        }
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

        throw new RuntimeException(
                String.format("Class %s does not contain a field named '%s'.", objectClass.getName(), fieldName));
    }
}
