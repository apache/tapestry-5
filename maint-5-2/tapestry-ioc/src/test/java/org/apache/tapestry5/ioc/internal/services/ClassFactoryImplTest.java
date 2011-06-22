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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.internal.util.LocationImpl;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

public class ClassFactoryImplTest extends IOCTestCase
{
    public static class BaseClass
    {
        public void run()
        {
        }
    }

    @Test
    public void new_class_with_name_and_base_class() throws Exception
    {
        ClassFactory factory = new ClassFactoryImpl();
        String name = ClassFabUtils.generateClassName(Runnable.class);

        ClassFab cf = factory.newClass(name, Object.class);
        cf.addInterface(Runnable.class);

        addRunMethod(cf);

        Class newClass = cf.createClass();

        Runnable instance = (Runnable) newClass.newInstance();

        instance.run();
    }

    @Test
    public void new_class_with_non_object_base_class() throws Exception
    {
        ClassFactory factory = new ClassFactoryImpl();
        String name = ClassFabUtils.generateClassName(Runnable.class);

        ClassFab cf = factory.newClass(name, BaseClass.class);
        cf.addInterface(Runnable.class);

        Class newClass = cf.createClass();

        Runnable instance = (Runnable) newClass.newInstance();

        instance.run();
    }

    @Test
    public void new_class_with_interface() throws Exception
    {
        ClassFactory factory = new ClassFactoryImpl();

        ClassFab cf = factory.newClass(Runnable.class);

        addRunMethod(cf);

        Class newClass = cf.createClass();

        Runnable instance = (Runnable) newClass.newInstance();

        instance.run();
    }

    @Test
    public void get_method_location() throws Exception
    {
        ClassFactory factory = new ClassFactoryImpl();

        Class target = LineNumberBean.class;

        Method m = target.getMethod("fred");

        // 21 is the line containing the close brace

        Location l = factory.getMethodLocation(m);
        assertEquals(
                l.toString(),
                "org.apache.tapestry5.ioc.internal.services.LineNumberBean.fred() (at LineNumberBean.java:25)");
        assertEquals(l.getLine(), 25);

        m = target.getMethod("betty", String.class, int.class);

        // 25 is the line of the return statement

        assertEquals(
                factory.getMethodLocation(m).toString(),
                "org.apache.tapestry5.ioc.internal.services.LineNumberBean.betty(String, int) (at LineNumberBean.java:29)");

        m = target.getDeclaredMethod("wilma", int[].class, Double[][][].class);

        assertEquals(
                factory.getMethodLocation(m).toString(),
                "org.apache.tapestry5.ioc.internal.services.LineNumberBean.wilma(int[], Double[][][]) (at LineNumberBean.java:34)");
    }

    private void addRunMethod(ClassFab cf)
    {
        cf.addMethod(Modifier.PUBLIC, new MethodSignature(void.class, "run", null, null), " { } ");
    }

    @Test
    public void get_constructor_location() throws Exception
    {
        Constructor cc = LineNumberBean.class.getConstructors()[0];

        ClassFactory factory = new ClassFactoryImpl();

        // Eclipse and Sun JDK don't agree on the line number, so we'll accept either.

        assertTrue(factory
                .getConstructorLocation(cc)
                .toString()
                .matches(
                "org.apache.tapestry5.ioc.internal.services.LineNumberBean\\(String, int\\) \\(at LineNumberBean.java:(19|20)\\)"));
    }

    /**
     * Import a class (or two) where the class is from a known and available class loader.
     */
    @Test
    public void import_ordinary_class()
    {
        ClassFactory factory = new ClassFactoryImpl();

        assertSame(factory.importClass(Object.class), Object.class);
        assertSame(factory.importClass(LocationImpl.class), LocationImpl.class);
    }

    /**
     * Import a class where the bytecode is not available, to ensure that the super-class (from an available class
     * loader) is returned.
     */
    @Test
    public void import_proxy_class() throws Exception
    {
        ClassFactory alienFactory = new ClassFactoryImpl();

        Class<TargetBean> clazz = TargetBean.class;

        ClassFab cf = alienFactory.newClass(clazz.getName() + "$$Proxy", clazz);

        Class alienClass = cf.createClass();

        ClassFactory factory = new ClassFactoryImpl();

        assertSame(factory.importClass(alienClass), clazz);
    }
    
    
    @Test
    public void with_annotations() throws Exception
    {
        ClassFactory factory = new ClassFactoryImpl();

        TestService proxy = factory.createProxy(TestService.class, TestServiceImpl.class, new ObjectCreator()
        {
            public Object createObject()
            {
                return new TestServiceImpl();
            }
        }, "Proxy<TestService>");
        
        Class clazz =  proxy.getClass();
        
        SimpleAnnotation annotation = (SimpleAnnotation) clazz.getAnnotation(SimpleAnnotation.class);
        
        assertNotNull(annotation);
        assertEquals(annotation.value(), "TestServiceImpl");
        
        Method method = clazz.getMethod("doSomething");

        annotation = method.getAnnotation(SimpleAnnotation.class);
        assertEquals(annotation.value(), "TestServiceImpl#doSomething");
    }
    

    public interface TestService
    {
        void doSomething();
    }
    
    @SimpleAnnotation("TestServiceImpl")
    public class TestServiceImpl implements TestService
    {
        @SimpleAnnotation("TestServiceImpl#doSomething")
        public void doSomething()
        {
            
        }
    }
}
