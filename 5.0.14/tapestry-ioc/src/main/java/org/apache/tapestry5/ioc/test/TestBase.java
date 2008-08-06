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

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IExpectationSetters;
import org.easymock.IMocksControl;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;

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
 * execute tests in parallel. Unfortunately, as of this writing (TestNG 5.1 and maven-surefire 2.8-SNAPSHOT) parallel
 * execution does not always work fully and consistently, some tests are dropped, and so Tapestry does not make use of
 * TestNG parallel execution.
 *
 * @see EasyMock#createControl()
 * @see MockTester
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
    protected final void setThrowable(Throwable throwable)
    {
        EasyMock.expectLastCall().andThrow(throwable);
    }

    /**
     * Convienience for {@link EasyMock#expectLastCall()} with {@link IExpectationSetters#andAnswer(org.easymock.IAnswer)}.
     *
     * @param answer callback for the most recent method invocation
     */
    protected final void setAnswer(IAnswer answer)
    {
        EasyMock.expectLastCall().andAnswer(answer);
    }

    /**
     * Invoked from code that should not be reachable. For example, place a call to unreachable() after invoking a
     * method that is expected to throw an exception.
     */

    protected final void unreachable()
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
    protected final <T> IExpectationSetters<T> expect(T value)
    {
        return EasyMock.expect(value);
    }

    /**
     * Asserts that the message property of the throwable contains each of the provided substrings.
     *
     * @param t          throwable to check
     * @param substrings some number of expected substrings
     */
    protected final void assertMessageContains(Throwable t, String... substrings)
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
    protected final <T> void assertListsEquals(List<T> actual, List<T> expected)
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
     * @param <T>      tyoe of objects to compare
     * @param actual   actual values to check
     * @param expected expected values
     */
    protected final <T> void assertListsEquals(List<T> actual, T... expected)
    {
        assertListsEquals(actual, Arrays.asList(expected));
    }

    /**
     * Convenience for {@link #assertListsEquals(List, List)}.
     *
     * @param <T>      tyoe of objects to compare
     * @param actual   actual values to check
     * @param expected expected values
     */
    protected final <T> void assertArraysEqual(T[] actual, T... expected)
    {
        assertListsEquals(Arrays.asList(actual), expected);
    }
}
