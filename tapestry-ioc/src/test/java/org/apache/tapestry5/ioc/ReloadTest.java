// Copyright 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import org.apache.tapestry5.ioc.test.TestBase;
import org.apache.tapestry5.services.UpdateListenerHub;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.example.ReloadModule;
import com.example.ReloadableService;

/**
 * Test the ability to perform live class reloading of a service implementation.
 */
@SuppressWarnings("unchecked")
public class ReloadTest extends TestBase
{
    private static final String PACKAGE = "com.example";

    private static final String CLASS = PACKAGE + ".ReloadableServiceImpl";

    private File classesDir;

    private ClassLoader classLoader;

    @BeforeClass
    public void setup() throws Exception
    {
        String uid = Long.toHexString(System.currentTimeMillis());

        classesDir = new File(System.getProperty("java.io.tmpdir"), uid);

        // URLClassLoader REQUIRES that File URLs end with a slash! That's a half hour of my life gone!

        URL classesURL = new URL("file:" + classesDir.getCanonicalPath() + "/");

        System.out.println("Reload classes dir: " + classesURL);

        classLoader = new URLClassLoader(new URL[]
        { classesURL }, Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void reload_a_service_implementation() throws Exception
    {
        // First, create the initial implementation

        createImplementationClass("initial");

        Registry registry = createRegistry();

        ReloadableService reloadable = registry.getService(ReloadableService.class);

        fireUpdateCheck(registry);

        assertEquals(reloadable.getStatus(), "initial");

        fireUpdateCheck(registry);

        // Sleep long enough that the Java millisecond clock advances.

        Thread.currentThread().sleep(1500);

        createImplementationClass("updated");

        // Doesn't take effect until after the update check

        assertEquals(reloadable.getStatus(), "initial");

        fireUpdateCheck(registry);

        assertEquals(reloadable.getStatus(), "updated");

        registry.shutdown();
    }

    @Test
    public void reload_a_proxy_object() throws Exception
    {
        createImplementationClass("initial proxy");

        Registry registry = createRegistry();

        Class<ReloadableService> clazz = (Class<ReloadableService>) classLoader.loadClass(CLASS);

        ReloadableService reloadable = registry.proxy(ReloadableService.class, clazz);

        assertEquals(reloadable.getStatus(), "initial proxy");

        Thread.currentThread().sleep(1500);

        createImplementationClass("updated proxy");

        fireUpdateCheck(registry);

        assertEquals(reloadable.getStatus(), "updated proxy");

        registry.shutdown();
    }

    private void fireUpdateCheck(Registry registry)
    {
        registry.getService(UpdateListenerHub.class).fireCheckForUpdates();
    }

    private Registry createRegistry()
    {
        RegistryBuilder builder = new RegistryBuilder(classLoader);

        builder.add(ReloadModule.class);

        return builder.build();
    }

    @Test
    public void invalid_service_implementation() throws Exception
    {
        createImplementationClass("initial");

        Registry registry = createRegistry();

        ReloadableService reloadable = registry.getService(ReloadableService.class);

        createInvalidImplentationClass();

        Thread.currentThread().sleep(1500);

        fireUpdateCheck(registry);

        try
        {
            reloadable.getStatus();

            unreachable();
        }
        catch (Exception ex)
        {
            assertEquals(ex.getMessage(),
                    "Service implementation class com.example.ReloadableServiceImpl does not have a suitable public constructor.");
        }

        registry.shutdown();
    }

    private void createImplementationClass(String status) throws Exception
    {
        ClassPool pool = new ClassPool(null);

        pool.appendSystemPath();

        CtClass ctClass = pool.makeClass(CLASS);

        ctClass.addInterface(pool.get(ReloadableService.class.getName()));

        CtMethod method = new CtMethod(pool.get("java.lang.String"), "getStatus", null, ctClass);

        method.setBody(String.format("return \"%s\";", status));

        ctClass.addMethod(method);

        ctClass.writeFile(classesDir.getAbsolutePath());
    }

    private void createInvalidImplentationClass() throws Exception
    {
        ClassPool pool = new ClassPool(null);

        pool.appendSystemPath();

        CtClass ctClass = pool.makeClass(CLASS);

        ctClass.addInterface(pool.get(ReloadableService.class.getName()));

        CtMethod method = new CtMethod(pool.get("java.lang.String"), "getStatus", null, ctClass);

        method.setBody("return \"unreachable\";");

        ctClass.addMethod(method);

        CtConstructor constructor = new CtConstructor(new CtClass[0], ctClass);

        constructor.setBody("return $0;");

        constructor.setModifiers(Modifier.PROTECTED);

        ctClass.addConstructor(constructor);

        ctClass.writeFile(classesDir.getAbsolutePath());

    }
}
