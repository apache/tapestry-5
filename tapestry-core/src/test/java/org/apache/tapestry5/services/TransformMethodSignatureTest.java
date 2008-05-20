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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TransformMethodSignatureTest extends Assert
{

    @Test
    public void signature_toString()
    {
        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "int", "doSomething",
                                                                    new String[]
                                                                            { "java.lang.String", "int" }, new String[]
                { "java.lang.RuntimeException", "org.foo.FredException" });

        assertEquals(
                sig.toString(),
                "public int doSomething(java.lang.String, int) throws java.lang.RuntimeException, org.foo.FredException");

        sig = new TransformMethodSignature(Modifier.ABSTRACT + Modifier.PROTECTED, "boolean", "misoHapi",
                                           new String[0], new String[0]);

        assertEquals(sig.toString(), "protected abstract boolean misoHapi()");
    }

    @Test
    public void medium_description()
    {
        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "int", "doSomething",
                                                                    new String[]
                                                                            { "java.lang.String", "int" }, new String[]
                { "java.lang.RuntimeException", "org.foo.FredException" });

        assertEquals(sig.getMediumDescription(), "doSomething(java.lang.String, int)");
    }

    @Test
    public void package_private_toString()
    {
        TransformMethodSignature sig = new TransformMethodSignature(0, "int", "packagePrivate", null, null);

        assertEquals(sig.toString(), "int packagePrivate()");
    }

    @Test
    public void null_value_for_parameters_and_exceptions()
    {
        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "int", "doSomething", null, null);

        assertEquals(sig.toString(), "public int doSomething()");

        assertEquals(sig.getParameterTypes(), new String[0]);
        assertEquals(sig.getExceptionTypes(), new String[0]);
    }

    @Test
    public void getters()
    {
        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "int", "doSomething",
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
        TransformMethodSignature foo = new TransformMethodSignature(Modifier.PUBLIC, "void", "foo", null, null);
        TransformMethodSignature bar = new TransformMethodSignature(Modifier.PUBLIC, "void", "bar", null, null);
        TransformMethodSignature baz0 = new TransformMethodSignature(Modifier.PUBLIC, "void", "baz", null, null);
        TransformMethodSignature baz1 = new TransformMethodSignature(Modifier.PUBLIC, "void", "baz", new String[]
                { "int" }, null);

        List<TransformMethodSignature> list = CollectionFactory.newList(Arrays.asList(foo, bar, baz0, baz1));

        Collections.sort(list);

        assertEquals(list, Arrays.asList(bar, baz1, baz0, foo));
    }

    @Test
    public void hash_code_and_equals()
    {
        TransformMethodSignature sig1 = new TransformMethodSignature(Modifier.PUBLIC, "int", "doSomething",
                                                                     new String[]
                                                                             { "int" }, new String[]
                { "org.foo.BarException" });
        int hashCode1 = sig1.hashCode();

        // Check that same value returned each time.

        assertEquals(sig1.hashCode(), hashCode1);

        TransformMethodSignature sig2 = new TransformMethodSignature(Modifier.PUBLIC, "int", "doSomething",
                                                                     new String[]
                                                                             { "int" }, new String[]
                { "org.foo.BarException" });

        assertEquals(sig2.hashCode(), hashCode1);
        assertEquals(sig2, sig1);

        // Now work through the different properties, changing each one.

        sig2 = new TransformMethodSignature(Modifier.PRIVATE, "int", "doSomething", new String[]
                { "int" }, new String[]
                { "org.foo.BarException" });

        assertFalse(sig2.hashCode() == hashCode1);
        assertFalse(sig2.equals(sig1));

        sig2 = new TransformMethodSignature(Modifier.PUBLIC, "long", "doSomething", new String[]
                { "int" }, new String[]
                { "org.foo.BarException" });

        assertFalse(sig2.hashCode() == hashCode1);
        assertFalse(sig2.equals(sig1));

        sig2 = new TransformMethodSignature(Modifier.PUBLIC, "int", "doSomethingElse", new String[]
                { "int" }, new String[]
                { "org.foo.BarException" });

        assertFalse(sig2.hashCode() == hashCode1);
        assertFalse(sig2.equals(sig1));

        sig2 = new TransformMethodSignature(Modifier.PUBLIC, "int", "doSomething", new String[]
                { "long" }, new String[]
                { "org.foo.BarException" });

        assertFalse(sig2.hashCode() == hashCode1);
        assertFalse(sig2.equals(sig1));

        sig2 = new TransformMethodSignature(Modifier.PUBLIC, "int", "doSomething", new String[]
                { "int" }, new String[0]);

        assertFalse(sig2.hashCode() == hashCode1);
        assertFalse(sig2.equals(sig1));

        // Other equality checks

        assertFalse(sig1.equals(null));
        assertFalse(sig1.equals(""));
    }

    /**
     * Tests the simple, no arguments constructor.
     */
    @Test
    public void short_constructor()
    {
        TransformMethodSignature sig = new TransformMethodSignature("pageLoad");

        assertEquals(sig.getModifiers(), Modifier.PUBLIC);
        assertEquals(sig.getReturnType(), "void");
        assertEquals(sig.getMethodName(), "pageLoad");
        assertEquals(sig.getParameterTypes(), new String[0]);
        assertEquals(sig.getExceptionTypes(), new String[0]);
    }
}
