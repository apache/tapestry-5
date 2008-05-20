// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import javassist.CtClass;
import org.apache.tapestry5.ioc.BaseLocatable;
import org.apache.tapestry5.ioc.internal.services.LoggingDecoratorImplTest.ToStringService;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

public class ClassFabImplTest extends IOCTestCase
{
    private final CtClassSource source;

    private final PropertyAccess access = new PropertyAccessImpl();

    public interface SampleService
    {
        int primitiveMethod(int primitiveValue);

        void voidMethod(String input);

        String objectMethod(String input);
    }

    public interface SampleToStringService
    {
        String toString();
    }

    public ClassFabImplTest()
    {
        ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();

        ClassFactoryClassPool pool = new ClassFactoryClassPool(threadLoader);

        pool.addClassLoaderIfNeeded(threadLoader);

        source = new CtClassSourceImpl(pool, threadLoader);
    }

    private ClassFab newClassFab(String className, Class superClass)
    {
        CtClass ctClass = source.newClass(className, superClass);

        return new ClassFabImpl(source, ctClass, LoggerFactory.getLogger("ClassFab"));
    }

    @Test
    public void create_simple_bean() throws Exception
    {
        ClassFab cf = newClassFab("TargetBean", Object.class);

        cf.addField("_stringValue", String.class);

        MethodSignature setStringValue = new MethodSignature(void.class, "setStringValue", new Class[] { String.class },
                                                             null);

        cf.addMethod(Modifier.PUBLIC, setStringValue, "_stringValue = $1;");

        MethodSignature getStringValue = new MethodSignature(String.class, "getStringValue", null, null);

        cf.addMethod(Modifier.PUBLIC, getStringValue, "return _stringValue;");

        Class targetClass = cf.createClass();

        Object targetBean = targetClass.newInstance();

        access.set(targetBean, "stringValue", "Fred");

        // May keep a test-time dependency on HiveMind, just for PropertyUtils.

        String actual = (String) access.get(targetBean, "stringValue");

        assertEquals(actual, "Fred");
    }

    @Test
    public void add_to_string() throws Exception
    {
        ClassFab cf = newClassFab("ToString", Object.class);

        cf.addToString("ToString Description");

        Class clazz = cf.createClass();

        Object instance = clazz.newInstance();

        assertEquals(instance.toString(), "ToString Description");
    }

    @Test
    public void proxy_methods_to_delegate() throws Exception
    {
        ClassFab cf = newClassFab("Delegator", Object.class);

        cf.addField("_delegate", SampleService.class);
        cf.addConstructor(new Class[] { SampleService.class }, null, "_delegate = $1;");

        cf.proxyMethodsToDelegate(SampleService.class, "_delegate", "<Delegator>");

        SampleService delegate = newMock(SampleService.class);

        Class clazz = cf.createClass();

        SampleService proxy = (SampleService) clazz.getConstructors()[0].newInstance(delegate);

        expect(delegate.primitiveMethod(5)).andReturn(10);

        delegate.voidMethod("fred");

        expect(delegate.objectMethod("barney")).andReturn("rubble");

        replay();

        assertEquals(proxy.primitiveMethod(5), 10);

        proxy.voidMethod("fred");

        assertEquals(proxy.objectMethod("barney"), "rubble");
        assertEquals(proxy.toString(), "<Delegator>");

        verify();
    }

    @Test
    public void proxy_methods_to_delegate_with_to_string() throws Exception
    {
        ClassFab cf = newClassFab("ToStringDelegator", Object.class);

        cf.addField("_delegate", ToStringService.class);
        cf.addConstructor(new Class[] { ToStringService.class }, null, "_delegate = $1;");

        cf.proxyMethodsToDelegate(ToStringService.class, "_delegate", "<ToStringDelegator>");

        ToStringService delegate = new ToStringService()
        {
            @Override
            public String toString()
            {
                return "ACTUAL TO-STRING";
            }
        };

        Class clazz = cf.createClass();

        ToStringService proxy = (ToStringService) clazz.getConstructors()[0].newInstance(delegate);

        assertEquals(proxy.toString(), "ACTUAL TO-STRING");
    }

