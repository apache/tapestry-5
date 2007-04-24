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

package org.apache.tapestry.internal.services;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Loader;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

import org.apache.commons.logging.Log;
import org.apache.tapestry.annotations.Meta;
import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.annotations.Retain;
import org.apache.tapestry.annotations.SetupRender;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.internal.transform.InheritedAnnotation;
import org.apache.tapestry.internal.transform.pages.AbstractFoo;
import org.apache.tapestry.internal.transform.pages.BarImpl;
import org.apache.tapestry.internal.transform.pages.BasicComponent;
import org.apache.tapestry.internal.transform.pages.ChildClassInheritsAnnotation;
import org.apache.tapestry.internal.transform.pages.ClaimedFields;
import org.apache.tapestry.internal.transform.pages.EventHandlerTarget;
import org.apache.tapestry.internal.transform.pages.FindFieldClass;
import org.apache.tapestry.internal.transform.pages.MethodIdentifier;
import org.apache.tapestry.internal.transform.pages.ParentClass;
import org.apache.tapestry.internal.transform.pages.TargetObject;
import org.apache.tapestry.internal.transform.pages.TargetObjectSubclass;
import org.apache.tapestry.ioc.internal.services.PropertyAccessImpl;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.ComponentResourcesAware;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.MethodFilter;
import org.apache.tapestry.services.MethodSignature;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * The tests share a number of resources, and so are run sequentially.
 */
@Test(sequential = true)
public class InternalClassTransformationImplTest extends InternalBaseTestCase
{
    private static final String STRING_CLASS_NAME = "java.lang.String";

    private ClassPool _classPool;

    private final ClassLoader _contextClassLoader = currentThread().getContextClassLoader();

    private Loader _loader;

    private PropertyAccess _access;

    @BeforeClass
    public void setup_access()
    {
        _access = getService("PropertyAccess", PropertyAccess.class);
    }

    @AfterClass
    public void cleanup_access()
    {
        _access = null;
    }

    /**
     * We need a new ClassPool for each individual test, since many of the tests will end up
     * modifying one or more CtClass instances.
     */
    @BeforeMethod
    public void setup_classpool()
    {
        _classPool = new ClassPool();

        _loader = new Loader(_contextClassLoader, _classPool);

        // This ensures that only the classes we explicitly access and modify
        // are loaded by the new loader; everthing else comes out of the common
        // context class loader, which prevents a lot of nasty class cast exceptions.

        _loader.delegateLoadingOf("org.apache.tapestry.");

        // Inside Maven Surefire, the system classpath is not sufficient to find all
        // the necessary files.
        _classPool.appendClassPath(new LoaderClassPath(_loader));
    }

    private CtClass findCtClass(Class targetClass) throws NotFoundException
    {
        return _classPool.get(targetClass.getName());
    }

