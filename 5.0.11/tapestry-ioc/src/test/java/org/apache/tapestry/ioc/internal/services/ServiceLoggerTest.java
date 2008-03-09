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

package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.services.ExceptionTracker;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.Arrays;

public class ServiceLoggerTest extends IOCTestCase
{
    private void try_entry(String methodName, String expected, Object... arguments)
    {
        Logger logger = mockLogger();
        ExceptionTracker tracker = mockExceptionTracker();

        logger.debug("[ENTER] " + expected);

        replay();

        new ServiceLogger(logger, tracker).entry(methodName, arguments);

        verify();

    }

    protected final ExceptionTracker mockExceptionTracker()
    {
        return newMock(ExceptionTracker.class);
    }

    private void try_exit(String methodName, String expected, Object result)
    {
        Logger logger = mockLogger();
        ExceptionTracker tracker = mockExceptionTracker();

        logger.debug("[ EXIT] " + expected);

        replay();

        new ServiceLogger(logger, tracker).exit(methodName, result);

        verify();
    }

    @Test
    public void entry_tests()
    {
        try_entry("fred", "fred()");
        try_entry("barney", "barney(\"rubble\")", "rubble");
        try_entry("yogi", "yogi(null, null)", null, null);
        try_entry("wilma", "wilma(1, 2, 3)", 1, 2, 3);
        try_entry("betty", "betty(\"rubble\", {1, 2, 3, \"four\"})", "rubble", new Object[]
                {1, 2, 3, "four"});
        try_entry("betty", "betty(\"rubble\", [1, 2, 3, \"four\", [5, 6]])", "rubble", Arrays
                .asList(1, 2, 3, "four", Arrays.asList(5, 6)));
    }

    @Test
    public void exit_test()
    {
        try_exit("fred", "fred [true]", true);
        try_exit("barney", "barney [\"rubble\"]", "rubble");
    }

    @Test
    public void void_exit_test()
    {
        Logger logger = mockLogger();
        ExceptionTracker tracker = mockExceptionTracker();

        logger.debug("[ EXIT] wilma");

        replay();

        new ServiceLogger(logger, tracker).voidExit("wilma");

        verify();
    }

    @Test
    public void fail_test_exception_not_already_logged()
    {
        Logger logger = mockLogger();
        ExceptionTracker tracker = mockExceptionTracker();

        RuntimeException t = new RuntimeException("Ouch!");

        train_isDebugEnabled(logger, true);

        train_exceptionLogged(tracker, t, false);

        logger.debug("[ FAIL] wilma -- " + t.getClass().getName(), t);

        replay();

        new ServiceLogger(logger, tracker).fail("wilma", t);

        verify();
    }

    @Test
    public void fail_test_exception_previously_logged()
    {
        Logger logger = mockLogger();
        ExceptionTracker tracker = mockExceptionTracker();

        RuntimeException t = new RuntimeException("Ouch!");

        train_isDebugEnabled(logger, true);

        train_exceptionLogged(tracker, t, true);

        logger.debug("[ FAIL] wilma -- " + t.getClass().getName(), (Throwable) null);

        replay();

        new ServiceLogger(logger, tracker).fail("wilma", t);

        verify();
    }

    @Test
    public void fail_debug_not_enabled()
    {
        Logger logger = mockLogger();
        ExceptionTracker tracker = mockExceptionTracker();

        RuntimeException t = new RuntimeException("Ouch!");

        train_isDebugEnabled(logger, false);

        replay();

        new ServiceLogger(logger, tracker).fail("wilma", t);

        verify();
    }

    private void train_exceptionLogged(ExceptionTracker tracker, Throwable exception, boolean logged)
    {
        expect(tracker.exceptionLogged(exception)).andReturn(logged);
    }

    @Test
    public void debug_enabled()
    {
        Logger logger = mockLogger();
        ExceptionTracker tracker = mockExceptionTracker();

        train_isDebugEnabled(logger, true);
        train_isDebugEnabled(logger, false);

        replay();

        ServiceLogger serviceLogger = new ServiceLogger(logger, tracker);

        assertTrue(serviceLogger.isDebugEnabled());
        assertFalse(serviceLogger.isDebugEnabled());

        verify();
    }
}