    @Test
    public void add_constructor() throws Exception
    {
        ClassFab cf = newClassFab("ConstructableBean", Object.class);

        cf.addField("_stringValue", String.class);
        cf.addConstructor(new Class[] { String.class }, null, "{ _stringValue = $1; }");

        MethodSignature getStringValue = new MethodSignature(String.class, "getStringValue", null, null);

        cf.addMethod(Modifier.PUBLIC, getStringValue, "return _stringValue;");

        Class targetClass = cf.createClass();

        try
        {
            targetClass.newInstance();
            unreachable();
        }
        catch (InstantiationException ex)
        {
        }

        Constructor c = targetClass.getConstructors()[0];

        Object targetBean = c.newInstance(new Object[] { "Buffy" });

        String actual = (String) access.get(targetBean, "stringValue");

        assertEquals("Buffy", actual);
    }

    @Test
    public void add_constructor_from_base_class() throws Exception
    {
        ClassFab cf = newClassFab("MyIntHolder", AbstractIntWrapper.class);

        cf.addField("_intValue", int.class);
        cf.addConstructor(new Class[] { int.class }, null, "{ _intValue = $1; }");

        cf.addMethod(Modifier.PUBLIC, new MethodSignature(int.class, "getIntValue", null, null), "return _intValue;");

        Class targetClass = cf.createClass();
        Constructor c = targetClass.getConstructors()[0];

        AbstractIntWrapper targetBean = (AbstractIntWrapper) c.newInstance(new Object[] { new Integer(137) });

        assertEquals(targetBean.getIntValue(), 137);
    }

    @Test
    public void invalid_super_class() throws Exception
    {
        ClassFab cf = newClassFab("InvalidSuperClass", List.class);

        try
        {
            cf.createClass();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertExceptionSubstring(ex, "Unable to create class InvalidSuperClass");
        }
    }

    private void assertExceptionSubstring(Throwable t, String partialMessage)
    {
        assertTrue(t.getMessage().contains(partialMessage));
    }

    @Test
    public void add_interface() throws Exception
    {
        ClassFab cf = newClassFab("SimpleService", Object.class);

        cf.addInterface(SimpleService.class);

        cf.addMethod(Modifier.PUBLIC, new MethodSignature(int.class, "add", new Class[] { int.class, int.class }, null),
                     "return $1 + $2;");

        Class targetClass = cf.createClass();

        SimpleService s = (SimpleService) targetClass.newInstance();

        assertEquals(207, s.add(99, 108));
    }

    @Test
    public void attempt_to_subclass_from_final_class() throws Exception
    {
        ClassFab cf = newClassFab("StringSubclass", String.class);

        try
        {
            cf.createClass();
        }
        catch (RuntimeException ex)
        {
            assertExceptionRegexp(ex, "Unable to create class StringSubclass\\: .*");
        }
    }

    private void assertExceptionRegexp(Throwable ex, String pattern)
    {
        assertTrue(ex.getMessage().matches(pattern));
    }

    @Test
    public void create_class_within_non_default_package() throws Exception
    {
        ClassFab cf = newClassFab("org.apache.hivemind.InPackage", Object.class);

        Class c = cf.createClass();

        Object o = c.newInstance();

        assertEquals("org.apache.hivemind.InPackage", o.getClass().getName());
    }

    @Test
    public void invalid_method_body() throws Exception
    {
        ClassFab cf = newClassFab("BadMethodBody", Object.class);

        cf.addInterface(Runnable.class);

        try
        {
            cf.addMethod(Modifier.PUBLIC, new MethodSignature(void.class, "run", null, null), "fail;");
        }
        catch (RuntimeException ex)
        {
            assertExceptionSubstring(ex, "Unable to add method void run() to class BadMethodBody:");
        }
    }

