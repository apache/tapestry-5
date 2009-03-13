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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.sql.SQLException;

public class MethodSignatureTest extends IOCTestCase
{
    private MethodSignature find(Class sourceClass, String methodName)
    {
        Method[] methods = sourceClass.getMethods();

        for (int i = 0; i < methods.length; i++)
        {
            Method m = methods[i];

            if (m.getName().equals(methodName)) return new MethodSignature(m);
        }

        unreachable();
        return null;
    }

    @Test
    public void excercize_equals_and_hashcode()
    {
        MethodSignature m1 = find(Object.class, "hashCode");
        MethodSignature m2 = find(Boolean.class, "hashCode");

        assertEquals(m1.hashCode(), m2.hashCode());
        assertTrue(m1.equals(m2));

        m1 = find(String.class, "charAt");
        m2 = find(StringBuilder.class, "charAt");

        assertEquals(m1.hashCode(), m2.hashCode());
        assertTrue(m1.equals(m2));

        m1 = find(ObjectInput.class, "close");
        m2 = find(ObjectInputStream.class, "close");

        assertEquals(m1.hashCode(), m2.hashCode());
        assertTrue(m1.equals(m2));
    }

    @Test
    public void equals_and_hashcode_with_null_parameters_and_exception_lists()
    {
        MethodSignature m1 = new MethodSignature(void.class, "foo", null, null);
        MethodSignature m2 = new MethodSignature(void.class, "foo", new Class[0], new Class[0]);

        assertEquals(m1, m2);
        assertEquals(m2, m1);

        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    public void equals_with_name_mismatch()
    {
        MethodSignature m1 = new MethodSignature(void.class, "foo", null, null);
        MethodSignature m2 = new MethodSignature(void.class, "bar", null, null);

        assertEquals(false, m1.equals(m2));
    }

    @Test
    public void equals_with_parameters_mismatch()
    {
        MethodSignature m1 = new MethodSignature(void.class, "foo", new Class[] { String.class }, null);
        MethodSignature m2 = new MethodSignature(void.class, "foo", new Class[] { Boolean.class }, null);

        assertEquals(false, m1.equals(m2));
    }

    @Test
    public void equals_with_null()
    {
        MethodSignature m1 = new MethodSignature(void.class, "foo", null, null);

        assertEquals(m1.equals(null), false);
    }

    @Test
    public void equals_with_not_method_signature()
    {
        MethodSignature m1 = new MethodSignature(void.class, "foo", null, null);

        assertEquals(m1.equals("Method Signature"), false);
    }

    @Test
    public void to_string()
    {
        MethodSignature m = find(String.class, "getChars");

        assertEquals(m.toString(), "void getChars(int, int, char[], int)");

        m = find(Class.class, "newInstance");

        assertEquals(m.toString(),
                     "java.lang.Object newInstance() throws java.lang.IllegalAccessException, java.lang.InstantiationException");
    }

    @Test
    public void unique_id()
    {
        MethodSignature m = find(String.class, "getChars");

        assertEquals(m.getUniqueId(), "getChars(int,int,char[],int)");

        m = find(Class.class, "newInstance");

        assertEquals(m.getUniqueId(), "newInstance()");
    }

    @Test
    public void overriding_signature_type_mismatch()
    {
        MethodSignature m1 = new MethodSignature(void.class, "foo", null, null);
        MethodSignature m2 = new MethodSignature(int.class, "foo", null, null);

        assertEquals(m1.isOverridingSignatureOf(m2), false);
    }

    @Test
    public void overriding_signature_name_mismatch()
    {
        MethodSignature m1 = new MethodSignature(void.class, "foo", null, null);
        MethodSignature m2 = new MethodSignature(void.class, "bar", null, null);

        assertEquals(m1.isOverridingSignatureOf(m2), false);
    }

    @Test
    public void overriding_signature_parameters_mismatch()
    {
        MethodSignature m1 = new MethodSignature(void.class, "foo", null, null);
        MethodSignature m2 = new MethodSignature(void.class, "foo", new Class[] { String.class }, null);

        assertEquals(m1.isOverridingSignatureOf(m2), false);
    }

    @Test
    public void overriding_signature()
    {
        MethodSignature m1 = new MethodSignature(void.class, "close", null, new Class[] { Exception.class });
        MethodSignature m2 = new MethodSignature(void.class, "close", null, new Class[] { RuntimeException.class });

        assertEquals(m1.isOverridingSignatureOf(m2), true);
        assertEquals(m2.isOverridingSignatureOf(m1), false);
    }

    /**
     * Tests a shorcut used when one signature has zero exceptions.
     */
    @Test
    public void overriding_signature_with_no_exceptions()
    {
        MethodSignature m1 = new MethodSignature(void.class, "close", null, null);
        MethodSignature m2 = new MethodSignature(void.class, "close", null, new Class[] { RuntimeException.class });

        assertEquals(m1.isOverridingSignatureOf(m2), false);
        assertEquals(m2.isOverridingSignatureOf(m1), true);
    }

    /**
     * Fill in code coverage for multiple matched signatures.
     */
    @Test
    public void overriding_signature_with_multiple_matched_exceptions()
    {
        MethodSignature m1 = new MethodSignature(void.class, "close", null,
                                                 new Class[] { SQLException.class, NumberFormatException.class });
        MethodSignature m2 = new MethodSignature(void.class, "close", null,
                                                 new Class[] { SQLException.class, IOException.class });

        assertEquals(m1.isOverridingSignatureOf(m2), false);
        assertEquals(m2.isOverridingSignatureOf(m1), false);
    }
}
