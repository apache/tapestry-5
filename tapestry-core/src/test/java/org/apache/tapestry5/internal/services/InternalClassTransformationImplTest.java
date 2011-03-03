// Copyright 2006, 2007, 2008, 2010, 2011 The Apache Software Foundation
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

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Loader;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.tapestry5.annotations.Meta;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Retain;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.model.MutableComponentModelImpl;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.transform.InheritedAnnotation;
import org.apache.tapestry5.internal.transform.TestPackageAwareLoader;
import org.apache.tapestry5.internal.transform.pages.AbstractFoo;
import org.apache.tapestry5.internal.transform.pages.BarImpl;
import org.apache.tapestry5.internal.transform.pages.ChildClassInheritsAnnotation;
import org.apache.tapestry5.internal.transform.pages.ClaimedFields;
import org.apache.tapestry5.internal.transform.pages.EventHandlerTarget;
import org.apache.tapestry5.internal.transform.pages.FieldAccessBean;
import org.apache.tapestry5.internal.transform.pages.MethodAccessSubject;
import org.apache.tapestry5.internal.transform.pages.MethodIdentifier;
import org.apache.tapestry5.internal.transform.pages.ParentClass;
import org.apache.tapestry5.internal.transform.pages.ReadOnlyBean;
import org.apache.tapestry5.internal.transform.pages.TargetObject;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryClassPool;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry5.ioc.internal.services.CtClassSourceImpl;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentResourcesAware;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.MethodAccess;
import org.apache.tapestry5.services.MethodFilter;
import org.apache.tapestry5.services.MethodInvocationResult;
import org.apache.tapestry5.services.TransformField;
import org.apache.tapestry5.services.TransformMethod;
import org.apache.tapestry5.services.TransformMethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The tests share a number of resources, and so are run sequentially.
 */
@Test
public class InternalClassTransformationImplTest extends InternalBaseTestCase
{
    private static final String STRING_CLASS_NAME = "java.lang.String";

    private PropertyAccess access;

    private final ClassLoader contextClassLoader = currentThread().getContextClassLoader();

    private ClassFactory classFactory;

    private Loader loader;

    private ClassFactoryClassPool classFactoryClassPool;

    private CtClassSourceImpl classSource;

    @BeforeClass
    public void setup_access()
    {
        access = getService("PropertyAccess", PropertyAccess.class);
    }

    @AfterClass
    public void cleanup_access()
    {
        access = null;
    }

    /**
     * We need a new ClassPool for each individual test, since many of the tests will end up modifying one or more
     * CtClass instances.
     */
    @BeforeMethod
    public void setup_classpool()
    {
        ClassLoader threadDeadlockBuffer = new URLClassLoader(new URL[0], contextClassLoader);

        classFactoryClassPool = new ClassFactoryClassPool(threadDeadlockBuffer);

        loader = new TestPackageAwareLoader(threadDeadlockBuffer, classFactoryClassPool);

        // Inside Maven Surefire, the system classpath is not sufficient to find all
        // the necessary files.
        classFactoryClassPool.appendClassPath(new LoaderClassPath(loader));

        Logger logger = LoggerFactory.getLogger(InternalClassTransformationImplTest.class);

        classFactory = new ClassFactoryImpl(loader, classFactoryClassPool, logger);

        classSource = new CtClassSourceImpl(classFactoryClassPool, loader);
    }

    private Object transform(Class componentClass, ComponentClassTransformWorker worker) throws Exception
    {
        InternalComponentResources resources = mockInternalComponentResources();

        CtClass targetObjectCtClass = findCtClass(componentClass);

        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel(logger);

        replay();

        InternalClassTransformation ct = new InternalClassTransformationImpl(classFactory, targetObjectCtClass,
                new ComponentClassCacheImpl(classFactory, null), model, classSource, false);

        worker.transform(ct, model);

        ct.finish();

        Instantiator instantiator = ct.createInstantiator();

        Component instance = instantiator.newInstance(resources);

        verify();

        expect(resources.getComponent()).andReturn(instance).anyTimes();

        replay();

        // Return the instance for further testing

        return instance;
    }

