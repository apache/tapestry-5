// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.test;

import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;
import org.easymock.IMocksControl;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;

/**
 * Manages a set of EasyMock mock objects. Used as a base class for test cases.
 * <p>
 * Extends from {@link org.testng.Assert} to bring in all the public static assert methods without
 * requiring extra imports.
 * <p>
 * Provides common mock factory and mock trainer methods. A single <em>standard</em> mock control
 * is used for all mock objects. Standard mocks do not care about the exact order in which methods
 * are invoked, though they are as rigourous as strict mocks when checking that parameters are the
 * correct values.
 * <p>
 * This class is thread safe (it uses a thread local to store the mock control). In theory, this
 * should allow TestNG to execute tests in parallel. Unfortunately, as of this writing (TestNG 5.1
 * and maven-surefire 2.8-SNAPSHOT) parallel execution does not always work fully and consistently,
 * some tests are dropped, and so Tapestry does not make use of TestNG parallel execution.
 * 
 * @see EasyMock#createControl()
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

    private final ThreadLocalControl _localControl = new ThreadLocalControl();

    /**
     * Returns the {@link IMocksControl} for this thread.
     */
    protected final IMocksControl getMocksControl()
    {
        return _localControl.get();
    }

    /**
     * Discards any mock objects created during the test.
     */
    @AfterMethod(alwaysRun = true)
    public final void discardMockControl()
    {
        _localControl.remove();
    }

    /**
     * Creates a new mock object of the indicated type. The shared mock control does <strong>not</strong>
     * check order, but does fail on any unexpected method invocations.
     * 
     * @param <T>
     *            the type of the mock object
     * @param mockClass
     *            the class to mock
     * @return the mock object, ready for training
     */
    protected final <T> T newMock(Class<T> mockClass)
    {
        return getMocksControl().createMock(mockClass);
    }

    /**
     * Replay's each mock object created by {@link #newMock(Class)}.
     */
    protected final void replay()
    {
        getMocksControl().replay();
    }

    /**
     * Verifies each created mock object, then resets the mock for additional training.
     */
    protected final void verify()
    {
        IMocksControl control = getMocksControl();

        control.verify();
        control.reset();
    }

    /**
     * Trains a mock object to throw an exception (for the most recent method call). Generally,
     * using {@link #expect(Object)}.andThrow() is preferred, but that doesn't work for void
     * methods.
     * 
     * @param throwable
     *            the exception to be thrown by the most recent method call on the mock
     */
    protected final void setThrowable(Throwable throwable)
    {
        getMocksControl().andThrow(throwable);
    }

    /**
     * Invoked from code that should not be reachable. For example, place a call to unreachable()
     * after invoking a method that is expected to throw an exception.
     */

    protected final void unreachable()
    {
        fail("This code should not be reachable.");
    }

    @SuppressWarnings("unchecked")
    protected final <T> IExpectationSetters<T> expect(T value)
    {
        // value will have been evaluated, we can then return the control to string together
        // andReturn() or etc. calls

        return getMocksControl();
    }

    /**
     * Asserts that the message property of the throwable contains each of the provided substrings.
     * 
     * @param t
     *            throwable to check
     * @param substrings
     *            some number of expected substrings
     */
    protected final void assertMessageContains(Throwable t, String... substrings)
    {
        String message = t.getMessage();

        for (String substring : substrings)
            assertTrue(message.contains(substring));
    }
}
