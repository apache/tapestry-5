// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ClassFabUtilsTest extends IOCTestCase
{

    @Test(dataProvider = "createInputs")
    public void to_jvm_binary_name(String input, String expected)
    {
        String actual = ClassFabUtils.toJVMBinaryName(input);

        assertEquals(actual, expected);
    }

    @DataProvider
    public Object[][] createInputs()
    {
        return new Object[][] { { "java.lang.Object", "java.lang.Object" },

                { "int", "int" },

                { "int[]", "[I" },

                { "java.lang.Throwable[]", "[Ljava.lang.Throwable;" },

                { "byte[][]", "[[B" },

                { "java.lang.Runnable[][]", "[[Ljava.lang.Runnable;" } };
    }

    @Test(dataProvider = "typeCodeProvider")
    public void get_type_code(Class input, String expected)
    {
        assertEquals(ClassFabUtils.getTypeCode(input), expected);
    }

    @DataProvider
    public Object[][] typeCodeProvider()
    {
        return new Object[][] { { int.class, "I" },

                { int[].class, "[I" },

                { Thread.class, "Ljava/lang/Thread;" },

                { Thread[].class, "[Ljava/lang/Thread;" },

                { Double[][].class, "[[Ljava/lang/Double;" },

                { void.class, "V" } };
    }

    @Test
    public void primitive_type_from_wrapper_type()
    {
        assertSame(ClassFabUtils.getPrimitiveType(Boolean.class), boolean.class);
    }

    @Test
    public void get_primitive_type_from_name()
    {
        assertSame(ClassFabUtils.getPrimitiveType("int"), int.class);
    }

    @Test
    public void cast_reference_to_object_type()
    {
        assertEquals(ClassFabUtils.castReference("ref", "java.lang.String"),
                     "(java.lang.String)ref");
    }

    @Test
    public void cast_reference_to_primitive_type()
    {
        assertEquals(ClassFabUtils.castReference("ref", "short"),
                     "((java.lang.Short)ref).shortValue()");

    }
}
