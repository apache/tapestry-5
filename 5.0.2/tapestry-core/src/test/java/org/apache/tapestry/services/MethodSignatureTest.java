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

package org.apache.tapestry.services;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.testng.annotations.Test;

/**
 * 
 */
public class MethodSignatureTest
{

    @Test
    public void signature_toString()
    {
        MethodSignature sig = new MethodSignature(Modifier.PUBLIC, "int", "doSomething",
                new String[]
                { "java.lang.String", "int" }, new String[]
                { "java.lang.RuntimeException", "org.foo.FredException" });

        assertEquals(
                sig.toString(),
                "public int doSomething(java.lang.String, int) throws java.lang.RuntimeException, org.foo.FredException");

        sig = new MethodSignature(Modifier.ABSTRACT + Modifier.PROTECTED, "boolean", "misoHapi",
                new String[0], new String[0]);

        assertEquals(sig.toString(), "protected abstract boolean misoHapi()");
    }

    @Test
    public void medium_description()
    {
        MethodSignature sig = new MethodSignature(Modifier.PUBLIC, "int", "doSomething",
                new String[]
                { "java.lang.String", "int" }, new String[]
                { "java.lang.RuntimeException", "org.foo.FredException" });

        assertEquals(sig.getMediumDescription(), "doSomething(java.lang.String, int)");
    }

    @Test
    public void package_private_toString()
    {
        MethodSignature sig = new MethodSignature(0, "int", "packagePrivate", null, null);

        assertEquals(sig.toString(), "int packagePrivate()");
    }

    @Test
    public void null_value_for_parameters_and_exceptions()
    {
        MethodSignature sig = new MethodSignature(Modifier.PUBLIC, "int", "doSomething", null, null);

        assertEquals(sig.toString(), "public int doSomething()");

        assertEquals(sig.getParameterTypes(), new String[0]);
        assertEquals(sig.getExceptionTypes(), new String[0]);
    }

    @Test
    public void getters()
    {
        MethodSignature sig = new MethodSignature(Modifier.PUBLIC, "int", "doSomething",
                new String[]
                { "java.lang.String", "int" }, new String[]
                { "java.lang.RuntimeException", "org.foo.FredException" });

        assertEquals(sig.getModifiers(), Modifier.PUBLIC);
        assertEquals(sig.getReturnType(), "int");
        assertEquals(sig.getMethodName(), "doSomething");
        assertEquals(sig.getParameterTypes(), new String[]
        { "java.lang.String", "int" });
        assertEquals(sig.getExceptionTypes(), new String[]
        { "java.lang.RuntimeException", "org.foo.FredException" });
    }

    @Test
    public void sorting()
    {
        MethodSignature s1 = new MethodSignature(Modifier.PUBLIC, "void", "foo", null, null);
        MethodSignature s2 = new MethodSignature(Modifier.PUBLIC, "void", "bar", null, null);
        MethodSignature s3 = new MethodSignature(Modifier.PUBLIC, "void", "baz", null, null);
        MethodSignature s4 = new MethodSignature(Modifier.PUBLIC, "void", "baz", new String[]
        { "int" }, null);

        List<MethodSignature> list = CollectionFactory.newList(Arrays.asList(s1, s2, s3, s4));

        Collections.sort(list);

        assertEquals(list, Arrays.asList(s2, s3, s4, s1));
    }

    @Test
    public void hash_code_and_equals()
    {
        MethodSignature sig1 = new MethodSignature(Modifier.PUBLIC, "int", "doSomething",
                new String[]
                { "int" }, new String[]
                { "org.foo.BarException" });
        int hashCode1 = sig1.hashCode();

        // Check that same value returned each time.

        assertEquals(sig1.hashCode(), hashCode1);

        MethodSignature sig2 = new MethodSignature(Modifier.PUBLIC, "int", "doSomething",
                new String[]
                { "int" }, new String[]
                { "org.foo.BarException" });

        assertEquals(sig2.hashCode(), hashCode1);
        assertEquals(sig2, sig1);

        // Now work through the different properties, changing each one.

        sig2 = new MethodSignature(Modifier.PRIVATE, "int", "doSomething", new String[]
        { "int" }, new String[]
        { "org.foo.BarException" });

        assertFalse(sig2.hashCode() == hashCode1);
        assertFalse(sig2.equals(sig1));

        sig2 = new MethodSignature(Modifier.PUBLIC, "long", "doSomething", new String[]
        { "int" }, new String[]
        { "org.foo.BarException" });

        assertFalse(sig2.hashCode() == hashCode1);
        assertFalse(sig2.equals(sig1));

        sig2 = new MethodSignature(Modifier.PUBLIC, "int", "doSomethingElse", new String[]
        { "int" }, new String[]
        { "org.foo.BarException" });

        assertFalse(sig2.hashCode() == hashCode1);
        assertFalse(sig2.equals(sig1));

        sig2 = new MethodSignature(Modifier.PUBLIC, "int", "doSomething", new String[]
        { "long" }, new String[]
        { "org.foo.BarException" });

        assertFalse(sig2.hashCode() == hashCode1);
        assertFalse(sig2.equals(sig1));

        sig2 = new MethodSignature(Modifier.PUBLIC, "int", "doSomething", new String[]
        { "int" }, new String[0]);

        assertFalse(sig2.hashCode() == hashCode1);
        assertFalse(sig2.equals(sig1));

        // Other equality checks

        assertFalse(sig1.equals(null));
        assertFalse(sig1.equals(""));
    }

    /** Tests the simple, no arguments constructor. */
    @Test
    public void short_constructor()
    {
        MethodSignature sig = new MethodSignature("pageLoad");

        assertEquals(sig.getModifiers(), Modifier.PUBLIC);
        assertEquals(sig.getReturnType(), "void");
        assertEquals(sig.getMethodName(), "pageLoad");
        assertEquals(sig.getParameterTypes(), new String[0]);
        assertEquals(sig.getExceptionTypes(), new String[0]);
    }
}
