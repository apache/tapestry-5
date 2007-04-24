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

package org.apache.tapestry.internal.services;

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

import org.apache.commons.logging.Log;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.internal.transform.pages.BasicComponent;
import org.apache.tapestry.internal.transform.pages.BasicSubComponent;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.RegistryBuilder;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.Infrastructure;
import org.apache.tapestry.services.TapestryModule;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.apache.tapestry.internal.services.ComponentInstantiatorSourceImpl}. Several
 * of these tests are more of the form of integration tests that instantiate the Tapestry IoC
 * Registry.
 */
public class ComponentInstantiatorSourceImplTest extends InternalBaseTestCase
{
    private static final ClassLoader _contextLoader = Thread.currentThread()
            .getContextClassLoader();

    private static final String SYNTH_COMPONENT_CLASSNAME = "org.apache.tapestry.internal.transform.pages.SynthComponent";

    private File _extraClasspath;

    private ComponentInstantiatorSource _source;

    private Registry _registry;

    private PropertyAccess _access;

    private ClassLoader _extraLoader;

    private String _tempDir;

    @Test
    public void controlled_packages() throws Exception
    {
        ComponentClassTransformer transformer = newMock(ComponentClassTransformer.class);
        Log log = newLog();

        replay();

        ComponentInstantiatorSourceImpl e = new ComponentInstantiatorSourceImpl(_contextLoader,
                transformer, log);

        assertEquals(e.inControlledPackage("foo.bar.Baz"), false);

        // Check that classes in the default package are never controlled

        assertEquals(e.inControlledPackage("Biff"), false);

        // Now add a controlled package

        e.addPackage("foo.bar");

        assertEquals(e.inControlledPackage("foo.bar.Baz"), true);

        // Sub-packages of controlled packages are controlled as well

        assertEquals(e.inControlledPackage("foo.bar.biff.Pop"), true);

        // Parents of controlled packages are not controlled

        assertEquals(e.inControlledPackage("foo.Gloop"), false);

        verify();
    }

    /** More of an integration test. */
    @Test
    public void load_component_via_service() throws Exception
    {
        Component target = createComponent(BasicComponent.class);

        // Should not be an instance, since it is loaded by a different class loader.
        assertFalse(BasicComponent.class.isInstance(target));

        _access.set(target, "value", "some default value");
        assertEquals(_access.get(target, "value"), "some default value");

        _access.set(target, "retainedValue", "some retained value");
        assertEquals(_access.get(target, "retainedValue"), "some retained value");

        // Setting a property value before pageDidLoad will cause that value
        // to be the default when the page detaches.

        target.containingPageDidLoad();

        _access.set(target, "value", "some transient value");
        assertEquals(_access.get(target, "value"), "some transient value");

        target.containingPageDidDetach();

        assertEquals(_access.get(target, "value"), "some default value");
        assertEquals(_access.get(target, "retainedValue"), "some retained value");
    }

    @Test
    public void load_sub_component_via_service() throws Exception
    {
        Component target = createComponent(BasicSubComponent.class);

        target.containingPageDidLoad();

        _access.set(target, "value", "base class");
        assertEquals(_access.get(target, "value"), "base class");

        _access.set(target, "intValue", 33);
        assertEquals(_access.get(target, "intValue"), 33);

        target.containingPageDidDetach();

        assertNull(_access.get(target, "value"));
        assertEquals(_access.get(target, "intValue"), 0);
    }

    /**
     * This allows tests the exists() method.
     */
    @Test
    public void component_class_reload() throws Exception
    {
        // Ensure it doesn't already exist:

        assertFalse(_source.exists(SYNTH_COMPONENT_CLASSNAME));

        // Create the class on the fly.

        createSynthComponentClass("Original");

        assertTrue(_source.exists(SYNTH_COMPONENT_CLASSNAME));

        Named named = (Named) createComponent(SYNTH_COMPONENT_CLASSNAME);

        assertEquals(named.getName(), "Original");

        String path = _tempDir + "/" + SYNTH_COMPONENT_CLASSNAME.replace('.', '/') + ".class";
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

        UpdateListenerHub hub = _registry.getService(
                "tapestry.internal.UpdateListenerHub",
                UpdateListenerHub.class);

        hub.fireUpdateEvent();

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

    private void createSynthComponentClass(String name) throws CannotCompileException,
            NotFoundException, IOException
    {
        ClassPool pool = new ClassPool();
        // Inside Maven Surefire, the system classpath is not sufficient to find all
        // the necessary files.
        pool.appendClassPath(new LoaderClassPath(_extraLoader));

        CtClass ctClass = pool.makeClass(SYNTH_COMPONENT_CLASSNAME);

        ctClass.setSuperclass(pool.get(BasicComponent.class.getName()));

        // Implement method getName()

        CtMethod method = CtNewMethod.make(
                "public String getName() { return \"" + name + "\"; }",
                ctClass);
        ctClass.addMethod(method);

        ctClass.addInterface(pool.get(Named.class.getName()));

        ctClass.writeFile(_extraClasspath.getAbsolutePath());
    }

    private Component createComponent(Class componentClass)
    {
        String classname = componentClass.getName();

        return createComponent(classname);
    }

    private Component createComponent(String classname)
    {
        InternalComponentResources resources = newInternalComponentResources();

        replay();

        // Can't wait for the HiveMind code base to start using some generics for this kind of
        // thing.

        Instantiator inst = _source.findInstantiator(classname);

        Component target = inst.newInstance(resources);

        verify();

        return target;
    }

    @BeforeClass
    public void setup_tests() throws Exception
    {
        String tempdir = System.getProperty("java.io.tmpdir");
        String uid = UUID.randomUUID().toString();

        _tempDir = tempdir + "/tapestry-test-classpath/" + uid;
        _extraClasspath = new File(_tempDir);

        System.out.println("Creating dir: " + _extraClasspath);

        _extraClasspath.mkdirs();

        URL url = _extraClasspath.toURL();

        _extraLoader = new URLClassLoader(new URL[]
        { url }, _contextLoader);
        RegistryBuilder builder = new RegistryBuilder(_extraLoader);

        builder.add(TapestryModule.class);

        _registry = builder.build();

        _registry.getService("tapestry.Infrastructure", Infrastructure.class).setMode("servlet");

        _source = _registry.getService(ComponentInstantiatorSource.class);
        _access = _registry.getService(PropertyAccess.class);

        _source.addPackage("org.apache.tapestry.internal.transform.pages");
    }

    @AfterClass
    public void cleanup_tests()
    {
        _registry.shutdown();

        _registry = null;
        _source = null;
        _access = null;
    }
}
