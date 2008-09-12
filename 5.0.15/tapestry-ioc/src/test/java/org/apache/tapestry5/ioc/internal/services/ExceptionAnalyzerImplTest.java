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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.services.ExceptionAnalysis;
import org.apache.tapestry5.ioc.services.ExceptionAnalyzer;
import org.apache.tapestry5.ioc.services.ExceptionInfo;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

public class ExceptionAnalyzerImplTest extends IOCInternalTestCase
{
    private ExceptionAnalyzer analyzer;

    @BeforeClass
    public void setup_analyzer()
    {
        analyzer = getService("ExceptionAnalyzer", ExceptionAnalyzer.class);
    }

    @AfterClass
    public void cleanup_analyzer()
    {
        analyzer = null;
    }

    @Test
    public void basic_exception()
    {
        String message = "Hey! We've Got Not Tomatoes!";

        Throwable t = new RuntimeException(message);

        ExceptionAnalysis ea = analyzer.analyze(t);

        assertEquals(ea.getExceptionInfos().size(), 1);

        ExceptionInfo ei = ea.getExceptionInfos().get(0);

        assertEquals(ei.getClassName(), RuntimeException.class.getName());
        assertEquals(ei.getMessage(), message);

        assertTrue(ei.getPropertyNames().isEmpty());
        assertFalse(ei.getStackTrace().isEmpty());
    }

    @Test
    public void exception_properties()
    {
        Location l = mockLocation();

        replay();

        Throwable t = new TapestryException("Message", l, null);

        ExceptionAnalysis ea = analyzer.analyze(t);

        assertEquals(ea.getExceptionInfos().size(), 1);

        ExceptionInfo ei = ea.getExceptionInfos().get(0);

        assertEquals(ei.getPropertyNames(), Arrays.asList("location"));

        assertEquals(ei.getProperty("location"), l);

        verify();
    }

    @Test
    public void nested_exceptions()
    {
        Throwable inner = new RuntimeException("Inner");
        Throwable outer = new RuntimeException("Outer", inner);

        ExceptionAnalysis ea = analyzer.analyze(outer);

        assertEquals(ea.getExceptionInfos().size(), 2);

        ExceptionInfo ei = ea.getExceptionInfos().get(0);

        assertEquals(ei.getMessage(), "Outer");
        assertTrue(ei.getStackTrace().isEmpty());

        ei = ea.getExceptionInfos().get(1);

        assertEquals(ei.getMessage(), "Inner");
        assertFalse(ei.getStackTrace().isEmpty());
    }

    @Test
    public void middle_exception_removed_with_no_value()
    {
        Throwable inner = new RuntimeException("Inner");
        Throwable middle = new RuntimeException("Middle", inner);
        Throwable outer = new RuntimeException("Outer: Middle", middle);

        ExceptionAnalysis ea = analyzer.analyze(outer);

        assertEquals(ea.getExceptionInfos().size(), 2);

        ExceptionInfo ei = ea.getExceptionInfos().get(0);

        assertEquals(ei.getMessage(), "Outer: Middle");
        assertTrue(ei.getStackTrace().isEmpty());

        ei = ea.getExceptionInfos().get(1);

        assertEquals(ei.getMessage(), "Inner");
        assertFalse(ei.getStackTrace().isEmpty());
    }

    @Test
    public void middle_exception_retained_due_to_extra_property()
    {
        Location l = mockLocation();

        replay();

        Throwable inner = new RuntimeException("Inner");
        Throwable middle = new TapestryException("Middle", l, inner);
        Throwable outer = new RuntimeException("Outer: Middle", middle);

        ExceptionAnalysis ea = analyzer.analyze(outer);

        assertEquals(ea.getExceptionInfos().size(), 3);

        ExceptionInfo ei = ea.getExceptionInfos().get(0);

        assertEquals(ei.getMessage(), "Outer: Middle");
        assertTrue(ei.getStackTrace().isEmpty());

        ei = ea.getExceptionInfos().get(1);

        assertEquals(ei.getMessage(), "Middle");
        assertTrue(ei.getStackTrace().isEmpty());

        ei = ea.getExceptionInfos().get(2);

        assertEquals(ei.getMessage(), "Inner");
        assertFalse(ei.getStackTrace().isEmpty());

        verify();
    }

    /**
     * TAPESTRY-2422
     */
    @Test
    public void exception_with_write_only_property()
    {
        WriteOnlyPropertyException ex = new WriteOnlyPropertyException();

        ex.setFaultCode(99);

        ExceptionAnalysis ea = analyzer.analyze(ex);

        ExceptionInfo ei = ea.getExceptionInfos().get(0);

        assertEquals(ei.getPropertyNames().size(), 1);

        assertEquals(ei.getProperty("code"), "0099");
    }

}