    @Test
    public void add_duplicate_method_signature() throws Exception
    {
        ClassFab cf = newClassFab("DupeMethodAdd", Object.class);

        cf.addMethod(Modifier.PUBLIC, new MethodSignature(void.class, "foo", null, null), "{}");

        try
        {
            cf.addMethod(Modifier.PUBLIC, new MethodSignature(void.class, "foo", null, null), "{}");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals("Attempt to redefine method void foo() of class DupeMethodAdd.", ex
                    .getMessage());
        }
    }

    @Test
    public void invalid_constructor_body() throws Exception
    {
        ClassFab cf = newClassFab("BadConstructor", Object.class);

        try
        {
            cf.addConstructor(null, null, " woops!");
        }
        catch (RuntimeException ex)
        {
            assertExceptionSubstring(ex, "Unable to add constructor to class BadConstructor");
        }

    }

    @Test
    public void invalid_field() throws Exception
    {
        ClassFab cf = newClassFab("InvalidField", Object.class);

        // You'd think some of these would fail, but the ultimate failure
        // occurs when we create the class.

        cf.addField("a%b", String.class);
        cf.addField("", int.class);

        // Aha! Adding a duplicate fails!

        cf.addField("buffy", int.class);

        try
        {
            cf.addField("buffy", String.class);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Unable to add field buffy to class InvalidField: duplicate field: buffy");
        }

    }

    @Test
    public void to_string() throws Exception
    {
        ClassFab cf = newClassFab("FredRunnable", BaseLocatable.class);

        cf.addInterface(Runnable.class);
        cf.addInterface(Serializable.class);

        cf.addField("_map", Map.class);

        cf.addConstructor(new Class[] { Map.class, Runnable.class },
                          new Class[] { IllegalArgumentException.class, DataFormatException.class }, "{ _map = $1; }");

        MethodSignature sig = new MethodSignature(Map.class, "doTheNasty", new Class[] { int.class, String.class },
                                                  new Class[] { InstantiationException.class,
                                                          IllegalAccessException.class });

        cf.addMethod(Modifier.PUBLIC + Modifier.FINAL + Modifier.SYNCHRONIZED, sig, "{ return _map; }");

        String toString = cf.toString();

        assertContains(toString,
                       "public class FredRunnable extends " + BaseLocatable.class.getName() + "\n" + "  implements java.lang.Runnable, java.io.Serializable");

        assertContains(toString, "private java.util.Map _map;");

        assertContains(toString,
                       "public FredRunnable(java.util.Map $1, java.lang.Runnable $2)\n" + "  throws java.lang.IllegalArgumentException, java.util.zip.DataFormatException\n" + "{ _map = $1; }");

        assertContains(toString,
                       "public final synchronized java.util.Map doTheNasty(int $1, java.lang.String $2)\n" + "  throws java.lang.InstantiationException, java.lang.IllegalAccessException\n" + "{ return _map; }");

    }

    @Test
    public void add_noop_method() throws Exception
    {
        ClassFab cf = newClassFab("NoOp", Object.class);
        cf.addInterface(Runnable.class);

        cf.addNoOpMethod(new MethodSignature(void.class, "run", null, null));
        cf.addNoOpMethod(new MethodSignature(int.class, "getInt", null, null));
        cf.addNoOpMethod(new MethodSignature(double.class, "getDouble", null, null));

        Class clazz = cf.createClass();

        Runnable instance = (Runnable) clazz.newInstance();

        instance.run();

        assertEquals(access.get(instance, "int"), 0);
        assertEquals(access.get(instance, "double"), 0.0d);
    }

    private void assertContains(String actual, String expectedSubstring)
    {
        assertTrue(actual.contains(expectedSubstring), "Missing substring: " + expectedSubstring);
    }
}