    @Test
    public void new_member_name() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, log);

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
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, log);

        assertEquals(ct.newMemberName("prefix", "fred"), "_$prefix_fred");
        assertEquals(ct.newMemberName("prefix", "fred"), "_$prefix_fred_0");

        // Here we're exposing a bit of the internal algorithm, which strips
        // off '$' and '_' before tacking "_$" in front.

        assertEquals(ct.newMemberName("prefix", "_fred"), "_$prefix_fred_1");
        assertEquals(ct.newMemberName("prefix", "_$fred"), "_$prefix_fred_2");
        assertEquals(ct.newMemberName("prefix", "__$___$____$_fred"), "_$prefix_fred_3");

        verify();
    }

    private InternalClassTransformation createClassTransformation(Class targetClass, Log log)
            throws NotFoundException
    {
        CtClass ctClass = findCtClass(targetClass);

        return new InternalClassTransformationImpl(ctClass, _contextClassLoader, log, null);
    }

    @Test
    public void find_annotation_on_unknown_field() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, log);

        try
        {
            ct.getFieldAnnotation("unknownField", Retain.class);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Class org.apache.tapestry.internal.transform.pages.ParentClass does not contain a field named 'unknownField'.");
        }

        verify();
    }

    @Test
    public void find_field_annotation() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, log);

        Retain retain = ct.getFieldAnnotation("_annotatedField", Retain.class);

        assertNotNull(retain);

        verify();
    }

    @Test
    public void field_does_not_contain_requested_annotation() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, log);

        // Field with annotations, but not that annotation
        assertNull(ct.getFieldAnnotation("_annotatedField", Override.class));

        // Field with no annotations
        assertNull(ct.getFieldAnnotation("_parentField", Override.class));

        verify();
    }

    @Test
    public void find_fields_with_annotation() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, log);

        List<String> fields = ct.findFieldsWithAnnotation(Retain.class);

        assertEquals(fields.size(), 1);
        assertEquals(fields.get(0), "_annotatedField");

        verify();
    }

    @Test
    public void find_fields_of_type() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(FindFieldClass.class, log);

        checkFindFields(ct, "boolean", "_booleanValue");
        checkFindFields(ct, "int[]", "_intArrayValue");
        checkFindFields(ct, "java.lang.String", "_stringValue");
        checkFindFields(ct, "java.util.Date[]", "_dateArrayValue");

        verify();
    }

    @Test
    public void get_field_modifiers() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(CheckFieldType.class, log);

        assertEquals(ct.getFieldModifiers("_privateField"), Modifier.PRIVATE);
        assertEquals(ct.getFieldModifiers("_map"), Modifier.PRIVATE + Modifier.FINAL);
    }

    @Test
    public void find_fields_of_type_excludes_claimed_fields() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(FindFieldClass.class, log);

        ct.claimField("_booleanValue", this);

        checkFindFields(ct, "boolean");

        verify();
    }

    private void checkFindFields(ClassTransformation ct, String fieldType, String... expectedNames)
    {
        List<String> actual = ct.findFieldsOfType(fieldType);

        assertEquals(actual, Arrays.asList(expectedNames));
    }

    @Test
    public void find_fields_with_annotation_excludes_claimed_files() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, log);

        ct.claimField("_annotatedField", this);

        List<String> fields = ct.findFieldsWithAnnotation(Retain.class);

        assertTrue(fields.isEmpty());

        verify();
    }

    @Test
    public void no_fields_contain_requested_annotation() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, log);

        List<String> fields = ct.findFieldsWithAnnotation(Documented.class);

        assertTrue(fields.isEmpty());

        verify();
    }

    @Test
    public void claim_fields() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ClaimedFields.class, log);

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
            assertEquals(
                    ex.getMessage(),
                    "Field _field4 of class org.apache.tapestry.internal.transform.pages.ClaimedFields is already claimed by Fred and can not be claimed by Barney.");
        }

        verify();
    }

    @Test
    public void added_fields_are_not_listed_as_unclaimed_fields() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ClaimedFields.class, log);

        ct.addField(Modifier.PRIVATE, "int", "newField");

        List<String> unclaimed = ct.findUnclaimedFields();

        assertEquals(unclaimed, asList("_field1", "_field4", "_zzfield"));

        verify();
    }

    @Test
    public void find_class_annotations() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, log);

        Meta meta = ct.getAnnotation(Meta.class);

        assertNotNull(meta);

        // Try again (the annotations will be cached). Use an annotation
        // that will not be present.

        Target t = ct.getAnnotation(Target.class);

        assertNull(t);

        verify();
    }

    /**
     * More a test of how Javassist works. Javassist does not honor the Inherited annotation for
     * classes (this kind of makes sense, since it won't necessarily have the super-class in
     * memory).
     */
    @Test
    public void ensure_subclasses_inherit_parent_class_annotations() throws Exception
    {
        // The Java runtime does honor @Inherited
        assertNotNull(ChildClassInheritsAnnotation.class.getAnnotation(InheritedAnnotation.class));

        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ChildClassInheritsAnnotation.class, log);

        InheritedAnnotation ia = ct.getAnnotation(InheritedAnnotation.class);

        // Javassist does not, but ClassTransformation patches around that.

        assertNotNull(ia);

        verify();
    }

    /**
     * These tests are really to assert my understanding of Javassist's API. I guess we should keep
     * them around to make sure that future versions of Javassist work the same as our expectations.
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
    public void ensure_javassist_does_not_show_interface_methods_on_abstract_class()
            throws Exception
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
    public void ensure_javassist_does_not_show_extended_interface_methods_on_interface()
            throws Exception
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
        InternalComponentResources resources = newInternalComponentResources();

        CtClass targetObjectCtClass = findCtClass(TargetObject.class);

        Log log = newLog();

        replay();

        InternalClassTransformation ct = new InternalClassTransformationImpl(targetObjectCtClass,
                _contextClassLoader, log, null);

        // Default behavior is to add an injected field for the InternalComponentResources object,
        // so we'll just check that.

        ct.finish();

        Class transformed = _classPool.toClass(targetObjectCtClass, _loader);

        Instantiator instantiator = ct.createInstantiator(transformed);

        ComponentResourcesAware instance = instantiator.newInstance(resources);

        assertSame(instance.getComponentResources(), resources);

        verify();
    }

    @Test
    public void add_injected_field_from_parent_transformation() throws Exception
    {
        final String value = "from the parent";

        InternalComponentResources resources = newInternalComponentResources();

        CtClass targetObjectCtClass = findCtClass(TargetObject.class);

        Log log = newLog();

        replay();

        InternalClassTransformation ct = new InternalClassTransformationImpl(targetObjectCtClass,
                _loader, log, null);

        String parentFieldName = ct.addInjectedField(String.class, "_value", value);

        // Default behavior is to add an injected field for the InternalComponentResources object,
        // so we'll just check that.

        ct.finish();

        // Instantiate the transformed base class, so that we can create a transformed
        // subclass.

        _classPool.toClass(targetObjectCtClass, _loader);

        // Now lets work on the subclass

        CtClass subclassCtClass = findCtClass(TargetObjectSubclass.class);

        ct = new InternalClassTransformationImpl(subclassCtClass, ct, _loader, log, null);

        String subclassFieldName = ct.addInjectedField(String.class, "_childValue", value);

        // This is what proves it is cached.

        assertEquals(subclassFieldName, parentFieldName);

        // This proves the the field is protected and can be used in subclasses.

        ct.addMethod(new MethodSignature(Modifier.PUBLIC, "java.lang.String", "getValue", null,
                null), "return " + subclassFieldName + ";");

        ct.finish();

        Class transformed = _classPool.toClass(subclassCtClass, _loader);

        Instantiator instantiator = ct.createInstantiator(transformed);

        Object instance = instantiator.newInstance(resources);

        Object actual = _access.get(instance, "value");

        assertSame(actual, value);

        verify();
    }

    @Test
    public void wrong_instance_type_passed_to_create_instantiator() throws Exception
    {
        CtClass ctClass = findCtClass(BasicComponent.class);

        Log log = newLog();

        replay();

        InternalClassTransformation ct = new InternalClassTransformationImpl(ctClass,
                _contextClassLoader, log, null);

        _classPool.toClass(ctClass, _loader);

        try
        {
            ct.createInstantiator(Boolean.class);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), ServicesMessages.incorrectClassForInstantiator(
                    BasicComponent.class.getName(),
                    Boolean.class));
        }

        verify();
    }

    @Test
    public void add_interface_to_class() throws Exception
    {
        InternalComponentResources resources = newInternalComponentResources();

        CtClass targetObjectCtClass = findCtClass(TargetObject.class);

        Log log = newLog();

        replay();

        InternalClassTransformation ct = new InternalClassTransformationImpl(targetObjectCtClass,
                _contextClassLoader, log, null);

        ct.addImplementedInterface(FooInterface.class);
        ct.addImplementedInterface(GetterMethodsInterface.class);

        ct.finish();

        Class transformed = _classPool.toClass(targetObjectCtClass, _loader);

        Class[] interfaces = transformed.getInterfaces();

        assertEquals(interfaces, new Class[]
        { Component.class, FooInterface.class, GetterMethodsInterface.class });

        Object target = ct.createInstantiator(transformed).newInstance(resources);

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
        InternalComponentResources resources = newInternalComponentResources();

        Log log = newLog();

        replay();

        CtClass targetObjectCtClass = findCtClass(ReadOnlyBean.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(targetObjectCtClass,
                _contextClassLoader, log, null);

        ct.makeReadOnly("_value");

        ct.finish();

        Class transformed = _classPool.toClass(targetObjectCtClass, _loader);

        Object target = ct.createInstantiator(transformed).newInstance(resources);

        PropertyAccess access = new PropertyAccessImpl();

        try
        {
            access.set(target, "value", "anything");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            // The PropertyAccess layer adds a wrapper exception around the real one.

            assertEquals(
                    ex.getCause().getMessage(),
                    "Field org.apache.tapestry.internal.services.ReadOnlyBean._value is read-only.");
        }

        verify();
    }

    @Test
    public void removed_fields_should_not_show_up_as_unclaimed() throws Exception
    {
        Log log = newLog();

        replay();

        CtClass targetObjectCtClass = findCtClass(RemoveFieldBean.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(targetObjectCtClass,
                _contextClassLoader, log, null);

        ct.removeField("_barney");

        assertEquals(ct.findUnclaimedFields(), asList("_fred"));

        verify();
    }

    @Test
    public void add_to_constructor() throws Exception
    {
        InternalComponentResources resources = newInternalComponentResources();

        Log log = newLog();

        replay();

        CtClass targetObjectCtClass = findCtClass(ReadOnlyBean.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(targetObjectCtClass,
                _contextClassLoader, log, null);

        ct.extendConstructor("_value = \"from constructor\";");

        ct.finish();

        Class transformed = _classPool.toClass(targetObjectCtClass, _loader);

        Object target = ct.createInstantiator(transformed).newInstance(resources);

        PropertyAccess access = new PropertyAccessImpl();

        assertEquals(access.get(target, "value"), "from constructor");

        verify();
    }

    @Test
    public void inject_field() throws Exception
    {
        InternalComponentResources resources = newInternalComponentResources();

        Log log = newLog();

        replay();

        CtClass targetObjectCtClass = findCtClass(ReadOnlyBean.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(targetObjectCtClass,
                _contextClassLoader, log, null);

        ct.injectField("_value", "Tapestry");

        ct.finish();

        Class transformed = _classPool.toClass(targetObjectCtClass, _loader);

        Object target = ct.createInstantiator(transformed).newInstance(resources);

        PropertyAccess access = new PropertyAccessImpl();

        assertEquals(access.get(target, "value"), "Tapestry");

        try
        {
            access.set(target, "value", "anything");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            // The PropertyAccess layer adds a wrapper exception around the real one.

            assertEquals(
                    ex.getCause().getMessage(),
                    "Field org.apache.tapestry.internal.services.ReadOnlyBean._value is read-only.");
        }

        verify();
    }

    /**
     * Tests the basic functionality of overriding read and write; also tests the case for multiple
     * field read/field write substitions.
     */
    @Test
    public void override_field_read_and_write() throws Exception
    {
        InternalComponentResources resources = newInternalComponentResources();

        Log log = newLog();

        replay();

        CtClass targetObjectCtClass = findCtClass(FieldAccessBean.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(targetObjectCtClass,
                _contextClassLoader, log, null);

        replaceAccessToField(ct, "foo");
        replaceAccessToField(ct, "bar");

        // Stuff ...

        ct.finish();

        Class transformed = _classPool.toClass(targetObjectCtClass, _loader);

        Object target = ct.createInstantiator(transformed).newInstance(resources);

        // target is no longer assignable to FieldAccessBean; its a new class from a new class
        // loader. So we use reflective access, which doesn't care about such things.

        PropertyAccess access = new PropertyAccessImpl();

        checkReplacedFieldAccess(access, target, "foo");
        checkReplacedFieldAccess(access, target, "bar");

        verify();
    }

    private void checkReplacedFieldAccess(PropertyAccess access, Object target, String propertyName)
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

        MethodSignature readMethodSignature = new MethodSignature(Modifier.PRIVATE,
                STRING_CLASS_NAME, readMethodName, null, null);

        ct.addMethod(readMethodSignature, String.format(
                "throw new RuntimeException(\"read %s\");",
                baseName));

        ct.replaceReadAccess(fieldName, readMethodName);

        String writeMethodName = "_write_" + baseName;

        MethodSignature writeMethodSignature = new MethodSignature(Modifier.PRIVATE, "void",
                writeMethodName, new String[]
                { STRING_CLASS_NAME }, null);
        ct.addMethod(writeMethodSignature, String.format(
                "throw new RuntimeException(\"write %s\");",
                baseName));

        ct.replaceWriteAccess(fieldName, writeMethodName);
    }

    @Test
    public void find_methods_with_annotation() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(AnnotatedPage.class, log);

        List<MethodSignature> l = ct.findMethodsWithAnnotation(SetupRender.class);

        // Check order

        assertEquals(l.size(), 2);
        assertEquals(l.get(0).toString(), "void beforeRender()");
        assertEquals(l.get(1).toString(), "boolean earlyRender(org.apache.tapestry.MarkupWriter)");

        // Check up on cacheing

        assertEquals(ct.findMethodsWithAnnotation(SetupRender.class), l);

        // Check up on no match.

        assertTrue(ct.findFieldsWithAnnotation(Deprecated.class).isEmpty());

        verify();
    }

    @Test
    public void find_methods_using_filter() throws Exception
    {
        Log log = newLog();

        replay();

        final ClassTransformation ct = createClassTransformation(AnnotatedPage.class, log);

        // Duplicates, somewhat less efficiently, the logic in find_methods_with_annotation().

        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(MethodSignature signature)
            {
                return ct.getMethodAnnotation(signature, SetupRender.class) != null;
            }
        };

        List<MethodSignature> l = ct.findMethods(filter);

        // Check order

        assertEquals(l.size(), 2);
        assertEquals(l.get(0).toString(), "void beforeRender()");
        assertEquals(l.get(1).toString(), "boolean earlyRender(org.apache.tapestry.MarkupWriter)");

        // Check up on cacheing

        assertEquals(ct.findMethodsWithAnnotation(SetupRender.class), l);

        // Check up on no match.

        assertTrue(ct.findFieldsWithAnnotation(Deprecated.class).isEmpty());

        verify();
    }

    @Test
    public void to_class_with_primitive_type() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(AnnotatedPage.class, log);

        assertSame(ct.toClass("float"), Float.class);

        verify();
    }

    @Test
    public void to_class_with_object_type() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(AnnotatedPage.class, log);

        assertSame(ct.toClass("java.util.Map"), Map.class);

        verify();
    }

    @Test
    public void non_private_fields_log_an_error() throws Exception
    {
        Log log = newLog();

        log.error(ServicesMessages.nonPrivateFields(VisibilityBean.class.getName(), Arrays.asList(
                "_$myPackagePrivate",
                "_$myProtected",
                "_$myPublic")));

        replay();

        InternalClassTransformation ct = createClassTransformation(VisibilityBean.class, log);

        List<String> names = ct.findFieldsWithAnnotation(Retain.class);

        // Only _myLong shows up, because its the only private field

        assertEquals(names, Arrays.asList("_$myLong"));

        // However, all the fields are "reserved" via the IdAllocator ...

        assertEquals(ct.newMemberName("_$myLong"), "_$myLong_0");
        assertEquals(ct.newMemberName("_$myStatic"), "_$myStatic_0");
        assertEquals(ct.newMemberName("_$myProtected"), "_$myProtected_0");

        // The check for non-private fields has been moved from the ICTI constructor to the finish
        // method.

        ct.finish();

        verify();
    }

    @Test
    public void find_annotation_in_method() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(EventHandlerTarget.class, log);

        OnEvent annotation = ct.getMethodAnnotation(new MethodSignature("handler"), OnEvent.class);

        // Check that the attributes of the annotation match the expectation.

        assertEquals(annotation.value(), new String[]
        { "fred", "barney" });
        assertEquals(annotation.component(), new String[]
        { "alpha", "beta" });

        verify();
    }

    @Test
    public void find_annotation_in_unknown_method() throws Exception
    {
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(ParentClass.class, log);

        try
        {
            ct.getMethodAnnotation(new MethodSignature("foo"), OnEvent.class);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Class org.apache.tapestry.internal.transform.pages.ParentClass does not declare method 'public void foo()'.");
        }

        verify();
    }

    @Test
    public void remove_field() throws Exception
    {
        Log log = newLog();

        replay();

        CtClass targetObjectCtClass = findCtClass(FieldRemoval.class);

        InternalClassTransformation ct = new InternalClassTransformationImpl(targetObjectCtClass,
                _contextClassLoader, log, null);

        ct.removeField("_fieldToRemove");

        ct.finish();

        Class transformed = _classPool.toClass(targetObjectCtClass, _loader);

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
        Log log = newLog();

        replay();

        ClassTransformation ct = createClassTransformation(MethodIdentifier.class, log);

        List<MethodSignature> sigs = ct.findMethodsWithAnnotation(OnEvent.class);

        assertEquals(sigs.size(), 1);

        MethodSignature sig = sigs.get(0);

        assertEquals(
                ct.getMethodIdentifier(sig),
                "org.apache.tapestry.internal.transform.pages.MethodIdentifier.makeWaves(java.lang.String, int[]) (at MethodIdentifier.java:24)");

        verify();
    }
}
