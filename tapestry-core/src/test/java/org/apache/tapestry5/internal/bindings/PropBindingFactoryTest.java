// Copyright 2006, 2007, 2008, 2009, 2010, 2011, 2026 The Apache Software Foundation
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

package org.apache.tapestry5.internal.bindings;

import static org.easymock.EasyMock.expect;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.BeforeRenderBody;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.commons.Location;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.util.IntegerRange;
import org.apache.tapestry5.internal.services.ForceDevelopmentModeModule;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.modules.TapestryModule;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.BindingFactory;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class PropBindingFactoryTest
{
    private static Registry registry;

    private static BindingFactory factory;

    private IMocksControl control;

    @BeforeAll
    static void setup_factory()
    {
        registry = new RegistryBuilder().add(TapestryModule.class, ForceDevelopmentModeModule.class).build();
        registry.performRegistryStartup();

        factory = registry.getService("PropBindingFactory", BindingFactory.class);
    }

    @AfterAll
    static void cleanup_factory()
    {
        registry.shutdown();

        registry = null;
        factory = null;
    }

    @BeforeEach
    void setup_mocks()
    {
        control = EasyMock.createControl();
    }

    @AfterEach
    void cleanupThread()
    {
        registry.cleanupThread();
    }

    private <T> T newMock(Class<T> mockClass)
    {
        return control.createMock(mockClass.getSimpleName(), mockClass);
    }

    private ComponentResources mockComponentResources()
    {
        return newMock(ComponentResources.class);
    }

    private Location mockLocation()
    {
        return newMock(Location.class);
    }

    private static void train_getComponent(ComponentResources resources, Component component)
    {
        expect(resources.getComponent()).andReturn(component).atLeastOnce();
    }

    private static void train_getCompleteId(ComponentResources resources, String completeId)
    {
        expect(resources.getCompleteId()).andReturn(completeId).atLeastOnce();
    }

    private static void assertMessageContains(Throwable t, String... substrings)
    {
        for (String substring : substrings)
        {
            assertTrue(t.getMessage().contains(substring), t.getMessage());
        }
    }

    private ComponentResources newComponentResources(Component component)
    {
        ComponentResources resources = mockComponentResources();
        train_getComponent(resources, component);

        train_getCompleteId(resources, "foo.Bar:baz");

        return resources;
    }

    @Test
    void object_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "objectValue", l);

        assertSame(String.class, binding.getBindingType());

        bean.setObjectValue("first");

        assertEquals("first", binding.get());

        binding.set("second");

        assertEquals("second", bean.getObjectValue());
        assertEquals(l, InternalUtils.locationOf(binding));

        assertEquals("PropBinding[test binding foo.Bar:baz(objectValue)]", binding.toString());

        control.verify();
    }

    @Test
    void annotation_from_read_only_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "readOnly", l);

        assertEquals("readonly", binding.getAnnotation(Validate.class).value());

        control.verify();
    }

    @Test
    void annotation_from_write_only_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "writeOnly", l);

        assertEquals("writeonly", binding.getAnnotation(Validate.class).value());

        control.verify();
    }

    @Test
    void annotation_does_not_exist()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "intValue", l);

        assertNull(binding.getAnnotation(Validate.class));

        control.verify();
    }

    @Test
    void annotation_on_named_method()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "stringHolderMethod()", l);

        assertNotNull(binding.getAnnotation(BeforeRenderBody.class));

        control.verify();
    }

    @Test
    void annnotation_on_read_method_takes_precedence_over_write_method()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "objectValue", l);

        assertEquals("getObjectValue", binding.getAnnotation(Validate.class).value());

        control.verify();
    }

    @Test
    void property_path()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "stringHolder.value", l);

        assertSame(String.class, binding.getBindingType());

        bean.getStringHolder().setValue("first");

        assertEquals("first", binding.get());

        binding.set("second");

        assertEquals("second", bean.getStringHolder().getValue());

        assertEquals("PropBinding[test binding foo.Bar:baz(stringHolder.value)]", binding.toString());

        control.verify();
    }

    /**
     * The "preamble" are the non-terminal property or method names.
     */
    @Test
    void property_path_with_explicit_method_in_preamble()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "stringHolderMethod().value", l);

        assertSame(String.class, binding.getBindingType());

        bean.getStringHolder().setValue("first");

        assertEquals("first", binding.get());

        assertEquals("PropBinding[test binding foo.Bar:baz(stringHolderMethod().value)]", binding.toString());

        control.verify();
    }

    @Test
    void method_call_as_terminal()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "stringHolderMethod().stringValue()", l);

        assertSame(String.class, binding.getBindingType());

        bean.getStringHolder().setValue("first");

        assertEquals("first", binding.get());

        TapestryException ex = assertThrows(TapestryException.class, () -> binding.set("read-only"));

        assertEquals(
                "Expression 'stringHolderMethod().stringValue()' for class org.apache.tapestry5.internal.bindings.TargetBean is read-only.",
                ex.getMessage());
        assertSame(l, ex.getLocation());

        control.verify();
    }

    @Test
    void method_not_found_in_preamble()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        control.replay();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> factory.newBinding("test binding", resources, null, "isThatRealBlood().value", l));

        assertMessageContains(ex,
                "Class org.apache.tapestry5.internal.bindings.TargetBean does not contain a public method named 'isThatRealBlood()'");

        control.verify();
    }

    @Test
    void method_not_found_in_terminal()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        control.replay();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> factory.newBinding("test binding", resources, null, "stringHolder.isThatRealBlood()", l));

        assertMessageContains(ex, "StringHolder", "does not contain a public method", "isThatRealBlood()");

        control.verify();
    }

    @Test
    void void_method_in_preamble()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        control.replay();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> factory.newBinding("test binding", resources, null, "voidMethod().value", l));

        assertMessageContains(ex,
                "Method org.apache.tapestry5.internal.bindings.TargetBean.voidMethod() returns void");

        control.verify();
    }

    @Test
    void void_method_as_terminal()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        control.replay();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> factory.newBinding("test binding", resources, null, "stringHolder.voidMethod()", l));

        assertMessageContains(ex,
                "Method org.apache.tapestry5.internal.bindings.StringHolder.voidMethod() returns void");

        control.verify();
    }

    @Test
    void property_path_through_missing_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        control.replay();

        String propertyPath = "stringHolder.missingProperty.terminalProperty";

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> factory.newBinding("test binding", resources, null, propertyPath, l));

        assertMessageContains(ex,
                "Class org.apache.tapestry5.internal.bindings.StringHolder does not contain a property",
                "\'missingProperty\'");

        control.verify();
    }

    @Test
    void property_path_through_write_only_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        control.replay();

        String propertyPath = "writeOnly.terminalProperty";

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> factory.newBinding("test binding", resources, null, propertyPath, l));

        assertMessageContains(ex,
                "Property \'writeOnly\' of class org.apache.tapestry5.internal.bindings.TargetBean",
                "is not readable (it has no read accessor method).");

        control.verify();
    }

    @Test
    void primitive_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "intValue", l);

        assertSame(int.class, binding.getBindingType());

        bean.setIntValue(1);

        assertEquals(1, binding.get());

        binding.set(2);

        assertEquals(2, bean.getIntValue());

        control.verify();
    }

    @Test
    void read_only_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "readOnly", l);

        assertEquals("ReadOnly", binding.get());

        TapestryException ex = assertThrows(TapestryException.class, () -> binding.set("fail"));

        assertEquals("Expression 'readOnly' for class org.apache.tapestry5.internal.bindings.TargetBean is read-only.",
                ex.getMessage());
        assertEquals(l, ex.getLocation());

        control.verify();
    }

    @Test
    void write_only_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        control.replay();

        Binding binding = factory.newBinding("test binding", resources, null, "writeOnly", l);

        binding.set("updated");

        assertEquals("updated", bean.writeOnly);

        TapestryException ex = assertThrows(TapestryException.class, binding::get);

        assertEquals("Expression 'writeOnly' for class org.apache.tapestry5.internal.bindings.TargetBean is write-only.",
                ex.getMessage());
        assertEquals(l, ex.getLocation());

        control.verify();
    }

    @Test
    void unknown_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        control.replay();

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> factory.newBinding("test binding", resources, null, "missingProperty", l));

        assertMessageContains(ex,
                "Class org.apache.tapestry5.internal.bindings.TargetBean does not contain a property",
                "\'missingProperty\'");

        control.verify();
    }

    private Binding newSpecialBinding(String expression)
    {
        Location l = mockLocation();
        String description = "my description";
        ComponentResources resources = mockComponentResources();
        Component component = newMock(Component.class);

        train_getComponent(resources, component);
        train_getCompleteId(resources, "Does.not.matter");

        control.replay();

        return factory.newBinding(description, resources, null, expression, l);
    }

    @ParameterizedTest
    @MethodSource("values")
    void special_prop_binding_values(String expression, Object expected)
    {
        Binding binding = newSpecialBinding(expression);

        assertEquals(expected, binding.get());

        control.verify();
    }

    // All of these are invariants, even though they are generated from the PropertyConduit.
    @ParameterizedTest
    @MethodSource("values")
    void special_prop_binding_values_are_invariant(String expression, Object expected)
    {
        Binding binding = newSpecialBinding(expression);

        assertTrue(binding.isInvariant());

        control.verify();
    }

    static Stream<Arguments> values()
    {
        return Stream.of(
                Arguments.of("true", true),
                Arguments.of("True", true),
                Arguments.of(" true ", true),
                Arguments.of("false", false),
                Arguments.of("null", null),
                Arguments.of("3", 3l),
                Arguments.of(" 37 ", 37l),
                Arguments.of(" -227", -227l),
                Arguments.of(" 5.", 5d),
                Arguments.of(" -100.", -100d),
                Arguments.of(" -0.0 ", -0d),
                Arguments.of("+50", 50l),
                Arguments.of("+7..+20", new IntegerRange(7, 20)),
                Arguments.of("+5.5", 5.5d),
                Arguments.of("1..10", new IntegerRange(1, 10)),
                Arguments.of(" -20 .. -30 ", new IntegerRange(-20, -30)),
                Arguments.of("0.", 0d),
                Arguments.of(" 227.75", 227.75d),
                Arguments.of(" -10123.67", -10123.67d),
                Arguments.of("'Hello World'", "Hello World"),
                Arguments.of(" 'Whitespace Ignored' ", "Whitespace Ignored"),
                Arguments.of(" ' Inside ' ", " Inside "));
    }
}
