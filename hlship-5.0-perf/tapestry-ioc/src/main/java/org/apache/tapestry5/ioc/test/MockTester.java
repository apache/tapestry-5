// Copyright 2007 The Apache Software Foundation
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
import org.easymock.IMocksControl;

/**
 * Contains core logic used by {@link TestBase}, allowing for mock objects to be used outside of a TestNG-based test
 * suite. A <em>single</em> standard mock control is used for all mock instances. The control does not care about
 * execution order, but will balk at any unexpected method invocations. This class is thread safe (it used a thread
 * local to store the mock control).
 */
public final class MockTester
{
    private static class ThreadLocalControl extends ThreadLocal<IMocksControl>
    {
        @Override
        protected IMocksControl initialValue()
        {
            return EasyMock.createControl();
        }
    }

    private final ThreadLocalControl localControl = new ThreadLocalControl();

    /**
     * Invoked after an individual unit test (i.e., a test method invocation) to discard the mock control.
     */
    public synchronized void cleanup()
    {
        localControl.remove();
    }

    public synchronized IMocksControl getMocksControl()
    {
        return localControl.get();
    }

    /**
     * Creates a new mock object of the indicated type. The shared mock control does <strong>not</strong> check order,
     * but does fail on any unexpected method invocations.
     *
     * @param <T>       the type of the mock object
     * @param mockClass the class to mock
     * @return the mock object, ready for training
     */
    public <T> T newMock(Class<T> mockClass)
    {
        return getMocksControl().createMock(mockClass.getSimpleName(), mockClass);
    }

    /**
     * Switches each mock object created by {@link #newMock(Class)} into replay mode (out of the initial training
     * mode).
     */
    public void replay()
    {
        getMocksControl().replay();
    }

    /**
     * Verifies that all trained methods have been invoked on all mock objects (created by {@link #newMock(Class)}, then
     * switches each mock object back to training mode.
     */
    public void verify()
    {
        IMocksControl control = getMocksControl();

        control.verify();
        control.reset();
    }
}
