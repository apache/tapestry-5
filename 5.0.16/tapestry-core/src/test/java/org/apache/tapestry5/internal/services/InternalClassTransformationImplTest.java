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

package org.apache.tapestry5.internal.services;

import javassist.*;
import org.apache.tapestry5.annotations.Meta;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Retain;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.model.MutableComponentModelImpl;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.transform.FieldRemoval;
import org.apache.tapestry5.internal.transform.InheritedAnnotation;
import org.apache.tapestry5.internal.transform.TestPackageAwareLoader;
import org.apache.tapestry5.internal.transform.pages.*;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryClassPool;
import org.apache.tapestry5.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentResourcesAware;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.MethodFilter;
import org.apache.tapestry5.services.TransformMethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.lang.Thread.currentThread;
import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;

/**
 * The tests share a number of resources, and so are run sequentially.
 */
@Test(sequential = true)
public class InternalClassTransformationImplTest extends InternalBaseTestCase
{
    private static final String STRING_CLASS_NAME = "java.lang.String";

    private PropertyAccess access;

    private final ClassLoader contextClassLoader = currentThread().getContextClassLoader();

    private ClassFactory classFactory;

    private Loader loader;

    private ClassFactoryClassPool classFactoryClassPool;

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
        //  _classPool = new ClassPool();

        classFactoryClassPool = new ClassFactoryClassPool(contextClassLoader);

        loader = new TestPackageAwareLoader(contextClassLoader, classFactoryClassPool);

        // Inside Maven Surefire, the system classpath is not sufficient to find all
        // the necessary files.
        classFactoryClassPool.appendClassPath(new LoaderClassPath(loader));

        Logger logger = LoggerFactory.getLogger(InternalClassTransformationImplTest.class);

        classFactory = new ClassFactoryImpl(loader, classFactoryClassPool, logger);
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

