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

package org.apache.tapestry5.internal.util;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.TransformMethodSignature;
import org.apache.tapestry5.test.TapestryTestCase;
import org.testng.annotations.Test;

import java.lang.reflect.Modifier;

public class MethodInvocationBuilderTest extends TapestryTestCase
{
    private static final String LOCALE_CLASS_NAME = "java.util.Locale";

    private static final String MARKUP_WRITER_CLASS_NAME = MarkupWriter.class.getName();

    @Test
    public void known_parameter_type()
    {
        ClassTransformation transformation = mockClassTransformation();

        replay();

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "void", "myMethod",
                                                                    new String[] { MARKUP_WRITER_CLASS_NAME }, null);

        MethodInvocationBuilder invoker = new MethodInvocationBuilder();

        invoker.addParameter(MARKUP_WRITER_CLASS_NAME, "$1");

        assertEquals(invoker.buildMethodInvocation(sig, transformation), "myMethod($1)");

        verify();
    }

    @Test
    public void unknown_parameter_type()
    {
        ClassTransformation transformation = mockClassTransformation();

        replay();

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "void", "myMethod",
                                                                    new String[] { MARKUP_WRITER_CLASS_NAME }, null);

        MethodInvocationBuilder invoker = new MethodInvocationBuilder();

        assertEquals(invoker.buildMethodInvocation(sig, transformation), "myMethod(null)");

        verify();
    }

    @Test
    public void multiple_parameters_for_method()
    {
        ClassTransformation transformation = mockClassTransformation();

        replay();

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "void", "myMethod", new String[] {
                MARKUP_WRITER_CLASS_NAME, LOCALE_CLASS_NAME }, null);

        MethodInvocationBuilder invoker = new MethodInvocationBuilder();

        invoker.addParameter(MARKUP_WRITER_CLASS_NAME, "$1");
        invoker.addParameter(LOCALE_CLASS_NAME, "$2");

        assertEquals(invoker.buildMethodInvocation(sig, transformation), "myMethod($1, $2)");

        verify();
    }
}
