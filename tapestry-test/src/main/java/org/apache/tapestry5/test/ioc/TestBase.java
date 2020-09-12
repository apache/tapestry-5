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

import org.easymock.*;
import org.testng.annotations.AfterMethod;

/**
 * Manages a set of EasyMock mock objects. Used as a base class for test cases.
 *
 * Extends from {@link org.testng.Assert} to bring in all the public static assert methods without requiring extra
 * imports.
 *
 * Provides a common mock factory method, {@link #newMock(Class)}. A single <em>standard</em> mock control is used for
 * all mock objects. Standard mocks do not care about the exact order in which methods are invoked, though they are as
 * rigorous as strict mocks when checking that parameters are the correct values.
 *
 * This base class is created with the intention of use within a TestNG test suite; if using JUnit, you can get the same
 * functionality using {@link MockTester}.
 *
 * This class is thread safe (it uses a thread local to store the mock control). In theory, this should allow TestNG to
 * execute tests in parallel.
 *
 * This class was originally in the tapestry-ioc module as was moved to tapestry-test; the package name was not changed
 * to ensure backwards compatibility.
 * 
 * @see org.easymock.EasyMock#createControl()
 * @see org.apache.tapestry5.test.ioc.MockTester
 * @deprecated In 5.4, with no replacement
 */
public class TestBase extends TestUtils
{
    private final MockTester tester = new MockTester();

    /**
     * @return the {@link IMocksControl} for this thread.
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
     * @param <T>
     *            the type of the mock object
     * @param mockClass
     *            the class to mock
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
     * @param throwable
     *            the exception to be thrown by the most recent method call on any mock
     */
    protected static void setThrowable(Throwable throwable)
    {
        EasyMock.expectLastCall().andThrow(throwable);
    }

    /**
     * Convienience for {@link EasyMock#expectLastCall()} with
     * {@link IExpectationSetters#andAnswer(org.easymock.IAnswer)}.
     * 
     * @param answer
     *            callback for the most recent method invocation
     */
    protected static void setAnswer(IAnswer<?> answer)
    {
        EasyMock.expectLastCall().andAnswer(answer);
    }

    /**
     * Convenience for {@link EasyMock#expect(Object)}.
     * 
     * @param value to expect
     * @return expectation setter, for setting return value, etc.
     */
    @SuppressWarnings("unchecked")
    protected static <T> IExpectationSetters<T> expect(T value)
    {
        return EasyMock.expect(value);
    }

    /**
     * A factory method to create EasyMock Capture objects.
     * @return new Capture
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected static <T> Capture<T> newCapture()
    {
        return new Capture<T>();
    }
}
