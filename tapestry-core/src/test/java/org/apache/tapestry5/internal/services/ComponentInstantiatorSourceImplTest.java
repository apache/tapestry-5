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

package org.apache.tapestry5.internal.services;

import javassist.*;
import org.apache.tapestry5.internal.*;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.transform.pages.BasicComponent;
import org.apache.tapestry5.internal.transform.pages.BasicSubComponent;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.TapestryModule;
import org.apache.tapestry5.services.UpdateListenerHub;
import org.slf4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.UUID;

/**
 * Tests for {@link org.apache.tapestry5.internal.services.ComponentInstantiatorSourceImpl}. Several of these tests are
 * more of the form of integration tests that instantiate the Tapestry IoC Registry.
 */
public class ComponentInstantiatorSourceImplTest extends InternalBaseTestCase
{
    private static final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();

    private static final String SYNTH_COMPONENT_CLASSNAME = "org.apache.tapestry5.internal.transform.pages.SynthComponent";

    private final ClasspathURLConverter converter = new ClasspathURLConverterImpl();

    private File extraClasspath;

    private ComponentInstantiatorSource source;

    private Registry registry;

    private PropertyAccess access;

    private ClassLoader extraLoader;

    private String tempDir;

    @Test
    public void controlled_packages() throws Exception
    {
        ComponentClassTransformer transformer = newMock(ComponentClassTransformer.class);
        Logger logger = mockLogger();

        replay();

        ComponentInstantiatorSourceImpl e = new ComponentInstantiatorSourceImpl(logger, contextLoader, transformer,
                                                                                null, converter);

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

    /**
     * More of an integration test.
     */
    @Test
    public void load_component_via_service() throws Exception
    {
        Component target = createComponent(BasicComponent.class);

        // Should not be an instance, since it is loaded by a different class loader.
        assertFalse(BasicComponent.class.isInstance(target));

        access.set(target, "value", "some default value");
        assertEquals(access.get(target, "value"), "some default value");

        access.set(target, "retainedValue", "some retained value");
        assertEquals(access.get(target, "retainedValue"), "some retained value");

        // Setting a property value before pageDidLoad will cause that value
        // to be the default when the page detaches.

        target.containingPageDidLoad();

        access.set(target, "value", "some transient value");
        assertEquals(access.get(target, "value"), "some transient value");

        target.containingPageDidDetach();

        assertEquals(access.get(target, "value"), "some default value");
        assertEquals(access.get(target, "retainedValue"), "some retained value");
    }

    @Test
    public void load_sub_component_via_service() throws Exception
    {
        Component target = createComponent(BasicSubComponent.class);

        target.containingPageDidLoad();

        access.set(target, "value", "base class");
        assertEquals(access.get(target, "value"), "base class");

        access.set(target, "intValue", 33);
        assertEquals(access.get(target, "intValue"), 33);

        target.containingPageDidDetach();

        assertNull(access.get(target, "value"));
        assertEquals(access.get(target, "intValue"), 0);
    }

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

        Named named = (Named) createComponent(SYNTH_COMPONENT_CLASSNAME);

        assertEquals(named.getName(), "Original");

        String path = tempDir + "/" + SYNTH_COMPONENT_CLASSNAME.replace('.', '/') + ".class";
        URL url = new File(path).toURL();

        long dtm = readDTM(url);

        while (true)
        {
            if (readDTM(url) != dtm) break;

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

    private Component createComponent(Class componentClass)
    {
        String classname = componentClass.getName();

        return createComponent(classname);
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

        extraLoader = new URLClassLoader(new URL[] { url }, contextLoader);
        RegistryBuilder builder = new RegistryBuilder(extraLoader);

        builder.add(TapestryModule.class);

        SymbolProvider provider = new SingleKeySymbolProvider(InternalSymbols.ALIAS_MODE, "servlet");
        ContributionDef contribution = new SyntheticSymbolSourceContributionDef("AliasMode", provider,
                                                                                "before:ApplicationDefaults");

        ModuleDef module = new SyntheticModuleDef(contribution);

        builder.add(module);

        registry = builder.build();

        // registry.getService("Alias", Alias.class).setMode("servlet");

        source = registry.getService(ComponentInstantiatorSource.class);
        access = registry.getService(PropertyAccess.class);

        source.addPackage("org.apache.tapestry5.internal.transform.pages");
    }

    @AfterClass
    public void cleanup_tests()
    {
        registry.shutdown();

        registry = null;
        source = null;
        access = null;
    }
}
