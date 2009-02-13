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

package org.apache.tapestry5.internal.bindings;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.BeforeRenderBody;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.util.IntegerRange;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.BindingFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PropBindingFactoryTest extends InternalBaseTestCase
{
    private BindingFactory factory;

    @BeforeClass
    public void setup_factory()
    {
        factory = getService("PropBindingFactory", BindingFactory.class);
    }

    @AfterClass
    public void cleanup_factory()
    {
        factory = null;
    }

    private ComponentResources newComponentResources(Component component)
    {
        ComponentResources resources = mockComponentResources();
        train_getComponent(resources, component);

        train_getCompleteId(resources, "foo.Bar:baz");

        return resources;
    }

    @Test
    public void object_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "objectValue", l);

        assertSame(binding.getBindingType(), String.class);

        bean.setObjectValue("first");

        assertEquals(binding.get(), "first");

        binding.set("second");

        assertEquals(bean.getObjectValue(), "second");
        assertEquals(InternalUtils.locationOf(binding), l);

        assertEquals(binding.toString(), "PropBinding[test binding foo.Bar:baz(objectValue)]");

        verify();
    }

    @Test
    public void annotation_from_read_only_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "readOnly", l);

        assertEquals(binding.getAnnotation(Validate.class).value(), "readonly");

        verify();
    }

    @Test
    public void annotation_from_write_only_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "writeOnly", l);

        assertEquals(binding.getAnnotation(Validate.class).value(), "writeonly");

        verify();
    }

    @Test
    public void annotation_does_not_exist()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "intValue", l);

        assertNull(binding.getAnnotation(Validate.class));

        verify();
    }

    @Test
    public void annotation_on_named_method()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding(
                "test binding",
                resources,
                null,
                "stringHolderMethod()",
                l);

        assertNotNull(binding.getAnnotation(BeforeRenderBody.class));

        verify();
    }

    @Test
    public void annnotation_on_read_method_takes_precedence_over_write_method()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "objectValue", l);

        assertEquals(binding.getAnnotation(Validate.class).value(), "getObjectValue");

        verify();
    }

    @Test
    public void property_path()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding(
                "test binding",
                resources,
                null,
                "stringHolder.value",
                l);

        assertSame(binding.getBindingType(), String.class);

        bean.getStringHolder().setValue("first");

        assertEquals(binding.get(), "first");

        binding.set("second");

        assertEquals(bean.getStringHolder().getValue(), "second");

        assertEquals(
                binding.toString(),
                "PropBinding[test binding foo.Bar:baz(stringHolder.value)]");

        verify();
    }

    /**
     * The "preamble" are the non-terminal property or method names.
     */
    @Test
    public void property_path_with_explicit_method_in_preamble()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding(
                "test binding",
                resources,
                null,
                "stringHolderMethod().value",
                l);

        assertSame(binding.getBindingType(), String.class);

        bean.getStringHolder().setValue("first");

        assertEquals(binding.get(), "first");

        assertEquals(
                binding.toString(),
                "PropBinding[test binding foo.Bar:baz(stringHolderMethod().value)]");

        verify();
    }

    @Test
    public void method_call_as_terminal()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding(
                "test binding",
                resources,
                null,
                "stringHolderMethod().stringValue()",
                l);

        assertSame(binding.getBindingType(), String.class);

        bean.getStringHolder().setValue("first");

        assertEquals(binding.get(), "first");

        try
        {
            binding.set("read-only");
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Expression 'stringHolderMethod().stringValue()' for class org.apache.tapestry5.internal.bindings.TargetBean is read-only.");
            assertSame(ex.getLocation(), l);
        }

        verify();
    }

    @Test
    public void method_not_found_in_preamble()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        replay();

        try
        {
            factory.newBinding("test binding", resources, null, "isThatRealBlood().value", l);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "No public method \'isThatRealBlood()\' in class org.apache.tapestry5.internal.bindings.TargetBean (within property expression \'isThatRealBlood().value\').");
        }

        verify();
    }

    @Test
    public void method_not_found_in_terminal()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        replay();

        try
        {
            factory.newBinding(
                    "test binding",
                    resources,
                    null,
                    "stringHolder.isThatRealBlood()",
                    l);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "No public method \'isThatRealBlood()\' in class org.apache.tapestry5.internal.bindings.StringHolder (within property expression \'stringHolder.isThatRealBlood()\').");
        }

        verify();
    }

    @Test
    public void void_method_in_preamble()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        replay();

        try
        {
            factory.newBinding("test binding", resources, null, "voidMethod().value", l);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Method \'voidMethod()\' returns void (in class org.apache.tapestry5.internal.bindings.TargetBean, within property expression \'voidMethod().value\').");
        }

        verify();
    }

    @Test
    public void void_method_as_terminal()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        replay();

        try
        {
            factory.newBinding("test binding", resources, null, "stringHolder.voidMethod()", l);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Method \'voidMethod()\' returns void (in class org.apache.tapestry5.internal.bindings.StringHolder, within property expression \'stringHolder.voidMethod()\').");
        }

        verify();
    }

    @Test
    public void property_path_through_missing_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        replay();

        String propertyPath = "stringHolder.missingProperty.terminalProperty";

        try
        {
            factory.newBinding("test binding", resources, null, propertyPath, l);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Class org.apache.tapestry5.internal.bindings.StringHolder does not contain a property named \'missingProperty\' "
                            + "(within property expression \'stringHolder.missingProperty.terminalProperty\').  Available properties: value.");
        }

        verify();
    }

    @Test
    public void property_path_through_write_only_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        replay();

        String propertyPath = "writeOnly.terminalProperty";

        try
        {
            factory.newBinding("test binding", resources, null, propertyPath, l);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Property \'writeOnly\' of class org.apache.tapestry5.internal.bindings.TargetBean (within property expression \'writeOnly.terminalProperty\') is not readable (it has no read accessor method).");
        }

        verify();
    }

    @Test
    public void primitive_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "intValue", l);

        assertSame(binding.getBindingType(), int.class);

        bean.setIntValue(1);

        assertEquals(binding.get(), 1);

        binding.set(2);

        assertEquals(bean.getIntValue(), 2);

        verify();
    }

    @Test
    public void read_only_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "readOnly", l);

        assertEquals(binding.get(), "ReadOnly");

        try
        {
            binding.set("fail");
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Expression 'readOnly' for class org.apache.tapestry5.internal.bindings.TargetBean is read-only.");
            assertEquals(ex.getLocation(), l);
        }

        verify();
    }

    @Test
    public void write_only_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = newComponentResources(bean);
        Location l = mockLocation();

        replay();

        Binding binding = factory.newBinding("test binding", resources, null, "writeOnly", l);

        binding.set("updated");

        assertEquals(bean.writeOnly, "updated");

        try
        {
            assertEquals(binding.get(), "ReadOnly");
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Expression writeOnly for class org.apache.tapestry5.internal.bindings.TargetBean is write-only.");
            assertEquals(ex.getLocation(), l);
        }

        verify();
    }

    @Test
    public void unknown_property()
    {
        TargetBean bean = new TargetBean();
        ComponentResources resources = mockComponentResources();
        Location l = mockLocation();

        train_getComponent(resources, bean);

        replay();

        try
        {
            factory.newBinding("test binding", resources, null, "missingProperty", l);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Class org.apache.tapestry5.internal.bindings.TargetBean does not contain a property named \'missingProperty\' "
                            + "(within property expression \'missingProperty\').  "
                            + "Available properties: class, componentResources, intValue, objectValue, readOnly, stringHolder, writeOnly.");
        }

        verify();
    }

    @Test(dataProvider = "values")
    public void special_prop_binding_values(String expression, Object expected)
    {
        Location l = mockLocation();
        String description = "my description";
        ComponentResources resources = mockComponentResources();
        Component component = mockComponent();

        train_getComponent(resources, component);
        train_getCompleteId(resources, "Does.not.matter");

        replay();

        Binding binding = factory.newBinding(description, resources, null, expression, l);

        assertEquals(binding.get(), expected);

        // All of these are invariatns, even though they are generated from the PropertyConduit.

        assertTrue(binding.isInvariant());

        verify();
    }

    @DataProvider
    public Object[][] values()
    {
        return new Object[][]
                {
                        { "true", true, },
                        { "True", true, },
                        { " true ", true, },
                        { "false", false },
                        { "null", null },
                        { "3", 3l },
                        { " 37 ", 37l },
                        { " -227", -227l },
                        { " 5.", 5d },
                        { " -100.", -100d },
                        { " -0.0 ", -0d },
                        { "+50", 50l },
                        { "+7..+20", new IntegerRange(7, 20) },
                        { "+5.5", 5.5d },
                        { "1..10", new IntegerRange(1, 10) },
                        { " -20 .. -30 ", new IntegerRange(-20, -30) },
                        { "0.", 0d },
                        { " 227.75", 227.75d },
                        { " -10123.67", -10123.67d },
                        { "'Hello World'", "Hello World" },
                        { " 'Whitespace Ignored' ", "Whitespace Ignored" },
                        { " ' Inside ' ", " Inside " }
                };
    }
}