    private CtClass findCtClass(Class targetClass) throws NotFoundException
    {
        return classFactoryClassPool.get(targetClass.getName());
    }

    private Class toClass(CtClass ctClass) throws Exception
    {
        return classFactoryClassPool.toClass(ctClass, loader, null);
    }

    @Test
    public void new_member_name() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        assertEquals(ct.newMemberName("fred"), "_$fred");
        assertEquals(ct.newMemberName("fred"), "_$fred_0");

        // Here we're exposing a bit of the internal algorithm, which strips
        // off '$' and '_' before tacking "_$" in front.

        assertEquals(ct.newMemberName("_fred"), "_$fred_1");
        assertEquals(ct.newMemberName("_$fred"), "_$fred_2");
        assertEquals(ct.newMemberName("__$___$____$_fred"), "_$fred_3");

        // Here we're trying to force conflicts with existing declared
        // fields and methods of the class.

        assertEquals(ct.newMemberName("_parentField"), "_$parentField");
        assertEquals(ct.newMemberName("conflictField"), "_$conflictField_0");
        assertEquals(ct.newMemberName("conflictMethod"), "_$conflictMethod_0");

        verify();
    }

    @Test
    public void new_member_name_with_prefix() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        assertEquals(ct.newMemberName("prefix", "fred"), "_$prefix_fred");
        assertEquals(ct.newMemberName("prefix", "fred"), "_$prefix_fred_0");

        // Here we're exposing a bit of the internal algorithm, which strips
        // off '$' and '_' before tacking "_$" in front.

        assertEquals(ct.newMemberName("prefix", "_fred"), "_$prefix_fred_1");
        assertEquals(ct.newMemberName("prefix", "_$fred"), "_$prefix_fred_2");
        assertEquals(ct.newMemberName("prefix", "__$___$____$_fred"), "_$prefix_fred_3");

        verify();
    }

    private InternalClassTransformation createClassTransformation(Class targetClass, Logger logger)
            throws NotFoundException
    {
        CtClass ctClass = findCtClass(targetClass);

        MutableComponentModel model = stubMutableComponentModel(logger);

        return new InternalClassTransformationImpl(classFactory, ctClass, null, model, null, false);
    }

    private MutableComponentModel stubMutableComponentModel(Logger logger)
    {
        return new MutableComponentModelImpl("unknown-class", logger, null, null);
    }

    @Test
    public void get_unknown_field() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        try
        {
            ct.getField("unknownField");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                    "Class org.apache.tapestry5.internal.transform.pages.ParentClass does not contain a field named 'unknownField'.");
        }

        verify();
    }

    @Test
    public void get_field_annotation() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        Retain retain = ct.getField("_annotatedField").getAnnotation(Retain.class);

        assertNotNull(retain);

        verify();
    }

    @Test
    public void field_does_not_contain_requested_annotation() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        // Field with annotation, but not that annotation
        assertNull(ct.getField("_annotatedField").getAnnotation(Override.class));

        // Field with no annotation
        assertNull(ct.getField("_parentField").getAnnotation(Override.class));

        verify();
    }

    @Test
    public void match_fields_with_annotation() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        List<TransformField> fields = ct.matchFieldsWithAnnotation(Retain.class);

        assertEquals(fields.size(), 1);
        assertEquals(fields.get(0).getName(), "_annotatedField");

        verify();
    }

    @Test
    public void get_field_modifiers() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(CheckFieldType.class, logger);

        assertEquals(ct.getFieldModifiers("_privateField"), Modifier.PRIVATE);
        assertEquals(ct.getFieldModifiers("_map"), Modifier.PRIVATE + Modifier.FINAL);
    }

    @Test
    public void get_field_exists() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(CheckFieldType.class, logger);

        assertTrue(ct.isField("_privateField"));
        assertFalse(ct.isField("_doesNotExist"));

        verify();
    }

    @Test
    public void no_fields_contain_requested_annotation() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        List<TransformField> fields = ct.matchFieldsWithAnnotation(Documented.class);

        assertTrue(fields.isEmpty());

        verify();
    }

    @Test
    public void claim_fields() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ClaimedFields.class, logger);

        List<String> unclaimed = ct.findUnclaimedFields();

        assertEquals(unclaimed, asList("_field1", "_field4", "_zzfield"));

        ct.getField("_field4").claim("Fred");

        unclaimed = ct.findUnclaimedFields();

        assertEquals(unclaimed, asList("_field1", "_zzfield"));

        try
        {
            ct.getField("_field4").claim("Barney");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Field _field4 of class org.apache.tapestry5.internal.transform.pages.ClaimedFields is already claimed by Fred and can not be claimed by Barney.");
        }

        verify();
    }

    @Test
    public void added_fields_are_not_listed_as_unclaimed_fields() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ClaimedFields.class, logger);

        ct.createField(Modifier.PRIVATE, "int", "newField");

        List<String> unclaimed = ct.findUnclaimedFields();

        assertEquals(unclaimed, asList("_field1", "_field4", "_zzfield"));

        verify();
    }

    @Test
    public void find_class_annotations() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        Meta meta = ct.getAnnotation(Meta.class);

        assertNotNull(meta);

        // Try again (the annotation will be cached). Use an annotation
        // that will not be present.

        Target t = ct.getAnnotation(Target.class);

        assertNull(t);

        verify();
    }

    /**
     * More a test of how Javassist works. Javassist does not honor the Inherited annotation for classes (this kind of
     * makes sense, since it won't necessarily have the super-class in memory).
     */
    @Test
    public void ensure_subclasses_inherit_parent_class_annotations() throws Exception
    {
        // The Java runtime does honor @Inherited
        assertNotNull(ChildClassInheritsAnnotation.class.getAnnotation(InheritedAnnotation.class));

        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ChildClassInheritsAnnotation.class, logger);

        InheritedAnnotation ia = ct.getAnnotation(InheritedAnnotation.class);

        // Javassist does not, but ClassTransformation patches around that.

        assertNotNull(ia);

        verify();
    }

    // TAPESTRY-2481
    @Test
    public void ensure_only_inherited_annotations_from_parent_class_are_visible() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ChildClassInheritsAnnotation.class, logger);

        Meta meta = ct.getAnnotation(Meta.class);

        assertNull(meta);

        verify();
    }

    /**
     * These tests are really to assert my understanding of Javassist's API. I guess we should keep them around to make
     * sure that future versions of Javassist work the same as our expectations.
     */
    @Test
    public void ensure_javassist_still_does_not_show_inherited_interfaces() throws Exception
    {
        CtClass ctClass = findCtClass(BarImpl.class);

        CtClass[] interfaces = ctClass.getInterfaces();

        // Just the interfaces implemented by this particular class, not
        // inherited interfaces.

        assertEquals(interfaces.length, 1);

        assertEquals(interfaces[0].getName(), BarInterface.class.getName());

        CtClass parentClass = ctClass.getSuperclass();

        interfaces = parentClass.getInterfaces();

        assertEquals(interfaces.length, 1);

        assertEquals(interfaces[0].getName(), FooInterface.class.getName());
    }

    @Test
    public void ensure_javassist_does_not_show_interface_methods_on_abstract_class() throws Exception
    {
        CtClass ctClass = findCtClass(AbstractFoo.class);

        CtClass[] interfaces = ctClass.getInterfaces();

        assertEquals(interfaces.length, 1);

        assertEquals(interfaces[0].getName(), FooInterface.class.getName());

        // In some cases, Java reflection on an abstract class implementing an interface
        // will show the interface methods as abstract methods on the class. This seems
        // to vary from JVM to JVM. I believe Javassist is more consistent here.

        CtMethod[] methods = ctClass.getDeclaredMethods();

        assertEquals(methods.length, 0);
    }

    @Test
    public void ensure_javassist_does_not_show_extended_interface_methods_on_interface() throws Exception
    {
        CtClass ctClass = findCtClass(FooBarInterface.class);

        // Just want to check that an interface that extends other interfaces
        // doesn't show those other interface's methods.

        CtMethod[] methods = ctClass.getDeclaredMethods();

        assertEquals(methods.length, 0);
    }

    public static final TransformMethodSignature RUN = new TransformMethodSignature("run");

    @Test
    public void access_to_protected_void_no_args_method() throws Exception
    {
        Object instance = transform(MethodAccessSubject.class, new ComponentClassTransformWorker()
        {
            public void transform(ClassTransformation transformation, MutableComponentModel model)
            {
                transformation.addImplementedInterface(Runnable.class);

                TransformMethodSignature targetMethodSignature = new TransformMethodSignature(Modifier.PROTECTED,
                        "void", "protectedVoidNoArgs", null, null);
                TransformMethod pvna = transformation.getOrCreateMethod(targetMethodSignature);

                final MethodAccess pvnaAccess = pvna.getAccess();

                transformation.getOrCreateMethod(RUN).addAdvice(new ComponentMethodAdvice()
                {
                    public void advise(ComponentMethodInvocation invocation)
                    {
                        invocation.proceed();

                        MethodInvocationResult invocationResult = pvnaAccess.invoke(invocation.getInstance());

                        assertFalse(invocationResult.isFail(), "fail should be false, no checked exception thrown");
                    }
                });
            }
        });

        Runnable r = (Runnable) instance;

        r.run();

        assertEquals(access.get(r, "marker"), "protectedVoidNoArgs");
    }

    @Test
    public void access_to_public_void_throws_exception() throws Exception
    {
        Object instance = transform(MethodAccessSubject.class, new ComponentClassTransformWorker()
        {
            public void transform(ClassTransformation transformation, MutableComponentModel model)
            {
                transformation.addImplementedInterface(Runnable.class);

                TransformMethodSignature targetMethodSignature = new TransformMethodSignature(Modifier.PUBLIC, "void",
                        "publicVoidThrowsException", null, new String[]
                        { SQLException.class.getName() });
                TransformMethod targetMethod = transformation.getOrCreateMethod(targetMethodSignature);

                final MethodAccess targetAccess = targetMethod.getAccess();

                transformation.getOrCreateMethod(RUN).addAdvice(new ComponentMethodAdvice()
                {
                    public void advise(ComponentMethodInvocation invocation)
                    {
                        invocation.proceed();

                        MethodInvocationResult invocationResult = targetAccess.invoke(invocation.getInstance());

                        assertTrue(invocationResult.isFail(), "fail should be true; checked exception thrown");

                        SQLException ex = invocationResult.getThrown(SQLException.class);

                        assertNotNull(ex);
                        assertEquals(ex.getMessage(), "From publicVoidThrowsException()");
                    }
                });
            }
        });

        Runnable r = (Runnable) instance;

        r.run();

        assertEquals(access.get(r, "marker"), "publicVoidThrowsException");
    }

    public interface ProcessInteger
    {
        int operate(int input);
    }

    @Test
    public void access_to_public_method_with_argument_and_return_value() throws Exception
    {
        Object instance = transform(MethodAccessSubject.class, new ComponentClassTransformWorker()
        {
            public void transform(ClassTransformation transformation, MutableComponentModel model)
            {
                transformation.addImplementedInterface(ProcessInteger.class);

                TransformMethod incrementer = transformation.getOrCreateMethod(new TransformMethodSignature(
                        Modifier.PUBLIC, "int", "incrementer", new String[]
                        { "int" }, null));

                final MethodAccess incrementerAccess = incrementer.getAccess();

                TransformMethodSignature operateSig = new TransformMethodSignature(Modifier.PUBLIC, "int", "operate",
                        new String[]
                        { "int" }, null);

                TransformMethod operate = transformation.getOrCreateMethod(operateSig);

                operate.addAdvice(new ComponentMethodAdvice()
                {
                    public void advise(ComponentMethodInvocation invocation)
                    {
                        // This advice *replaces* the original do-nothing method, because
                        // it never calls invocation.proceed().

                        // This kind of advice always needs some special knowledge of
                        // the parameters to the original method, so that they can be mapped
                        // to some other method (including a MethodAccess).

                        Integer parameter = (Integer) invocation.getParameter(0);

                        MethodInvocationResult result = incrementerAccess.invoke(invocation.getInstance(), parameter);

                        invocation.overrideResult(result.getReturnValue());
                    }
                });
            }
        });

        ProcessInteger pi = (ProcessInteger) instance;

        assertEquals(pi.operate(99), 100);

        assertEquals(access.get(instance, "marker"), "incrementer(99)");
    }

    public interface ProcessStringAndInteger
    {
        String process(String input, int value);
    }

    @Test
    public void access_to_private_method() throws Exception
    {
        Object instance = transform(MethodAccessSubject.class, new ComponentClassTransformWorker()
        {
            public void transform(ClassTransformation transformation, MutableComponentModel model)
            {
                transformation.addImplementedInterface(ProcessStringAndInteger.class);

                TransformMethod targetMethod = transformation.getOrCreateMethod(new TransformMethodSignature(
                        Modifier.PRIVATE, "java.lang.String", "privateMethod", new String[]
                        { "java.lang.String", "int" }, null));

                final MethodAccess targetMethodAccess = targetMethod.getAccess();

                TransformMethodSignature processSig = new TransformMethodSignature(Modifier.PUBLIC, "java.lang.String",
                        "process", new String[]
                        { "java.lang.String", "int" }, null);

                TransformMethod process = transformation.getOrCreateMethod(processSig);

                process.addAdvice(new ComponentMethodAdvice()
                {
                    public void advise(ComponentMethodInvocation invocation)
                    {
                        // Don't even bother with proceed() this time, which is OK (but
                        // somewhat rare).

                        MethodInvocationResult result = targetMethodAccess.invoke(invocation.getInstance(),
                                invocation.getParameter(0), invocation.getParameter(1));

                        invocation.overrideResult(result.getReturnValue());
                    }
                });
            }
        });

        ProcessStringAndInteger p = (ProcessStringAndInteger) instance;

        assertEquals(p.process("Tapestry!", 2), "Tapestry!Tapestry!");

        assertEquals(access.get(instance, "marker"), "privateMethod");
    }

    @Test
    public void add_injected_field() throws Exception
    {
        InternalComponentResources resources = mockInternalComponentResources();

        CtClass targetObjectCtClass = findCtClass(TargetObject.class);

        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel(logger);

        replay();

        InternalClassTransformation ct = new InternalClassTransformationImpl(classFactory, targetObjectCtClass, null,
                model, null, false);

        // Default behavior is to add an injected field for the InternalComponentResources object,
        // so we'll just check that.

        ct.finish();

        Instantiator instantiator = ct.createInstantiator();

        ComponentResourcesAware instance = instantiator.newInstance(resources);

        assertSame(instance.getComponentResources(), resources);

        verify();
    }

    @Test
    public void make_field_read_only() throws Exception
    {
        InternalComponentResources resources = mockInternalComponentResources();

        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel(logger);

        replay();

        CtClass targetObjectCtClass = findCtClass(ReadOnlyBean.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(classFactory, targetObjectCtClass, null,
                model, null, false);

        ct.getField("_value").inject("Read-Only Value");

        ct.finish();

        Object target = instantiate(ReadOnlyBean.class, ct, resources);

        try
        {
            access.set(target, "value", "anything");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            // The PropertyAccess layer adds a wrapper exception around the real one.

            assertEquals(ex.getCause().getMessage(),
                    "Field org.apache.tapestry5.internal.transform.pages.ReadOnlyBean._value is read-only.");
        }

        verify();
    }

    @Test
    public void inject_field() throws Exception
    {
        InternalComponentResources resources = mockInternalComponentResources();

        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel(logger);

        replay();

        CtClass targetObjectCtClass = findCtClass(ReadOnlyBean.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(classFactory, targetObjectCtClass, null,
                model, null, false);

        ct.getField("_value").inject("Tapestry");

        ct.finish();

        Object target = instantiate(ReadOnlyBean.class, ct, resources);

        assertEquals(access.get(target, "value"), "Tapestry");

        try
        {
            access.set(target, "value", "anything");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            // The PropertyAccess layer adds a wrapper exception around the real one.

            assertEquals(ex.getCause().getMessage(),
                    "Field org.apache.tapestry5.internal.transform.pages.ReadOnlyBean._value is read-only.");
        }

        verify();
    }

    /**
     * Tests the basic functionality of overriding read and write; also tests the case for multiple field read/field
     * write substitions.
     */
    @Test
    public void override_field_read_and_write() throws Exception
    {
        InternalComponentResources resources = mockInternalComponentResources();

        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel(logger);

        replay();

        CtClass targetObjectCtClass = findCtClass(FieldAccessBean.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(classFactory, targetObjectCtClass, null,
                model, null, false);

        replaceAccessToField(ct, "foo");
        replaceAccessToField(ct, "bar");

        // Stuff ...

        ct.finish();

        Object target = instantiate(FieldAccessBean.class, ct, resources);

        // target is no longer assignable to FieldAccessBean; its a new class from a new class
        // loader. So we use reflective access, which doesn't care about such things.

        checkReplacedFieldAccess(target, "foo");
        checkReplacedFieldAccess(target, "bar");

        verify();
    }

    private void checkReplacedFieldAccess(Object target, String propertyName)
    {

        try
        {
            access.get(target, propertyName);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            // PropertyAccess adds a wrapper exception
            assertEquals(ex.getCause().getMessage(), "read " + propertyName);
        }

        try
        {
            access.set(target, propertyName, "new value");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            // PropertyAccess adds a wrapper exception
            assertEquals(ex.getCause().getMessage(), "write " + propertyName);
        }
    }

    private void replaceAccessToField(InternalClassTransformation ct, String baseName)
    {
        String fieldName = "_" + baseName;
        String readMethodName = "_read_" + baseName;

        TransformMethodSignature readMethodSignature = new TransformMethodSignature(Modifier.PRIVATE,
                STRING_CLASS_NAME, readMethodName, null, null);

        ct.addNewMethod(readMethodSignature, String.format("throw new RuntimeException(\"read %s\");", baseName));

        ct.replaceReadAccess(fieldName, readMethodName);

        String writeMethodName = "_write_" + baseName;

        TransformMethodSignature writeMethodSignature = new TransformMethodSignature(Modifier.PRIVATE, "void",
                writeMethodName, new String[]
                { STRING_CLASS_NAME }, null);
        ct.addNewMethod(writeMethodSignature, String.format("throw new RuntimeException(\"write %s\");", baseName));

        ct.replaceWriteAccess(fieldName, writeMethodName);
    }

    @Test
    public void match_methods_with_annotation() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(AnnotatedPage.class, logger);

        List<TransformMethod> l = ct.matchMethodsWithAnnotation(SetupRender.class);

        // Check order

        assertEquals(l.size(), 2);
        assertEquals(l.get(0).getSignature().toString(), "void beforeRender()");
        assertEquals(l.get(1).getSignature().toString(), "boolean earlyRender(org.apache.tapestry5.MarkupWriter)");

        // Check up on cacheing

        assertEquals(ct.matchMethodsWithAnnotation(SetupRender.class), l);

        // Check up on no match.

        assertTrue(ct.matchMethodsWithAnnotation(Deprecated.class).isEmpty());

        verify();
    }

    @Test
    public void match_methods_using_predicate() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        final ClassTransformation ct = createClassTransformation(AnnotatedPage.class, logger);

        // Duplicates, somewhat less efficiently, the logic in find_methods_with_annotation().

        Predicate<TransformMethod> predicate = new Predicate<TransformMethod>()
        {
            public boolean accept(TransformMethod element)
            {
                return element.getAnnotation(SetupRender.class) != null;
            }
        };

        List<TransformMethod> l = ct.matchMethods(predicate);

        // Check order

        assertEquals(l.size(), 2);
        assertEquals(l.get(0).getSignature().toString(), "void beforeRender()");
        assertEquals(l.get(1).getSignature().toString(), "boolean earlyRender(org.apache.tapestry5.MarkupWriter)");

        // Check up on cacheing

        assertEquals(ct.matchMethodsWithAnnotation(SetupRender.class), l);

        // Check up on no match.

        assertTrue(ct.matchMethodsWithAnnotation(Deprecated.class).isEmpty());

        verify();
    }

    @Test
    public void to_class_with_primitive_type() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(AnnotatedPage.class, logger);

        assertSame(ct.toClass("float"), Float.class);

        verify();
    }

    @Test
    public void to_class_with_object_type() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(AnnotatedPage.class, logger);

        assertSame(ct.toClass("java.util.Map"), Map.class);

        verify();
    }

    @Test
    public void non_private_fields_are_an_exception() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        try
        {

            InternalClassTransformation ct = createClassTransformation(VisibilityBean.class, logger);

            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertMessageContains(ex, "Class " + VisibilityBean.class.getName() + " contains field(s)",
                    "_$myPackagePrivate", "_$myProtected", "_$myPublic");
        }

        verify();
    }

    @Test
    public void find_annotation_in_method() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(EventHandlerTarget.class, logger);

        OnEvent annotation = ct.getOrCreateMethod(new TransformMethodSignature("handler")).getAnnotation(OnEvent.class);

        // Check that the attributes of the annotation match the expectation.

        assertEquals(annotation.value(), "fred");
        assertEquals(annotation.component(), "alpha");

        verify();
    }

    private Component instantiate(Class<?> expectedClass, InternalClassTransformation ct,
            InternalComponentResources resources) throws Exception
    {
        Instantiator ins = ct.createInstantiator();

        return ins.newInstance(resources);
    }

    @Test
    public void get_method_identifier() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(MethodIdentifier.class, logger);

        List<TransformMethod> methods = ct.matchMethodsWithAnnotation(OnEvent.class);

        assertEquals(methods.size(), 1);

        assertEquals(
                methods.get(0).getMethodIdentifier(),
                "org.apache.tapestry5.internal.transform.pages.MethodIdentifier.makeWaves(java.lang.String, int[]) (at MethodIdentifier.java:24)");

        verify();
    }

    @Test
    public void base_class_methods_are_never_overridden() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(SimpleBean.class, logger);

        List<TransformMethod> methods = ct.matchMethods(F.<TransformMethod> notNull());

        assertFalse(methods.isEmpty());

        for (TransformMethod method : methods)
        {
            assertFalse(method.isOverride());
        }

        verify();
    }

    @Test
    public void check_for_overridden_methods() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        InternalClassTransformation parentTransform = createClassTransformation(SimpleBean.class, logger);

        parentTransform.finish();

        CtClass childClass = findCtClass(SimpleBeanSubclass.class);

        ClassTransformation childTransform = parentTransform.createChildTransformation(childClass,
                stubMutableComponentModel(logger));

        assertFalse(childTransform.getOrCreateMethod(new TransformMethodSignature("notOverridden")).isOverride());

        assertTrue(childTransform.getOrCreateMethod(
                new TransformMethodSignature(Modifier.PUBLIC, "void", "setAge", new String[]
                { "int" }, null)).isOverride());
    }

}
