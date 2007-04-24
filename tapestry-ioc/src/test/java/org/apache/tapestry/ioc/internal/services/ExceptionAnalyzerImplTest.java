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

import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.ioc.services.ExceptionAnalysis;
import org.apache.tapestry.ioc.services.ExceptionAnalyzer;
import org.apache.tapestry.ioc.services.ExceptionInfo;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ExceptionAnalyzerImplTest extends IOCInternalTestCase
{
    private ExceptionAnalyzer _analyzer;

    @BeforeClass
    public void setup_analyzer()
    {
        _analyzer = getService("tapestry.ioc.ExceptionAnalyzer", ExceptionAnalyzer.class);
    }

    @AfterClass
    public void cleanup_analyzer()
    {
        _analyzer = null;
    }

    @Test
    public void basic_exception()
    {
        String message = "Hey! We've Got Not Tomatoes!";

        Throwable t = new RuntimeException(message);

        ExceptionAnalysis ea = _analyzer.analyze(t);

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
        Location l = newLocation();

        replay();

        Throwable t = new TapestryException("Message", l, null);

        ExceptionAnalysis ea = _analyzer.analyze(t);

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

        ExceptionAnalysis ea = _analyzer.analyze(outer);

        assertEquals(ea.getExceptionInfos().size(), 2);

        ExceptionInfo ei = ea.getExceptionInfos().get(0);

        assertEquals(ei.getMessage(), "Outer");
        assertTrue(ei.getStackTrace().isEmpty());

        ei = ea.getExceptionInfos().get(1);

        assertEquals(ei.getMessage(), "Inner");
        assertFalse(ei.getStackTrace().isEmpty());
    }

}
