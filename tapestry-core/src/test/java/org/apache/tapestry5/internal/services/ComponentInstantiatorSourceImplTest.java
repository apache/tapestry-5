// Copyright 2006-2013 The Apache Software Foundation
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

import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.plastic.asm.ClassWriter;
import org.apache.tapestry5.internal.plastic.asm.MethodVisitor;
import org.apache.tapestry5.internal.t5internal.pages.BasicComponent;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;
import org.apache.tapestry5.modules.TapestryModule;
import org.apache.tapestry5.runtime.Component;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import static org.apache.tapestry5.internal.plastic.asm.Opcodes.ACC_PUBLIC;
import static org.apache.tapestry5.internal.plastic.asm.Opcodes.ARETURN;

/**
 * Tests for {@link org.apache.tapestry5.internal.services.ComponentInstantiatorSourceImpl}. Several of these tests are
 * more of the form of integration tests that instantiate the Tapestry IoC Registry.
 */
public class ComponentInstantiatorSourceImplTest extends InternalBaseTestCase
{
    // BaseComponent and SynthComponent need to be inside controlled packages, as defined
    // by some contributed LibraryMapping.
    // org.apache.tapestry5.internal.t5internal is a handy package to use.

    private static final String BASIC_COMPONENT_CLASSNAME = BasicComponent.class.getName();

    private static final String SYNTH_COMPONENT_CLASSNAME = "org.apache.tapestry5.internal.t5internal.pages.SynthComponent";

    private ComponentInstantiatorSource source;

    private ClassCreationHelper helper;

    private Registry registry;

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

        long dtm = helper.readDTM(SYNTH_COMPONENT_CLASSNAME);

        while (true)
        {
            createSynthComponentClass("Updated");

            if (helper.readDTM(SYNTH_COMPONENT_CLASSNAME) != dtm)
                break;

            // Keep re-writing the file until we see the DTM change.
        }

        // Detect the change and clear out the internal caches

        UpdateListenerHub hub = registry.getService("UpdateListenerHub", UpdateListenerHub.class);

        hub.fireCheckForUpdates();

        // This will be the new version of the class

        named = (Named) createComponent(SYNTH_COMPONENT_CLASSNAME);

        assertEquals(named.getName(), "Updated");
    }

    @Test
    public void access_to_library_name_via_component_resources() throws Exception
    {
        Instantiator instantiator = source.getInstantiator(BASIC_COMPONENT_CLASSNAME);

        assertEquals(instantiator.getModel().getLibraryName(), "t5internal");
    }

    private void createSynthComponentClass(String name) throws Exception
    {
        ClassWriter cw = helper.createWriter(SYNTH_COMPONENT_CLASSNAME, BASIC_COMPONENT_CLASSNAME, Named.class.getName());

        helper.implementPublicConstructor(cw, BASIC_COMPONENT_CLASSNAME);

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getName", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(name);
        mv.visitInsn(ARETURN);
        mv.visitEnd();

        cw.visitEnd();

        helper.writeFile(cw, SYNTH_COMPONENT_CLASSNAME);
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
        helper = new ClassCreationHelper();

        File extraClasspath = new File(helper.tempDir);

        extraClasspath.mkdirs();

        URL url = extraClasspath.toURL();

        URLClassLoader extraLoader = new URLClassLoader(new URL[]
                {url}, Thread.currentThread().getContextClassLoader());

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
