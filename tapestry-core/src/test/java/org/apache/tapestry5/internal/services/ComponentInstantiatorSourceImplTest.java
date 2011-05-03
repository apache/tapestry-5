// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.UUID;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.transform.pages.BasicComponent;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.TapestryModule;
import org.apache.tapestry5.services.UpdateListenerHub;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.apache.tapestry5.internal.services.ComponentInstantiatorSourceImpl}. Several of these tests are
 * more of the form of integration tests that instantiate the Tapestry IoC Registry.
 */
public class ComponentInstantiatorSourceImplTest extends InternalBaseTestCase
{
    private static final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

    private static final String SYNTH_COMPONENT_CLASSNAME = "org.apache.tapestry5.internal.transform.pages.SynthComponent";

    private File extraClasspath;

    private ComponentInstantiatorSource source;

    private Registry registry;

    private ClassLoader extraLoader;

    private String tempDir;

    /**
     * This allows tests the exists() method.
     */
    @Test
    public void component_class_reload() throws Exception
    {
        // Ensure it doesn't already exist:

        assertFalse(source.exists(SYNTH_COMPONENT_CLASSNAME));

        // Create the class on the fly.

        createSynthComponentClass("Original");

        assertTrue(source.exists(SYNTH_COMPONENT_CLASSNAME));

        getMocksControl().resetToNice();

        Named named = (Named) createComponent(SYNTH_COMPONENT_CLASSNAME);

        assertEquals(named.getName(), "Original");

        String path = tempDir + "/" + SYNTH_COMPONENT_CLASSNAME.replace('.', '/') + ".class";
        URL url = new File(path).toURL();

        long dtm = readDTM(url);

        while (true)
        {
            if (readDTM(url) != dtm)
                break;

            // Keep re-writing the file until we see the DTM change.

            createSynthComponentClass("Updated");
        }

        // Detect the change and clear out the internal caches

        UpdateListenerHub hub = registry.getService("UpdateListenerHub", UpdateListenerHub.class);

        hub.fireCheckForUpdates();

        // This will be the new version of the class

        named = (Named) createComponent(SYNTH_COMPONENT_CLASSNAME);

        assertEquals(named.getName(), "Updated");
    }

    private long readDTM(URL url) throws Exception
    {
        URLConnection connection = url.openConnection();

        connection.connect();

        return connection.getLastModified();
    }

    private void createSynthComponentClass(String name) throws CannotCompileException, NotFoundException, IOException
    {
        ClassPool pool = new ClassPool();
        // Inside Maven Surefire, the system classpath is not sufficient to find all
        // the necessary files.
        pool.appendClassPath(new LoaderClassPath(extraLoader));

        CtClass ctClass = pool.makeClass(SYNTH_COMPONENT_CLASSNAME);

        ctClass.setSuperclass(pool.get(BasicComponent.class.getName()));

        // Implement method getName()

        CtMethod method = CtNewMethod.make("public String getName() { return \"" + name + "\"; }", ctClass);
        ctClass.addMethod(method);

        ctClass.addInterface(pool.get(Named.class.getName()));

        ctClass.writeFile(extraClasspath.getAbsolutePath());
    }

    private Component createComponent(String classname)
    {
        InternalComponentResources resources = mockInternalComponentResources();

        replay();

        Instantiator inst = source.getInstantiator(classname);

        Component target = inst.newInstance(resources);

        verify();

        return target;
    }

    @BeforeClass
    public void setup_tests() throws Exception
    {
        String tempdir = System.getProperty("java.io.tmpdir");
        String uid = UUID.randomUUID().toString();

        tempDir = tempdir + "/tapestry-test-classpath/" + uid;
        extraClasspath = new File(tempDir);

        System.out.println("Creating dir: " + extraClasspath);

        extraClasspath.mkdirs();

        URL url = extraClasspath.toURL();

        extraLoader = new URLClassLoader(new URL[]
        { url }, contextLoader);
        RegistryBuilder builder = new RegistryBuilder(extraLoader);

        builder.add(TapestryModule.class, ForceDevelopmentModeModule.class, AddTransformPagesToCISModule.class);

        registry = builder.build();

        source = registry.getService(ComponentInstantiatorSource.class);
    }

    @AfterClass
    public void cleanup_tests()
    {
        registry.shutdown();

        registry = null;
        source = null;
    }
}
