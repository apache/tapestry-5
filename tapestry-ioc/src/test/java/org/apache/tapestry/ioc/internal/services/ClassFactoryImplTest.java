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

package org.apache.tapestry.ioc.internal.services;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFabUtils;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.MethodSignature;
import org.apache.tapestry.ioc.test.IOCTestCase;
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
    public void get_method_line_number() throws Exception
    {
        ClassFactory factory = new ClassFactoryImpl();

        Class target = LineNumberBean.class;

        Method m = target.getMethod("fred");

        // 21 is the line containing the close brace

        assertEquals(factory.getMethodLineNumber(m), 21);

        m = target.getMethod("betty", String.class, int.class);

        // 25 is the line of the return statement

        assertEquals(factory.getMethodLineNumber(m), 25);

        m = target.getDeclaredMethod("wilma", int[].class, Double[][][].class);

        assertEquals(factory.getMethodLineNumber(m), 30);
    }

    private void addRunMethod(ClassFab cf)
    {
        cf.addMethod(Modifier.PUBLIC, new MethodSignature(void.class, "run", null, null), " { } ");
    }
}