        return new InternalClassTransformationImpl(classFactory, ctClass, null, model, null);
    }

    private MutableComponentModel stubMutableComponentModel(Logger logger)
    {
        return new MutableComponentModelImpl("unknown-class", logger, null, null);
    }

    @Test
    public void find_annotation_on_unknown_field() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        try
        {
            ct.getFieldAnnotation("unknownField", Retain.class);
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
    public void find_field_annotation() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        Retain retain = ct.getFieldAnnotation("_annotatedField", Retain.class);

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
        assertNull(ct.getFieldAnnotation("_annotatedField", Override.class));

        // Field with no annotation
        assertNull(ct.getFieldAnnotation("_parentField", Override.class));

        verify();
    }

    @Test
    public void find_fields_with_annotation() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        List<String> fields = ct.findFieldsWithAnnotation(Retain.class);

        assertEquals(fields.size(), 1);
        assertEquals(fields.get(0), "_annotatedField");

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

        List<String> fields = ct.findFieldsWithAnnotation(Documented.class);

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

        ct.claimField("_field4", "Fred");

        unclaimed = ct.findUnclaimedFields();

        assertEquals(unclaimed, asList("_field1", "_zzfield"));

        try
        {
            ct.claimField("_field4", "Barney");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
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

        ct.addField(Modifier.PRIVATE, "int", "newField");

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

    @Test
    public void add_injected_field() throws Exception
    {
        InternalComponentResources resources = mockInternalComponentResources();

        CtClass targetObjectCtClass = findCtClass(TargetObject.class);

        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel(logger);

        replay();

        InternalClassTransformation ct = new InternalClassTransformationImpl(classFactory, targetObjectCtClass, null,
                                                                             model, null);

        // Default behavior is to add an injected field for the InternalComponentResources object,
        // so we'll just check that.

        ct.finish();

        Instantiator instantiator = ct.createInstantiator();

        ComponentResourcesAware instance = instantiator.newInstance(resources);

        assertSame(instance.getComponentResources(), resources);

        verify();
    }

    @Test
    public void add_injected_field_from_parent_transformation() throws Exception
    {
        final String value = "from the parent";

        InternalComponentResources resources = mockInternalComponentResources();

        CtClass targetObjectCtClass = findCtClass(TargetObject.class);

        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel();

        train_getLogger(model, logger);

        replay();

        InternalClassTransformation ct = new InternalClassTransformationImpl(classFactory, targetObjectCtClass, null,
                                                                             model, null);

        String parentFieldName = ct.addInjectedField(String.class, "_value", value);

        // Default behavior is to add an injected field for the InternalComponentResources object,
        // so we'll just check that.

        ct.finish();

        // Now lets work on the subclass

        CtClass subclassCtClass = findCtClass(TargetObjectSubclass.class);

        ct = ct.createChildTransformation(subclassCtClass, model);

        String subclassFieldName = ct.addInjectedField(String.class, "_childValue", value);

        // This is what proves it is cached.

        assertEquals(subclassFieldName, parentFieldName);

        // This proves the the field is protected and can be used in subclasses.

        ct.addMethod(new TransformMethodSignature(Modifier.PUBLIC, "java.lang.String", "getValue", null, null),
                     "return " + subclassFieldName + ";");

        ct.finish();

        Instantiator instantiator = ct.createInstantiator();

        Object instance = instantiator.newInstance(resources);

        Object actual = access.get(instance, "value");

        assertSame(actual, value);

        verify();
    }

    @Test
    public void add_interface_to_class() throws Exception
    {
        InternalComponentResources resources = mockInternalComponentResources();

        CtClass targetObjectCtClass = findCtClass(TargetObject.class);

        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel(logger);

        replay();

        InternalClassTransformation ct = new InternalClassTransformationImpl(classFactory, targetObjectCtClass, null,
                                                                             model, null);

        ct.addImplementedInterface(FooInterface.class);
        ct.addImplementedInterface(GetterMethodsInterface.class);

        ct.finish();

        Class transformed = toClass(targetObjectCtClass);

        Class[] interfaces = transformed.getInterfaces();

        assertEquals(interfaces, new Class[] { Component.class, FooInterface.class, GetterMethodsInterface.class });

        Object target = ct.createInstantiator().newInstance(resources);

        FooInterface asFoo = (FooInterface) target;

        asFoo.foo();

        GetterMethodsInterface getters = (GetterMethodsInterface) target;

        assertEquals(getters.getBoolean(), false);
        assertEquals(getters.getByte(), (byte) 0);
        assertEquals(getters.getShort(), (short) 0);
        assertEquals(getters.getInt(), 0);
        assertEquals(getters.getLong(), 0l);
        assertEquals(getters.getFloat(), 0.0f);
        assertEquals(getters.getDouble(), 0.0d);
        assertNull(getters.getString());
        assertNull(getters.getObjectArray());
        assertNull(getters.getIntArray());

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
                                                                             model, null);

        ct.makeReadOnly("_value");

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
    public void removed_fields_should_not_show_up_as_unclaimed() throws Exception
    {
        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel(logger);

        replay();

        CtClass targetObjectCtClass = findCtClass(RemoveFieldBean.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(null, targetObjectCtClass, null, model,
                                                                             null);

        ct.removeField("_barney");

        assertEquals(ct.findUnclaimedFields(), asList("_fred"));

        verify();
    }

    @Test
    public void add_to_constructor() throws Exception
    {
        InternalComponentResources resources = mockInternalComponentResources();

        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel(logger);

        replay();

        CtClass targetObjectCtClass = findCtClass(ReadOnlyBean.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(classFactory, targetObjectCtClass, null,
                                                                             model, null);

        ct.extendConstructor("_value = \"from constructor\";");

        ct.finish();

        Object target = instantiate(ReadOnlyBean.class, ct, resources);

        assertEquals(access.get(target, "value"), "from constructor");

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
                                                                             model, null);

        ct.injectField("_value", "Tapestry");

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
                                                                             model, null);

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

        TransformMethodSignature readMethodSignature = new TransformMethodSignature(Modifier.PRIVATE, STRING_CLASS_NAME,
                                                                                    readMethodName, null, null);

        ct.addMethod(readMethodSignature, String.format("throw new RuntimeException(\"read %s\");", baseName));

        ct.replaceReadAccess(fieldName, readMethodName);

        String writeMethodName = "_write_" + baseName;

        TransformMethodSignature writeMethodSignature = new TransformMethodSignature(Modifier.PRIVATE, "void",
                                                                                     writeMethodName,
                                                                                     new String[] { STRING_CLASS_NAME },
                                                                                     null);
        ct.addMethod(writeMethodSignature, String.format("throw new RuntimeException(\"write %s\");", baseName));

        ct.replaceWriteAccess(fieldName, writeMethodName);
    }

    @Test
    public void find_methods_with_annotation() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(AnnotatedPage.class, logger);

        List<TransformMethodSignature> l = ct.findMethodsWithAnnotation(SetupRender.class);

        // Check order

        assertEquals(l.size(), 2);
        assertEquals(l.get(0).toString(), "void beforeRender()");
        assertEquals(l.get(1).toString(), "boolean earlyRender(org.apache.tapestry5.MarkupWriter)");

        // Check up on cacheing

        assertEquals(ct.findMethodsWithAnnotation(SetupRender.class), l);

        // Check up on no match.

        assertTrue(ct.findFieldsWithAnnotation(Deprecated.class).isEmpty());

        verify();
    }

    @Test
    public void find_methods_using_filter() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        final ClassTransformation ct = createClassTransformation(AnnotatedPage.class, logger);

        // Duplicates, somewhat less efficiently, the logic in find_methods_with_annotation().

        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(TransformMethodSignature signature)
            {
                return ct.getMethodAnnotation(signature, SetupRender.class) != null;
            }
        };

        List<TransformMethodSignature> l = ct.findMethods(filter);

        // Check order

        assertEquals(l.size(), 2);
        assertEquals(l.get(0).toString(), "void beforeRender()");
        assertEquals(l.get(1).toString(), "boolean earlyRender(org.apache.tapestry5.MarkupWriter)");

        // Check up on cacheing

        assertEquals(ct.findMethodsWithAnnotation(SetupRender.class), l);

        // Check up on no match.

        assertTrue(ct.findFieldsWithAnnotation(Deprecated.class).isEmpty());

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

        OnEvent annotation = ct.getMethodAnnotation(new TransformMethodSignature("handler"), OnEvent.class);

        // Check that the attributes of the annotation match the expectation.

        assertEquals(annotation.value(), "fred");
        assertEquals(annotation.component(), "alpha");

        verify();
    }

    @Test
    public void find_annotation_in_unknown_method() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        try
        {
            ct.getMethodAnnotation(new TransformMethodSignature("foo"), OnEvent.class);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Class org.apache.tapestry5.internal.transform.pages.ParentClass does not declare method 'public void foo()'.");
        }

        verify();
    }

    @Test
    public void prefix_method() throws Exception
    {
        Logger logger = mockLogger();
        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "int", "getParentField", null,
                                                                    null);

        replay();

        InternalClassTransformation ct = createClassTransformation(ParentClass.class, logger);
        ct.prefixMethod(sig, "return 42;");

        String desc = ct.toString();
        assertTrue(desc.contains("prefix"));
        assertTrue(desc.contains("getParentField"));

        // fail if frozen
        ct.finish();
        try
        {
            ct.prefixMethod(sig, "return 0;");
            unreachable();
        }
        catch (IllegalStateException e)
        {
        }


        verify();
    }

    @Test
    public void fields_in_prefixed_methods_are_transformed() throws Exception
    {
        Logger logger = mockLogger();
        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "int", "getTargetValue", null,
                                                                    null);
        Runnable runnable = mockRunnable();

        runnable.run();

        replay();

        InternalClassTransformation ct = createClassTransformation(MethodPrefixTarget.class, logger);

        String name = ct.addInjectedField(Runnable.class, "runnable", runnable);

        // Transform the field.

        TransformMethodSignature reader = new TransformMethodSignature(Modifier.PRIVATE, "int", "read_target_value",
                                                                       null, null);

        ct.addMethod(reader, "return 66;");

        ct.replaceReadAccess("_targetField", "read_target_value");

        ct.prefixMethod(sig, name + ".run();");

        ct.finish();

        Object target = instantiate(MethodPrefixTarget.class, ct, null);

        // 66 reflects the change to the field.

        assertEquals(access.get(target, "targetValue"), 66);

        verify();
    }

    private Component instantiate(Class<?> expectedClass, InternalClassTransformation ct,
                                  InternalComponentResources resources) throws Exception
    {
        Instantiator ins = ct.createInstantiator();

        return ins.newInstance(resources);
    }

    @Test
    public void extend_existing_method_fields_are_transformed() throws Exception
    {
        Logger logger = mockLogger();
        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "int", "getTargetValue", null,
                                                                    null);
        Runnable runnable = mockRunnable();

        runnable.run();

        replay();

        InternalClassTransformation ct = createClassTransformation(MethodPrefixTarget.class, logger);

        String name = ct.addInjectedField(Runnable.class, "runnable", runnable);

        // Transform the field.

        TransformMethodSignature reader = new TransformMethodSignature(Modifier.PRIVATE, "int", "read_target_value",
                                                                       null, null);

        ct.addMethod(reader, "return 66;");

        ct.replaceReadAccess("_targetField", "read_target_value");

        BodyBuilder builder = new BodyBuilder();
        builder.begin();
        builder.addln("%s.run();", name);
        builder.addln("return $_ + 1;");
        builder.end();

        ct.extendExistingMethod(sig, builder.toString());

        ct.finish();

        Object target = instantiate(MethodPrefixTarget.class, ct, null);

        // 66 reflects the change to the field, +1 reflects the extension of the method.

        assertEquals(access.get(target, "targetValue"), 67);

        verify();
    }

    @Test
    public void invalid_code() throws Exception
    {
        Logger logger = mockLogger();
        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PUBLIC, "int", "getParentField", null,
                                                                    null);

        replay();

        InternalClassTransformation ct = createClassTransformation(ParentClass.class, logger);

        try
        {
            ct.prefixMethod(sig, "return supercalafragalistic;");
            unreachable();
        }
        catch (MethodCompileException ex)
        {

        }

        verify();
    }


    @Test
    public void remove_field() throws Exception
    {
        Logger logger = mockLogger();
        MutableComponentModel model = mockMutableComponentModel(logger);

        replay();

        CtClass targetObjectCtClass = findCtClass(FieldRemoval.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(classFactory, targetObjectCtClass, null,
                                                                             model, null);

        ct.removeField("_fieldToRemove");

        ct.finish();

        Class transformed = toClass(targetObjectCtClass);

        for (Field f : transformed.getDeclaredFields())
        {
            if (f.getName().equals("_fieldToRemove"))
                throw new AssertionError("_fieldToRemove still in transformed class.");
        }

        verify();
    }

    @Test
    public void get_method_identifier() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(MethodIdentifier.class, logger);

        List<TransformMethodSignature> sigs = ct.findMethodsWithAnnotation(OnEvent.class);

        assertEquals(sigs.size(), 1);

        TransformMethodSignature sig = sigs.get(0);

        assertEquals(ct.getMethodIdentifier(sig),
                     "org.apache.tapestry5.internal.transform.pages.MethodIdentifier.makeWaves(java.lang.String, int[]) (at MethodIdentifier.java:24)");

        verify();
    }

    @Test
    public void base_class_methods_are_never_overridden() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(TransformMethodSignature signature)
            {
                return true;
            }
        };

        ClassTransformation ct = createClassTransformation(SimpleBean.class, logger);

        List<TransformMethodSignature> methods = ct.findMethods(filter);

        assertFalse(methods.isEmpty());

        for (TransformMethodSignature sig : methods)
        {
            assertFalse(ct.isMethodOverride(sig));
        }


        verify();
    }

    @Test
    public void check_for_method_override_on_non_declared_method() throws Exception
    {
        Logger logger = mockLogger();

        replay();

        ClassTransformation ct = createClassTransformation(SimpleBean.class, logger);

        TransformMethodSignature sig = new TransformMethodSignature("methodDoesNotExist");

        try
        {
            ct.isMethodOverride(sig);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Method public void methodDoesNotExist() is not implemented by transformed class org.apache.tapestry5.internal.services.SimpleBean.");
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
                                                                                       stubMutableComponentModel(
                                                                                               logger));

        assertFalse(childTransform.isMethodOverride(new TransformMethodSignature("notOverridden")));

        assertTrue(childTransform.isMethodOverride(
                new TransformMethodSignature(Modifier.PUBLIC, "void", "setAge", new String[] { "int" }, null)));
    }


}
