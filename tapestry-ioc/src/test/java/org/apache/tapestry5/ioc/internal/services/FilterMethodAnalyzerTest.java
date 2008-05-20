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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class FilterMethodAnalyzerTest extends IOCInternalTestCase
{
    private MethodSignature find(Class target, String name)
    {
        Method method = findMethod(target, name);

        return new MethodSignature(method);
    }

    private void assertPosition(String methodName, int expected)
    {
        FilterMethodAnalyzer a = new FilterMethodAnalyzer(SampleService.class);

        MethodSignature ms = find(SampleService.class, methodName);
        MethodSignature fms = find(SampleFilter.class, methodName);

        assertEquals(expected, a.findServiceInterfacePosition(ms, fms));
    }

    private void assertMismatch(String methodName)
    {
        assertPosition(methodName, -1);
    }

    @Test
    public void simple_match()
    {
        assertPosition("simpleMatch", 0);
    }

    @Test
    public void mismatched_parameter_count()
    {
        assertMismatch("mismatchParameterCount");
    }

    @Test
    public void mismatch_on_method_return_type()
    {
        assertMismatch("mismatchReturnType");
    }

    @Test
    public void service_interface_not_in_filter_method_signature()
    {
        assertMismatch("missingServiceInterface");
    }

    @Test
    public void match_with_multiple_parameters()
    {
        assertPosition("complexMatch", 2);
    }
}
