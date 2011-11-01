// Copyright 2010, 2011 The Apache Software Foundation
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

import com.example.*;
import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;
import org.apache.tapestry5.internal.plastic.asm.ClassWriter;
import org.apache.tapestry5.internal.plastic.asm.MethodVisitor;
import org.apache.tapestry5.ioc.test.IOCTestCase;
import org.apache.tapestry5.services.UpdateListenerHub;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;

import static org.apache.tapestry5.internal.plastic.asm.Opcodes.*;

/**
 * Test the ability to perform live class reloading of a service implementation.
 */
@SuppressWarnings("unchecked")
public class ReloadTest extends IOCTestCase
{
    private static final String PACKAGE = "com.example";

    private static final String CLASS = PACKAGE + ".ReloadableServiceImpl";

    private static final String BASE_CLASS = PACKAGE + ".BaseReloadableServiceImpl";

    private File classesDir;

    private ClassLoader classLoader;

    public static boolean eagerLoadServiceWasInstantiated;

    private File classFile;

    @BeforeClass
    public void setup() throws Exception
    {
        String uid = Long.toHexString(System.currentTimeMillis());

        classesDir = new File(System.getProperty("java.io.tmpdir"), uid);

        // URLClassLoader REQUIRES that File URLs end with a slash! That's a half hour of my life gone!

        URL classesURL = new URL("file:" + classesDir.getCanonicalPath() + "/");

        System.out.println("Reload classes dir: " + classesURL);

        classLoader = new URLClassLoader(new URL[]
                {classesURL}, Thread.currentThread().getContextClassLoader());

        classFile = new File(classesDir, "com/example/ReloadableServiceImpl.class");
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

        touch(classFile);

        createImplementationClass("updated");

        // Doesn't take effect until after the update check

        assertEquals(reloadable.getStatus(), "initial");

        fireUpdateCheck(registry);

        assertEquals(reloadable.getStatus(), "updated");

        registry.shutdown();
    }

    @Test
    public void reload_a_base_class() throws Exception
    {
        String baseClassInternalName = PlasticInternalUtils.toInternalName(BASE_CLASS);
        String internalName = PlasticInternalUtils.toInternalName(CLASS);

        createImplementationClass(BASE_CLASS, "initial from base");


        ClassWriter cw = createClassWriter(internalName, baseClassInternalName, ACC_PUBLIC);

        // Add default constructor

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, baseClassInternalName, "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        writeBytecode(cw, internalName);

        Registry registry = createRegistry();

        ReloadableService reloadable = registry.getService(ReloadableService.class);

        fireUpdateCheck(registry);

        assertEquals(reloadable.getStatus(), "initial from base");

        touch(new File(pathForInternalName(baseClassInternalName)));

        createImplementationClass(BASE_CLASS, "updated from base");

        fireUpdateCheck(registry);

        assertEquals(reloadable.getStatus(), "updated from base");

        registry.shutdown();

    }

    @Test
    public void delete_class() throws Exception
    {
        createImplementationClass("before delete");

        Registry registry = createRegistry();

        ReloadableService reloadable = registry.getService(ReloadableService.class);

        assertEquals(reloadable.getStatus(), "before delete");

        assertTrue(classFile.exists(), "The class file must exist.");

        classFile.delete();

        fireUpdateCheck(registry);

        try
        {
            reloadable.getStatus();
            unreachable();
        } catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Unable to reload", CLASS);
        }

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

        touch(classFile);

        createImplementationClass("updated proxy");

        fireUpdateCheck(registry);

        assertEquals(reloadable.getStatus(), "updated proxy");

        touch(classFile);

        createImplementationClass("re-updated proxy");

        fireUpdateCheck(registry);

        assertEquals(reloadable.getStatus(), "re-updated proxy");

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

        touch(classFile);

        createInvalidImplentationClass();

        fireUpdateCheck(registry);

        try
        {
            reloadable.getStatus();

            unreachable();
        } catch (Exception ex)
        {
            assertEquals(ex.getMessage(),
                    "Service implementation class com.example.ReloadableServiceImpl does not have a suitable public constructor.");
        }

        registry.shutdown();
    }

    private void createImplementationClass(String status) throws Exception
    {
        createImplementationClass(CLASS, status);
    }

    private void createImplementationClass(String className, String status) throws Exception
    {
        String internalName = PlasticInternalUtils.toInternalName(className);

        ClassWriter cw = createClassWriter(internalName, "java/lang/Object", ACC_PUBLIC);

        // Add default constructor

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();


        mv = cw.visitMethod(ACC_PUBLIC, "getStatus", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(status);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        writeBytecode(cw, internalName);
    }

    private ClassWriter createClassWriter(String internalName, String baseClassInternalName, int classModifiers)
    {
        ClassWriter cw = new ClassWriter(0);

        cw.visit(V1_5, classModifiers, internalName, null,
                baseClassInternalName, new String[]{
                PlasticInternalUtils.toInternalName(ReloadableService.class.getName())
        });

        return cw;
    }

    private void writeBytecode(ClassWriter cw, String internalName) throws Exception
    {
        byte[] bytecode = cw.toByteArray();

        writeBytecode(bytecode, pathForInternalName(internalName));
    }

    private String pathForInternalName(String internalName)
    {
        return String.format("%s/%s.class",
                classesDir.getAbsolutePath(),
                internalName);
    }

    private void writeBytecode(byte[] bytecode, String path) throws Exception
    {
        File file = new File(path);

        file.getParentFile().mkdirs();

        OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));

        stream.write(bytecode);

        stream.close();
    }

    private void createInvalidImplentationClass() throws Exception
    {
        String internalName = PlasticInternalUtils.toInternalName(CLASS);

        ClassWriter cw = createClassWriter(internalName, "java/lang/Object", ACC_PUBLIC);

        // Add default constructor

        MethodVisitor mv = cw.visitMethod(ACC_PROTECTED, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Notice the  class is abstract, so no implementation.

        cw.visitEnd();

        writeBytecode(cw, internalName);
    }

    @Test
    public void eager_load_service_with_proxy()
    {
        eagerLoadServiceWasInstantiated = false;

        Registry r = new RegistryBuilder().add(EagerProxyReloadModule.class).build();

        r.performRegistryStartup();

        assertTrue(eagerLoadServiceWasInstantiated);
    }

    @Test
    public void reload_aware() throws Exception
    {
        Registry r = buildRegistry(ReloadAwareModule.class);

        assertEquals(ReloadAwareModule.counterInstantiations, 0);
        assertEquals(ReloadAwareModule.counterReloads, 0);

        Counter counter = r.proxy(Counter.class, CounterImpl.class);

        assertEquals(ReloadAwareModule.counterInstantiations, 0);

        assertEquals(counter.increment(), 1);
        assertEquals(counter.increment(), 2);

        assertEquals(ReloadAwareModule.counterInstantiations, 1);

        URL classURL = CounterImpl.class.getResource("CounterImpl.class");

        File classFile = new File(classURL.toURI());

        touch(classFile);

        assertEquals(ReloadAwareModule.counterInstantiations, 1);
        assertEquals(ReloadAwareModule.counterReloads, 0);

        fireUpdateCheck(r);

        assertEquals(ReloadAwareModule.counterInstantiations, 2);
        assertEquals(ReloadAwareModule.counterReloads, 1);

        // Check that internal state has reset

        assertEquals(counter.increment(), 1);

        r.shutdown();
    }
}
