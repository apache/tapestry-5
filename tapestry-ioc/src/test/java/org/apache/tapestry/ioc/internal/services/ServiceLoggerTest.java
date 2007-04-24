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

package org.apache.tapestry.ioc.internal.services;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.services.ExceptionTracker;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

public class ServiceLoggerTest extends IOCTestCase
{
    private void try_entry(String methodName, String expected, Object... arguments)
    {
        Log log = newLog();
        ExceptionTracker tracker = newExceptionTracker();

        log.debug("[ENTER] " + expected);

        replay();

        new ServiceLogger(log, tracker).entry(methodName, arguments);

        verify();

    }

    protected final ExceptionTracker newExceptionTracker()
    {
        return newMock(ExceptionTracker.class);
    }

    private void try_exit(String methodName, String expected, Object result)
    {
        Log log = newLog();
        ExceptionTracker tracker = newExceptionTracker();

        log.debug("[ EXIT] " + expected);

        replay();

        new ServiceLogger(log, tracker).exit(methodName, result);

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
        { 1, 2, 3, "four" });
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
        Log log = newLog();
        ExceptionTracker tracker = newExceptionTracker();

        log.debug("[ EXIT] wilma");

        replay();

        new ServiceLogger(log, tracker).voidExit("wilma");

        verify();
    }

    @Test
    public void fail_test_exception_not_already_logged()
    {
        Log log = newLog();
        ExceptionTracker tracker = newExceptionTracker();

        RuntimeException t = new RuntimeException("Ouch!");

        train_isDebugEnabled(log, true);

        train_exceptionLogged(tracker, t, false);

        log.debug("[ FAIL] wilma -- " + t.getClass().getName(), t);

        replay();

        new ServiceLogger(log, tracker).fail("wilma", t);

        verify();
    }

    @Test
    public void fail_test_exception_previously_logged()
    {
        Log log = newLog();
        ExceptionTracker tracker = newExceptionTracker();

        RuntimeException t = new RuntimeException("Ouch!");

        train_isDebugEnabled(log, true);

        train_exceptionLogged(tracker, t, true);

        log.debug("[ FAIL] wilma -- " + t.getClass().getName(), null);

        replay();

        new ServiceLogger(log, tracker).fail("wilma", t);

        verify();
    }

    @Test
    public void fail_debug_not_enabled()
    {
        Log log = newLog();
        ExceptionTracker tracker = newExceptionTracker();

        RuntimeException t = new RuntimeException("Ouch!");

        train_isDebugEnabled(log, false);

        replay();

        new ServiceLogger(log, tracker).fail("wilma", t);

        verify();
    }

    private void train_exceptionLogged(ExceptionTracker tracker, Throwable exception, boolean logged)
    {
        expect(tracker.exceptionLogged(exception)).andReturn(logged);
    }

    @Test
    public void debug_enabled()
    {
        Log log = newLog();
        ExceptionTracker tracker = newExceptionTracker();

        train_isDebugEnabled(log, true);
        train_isDebugEnabled(log, false);

        replay();

        ServiceLogger logger = new ServiceLogger(log, tracker);

        assertTrue(logger.isDebugEnabled());
        assertFalse(logger.isDebugEnabled());

        verify();
    }
}