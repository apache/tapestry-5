// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.beaneditor.RelativePosition;
import org.apache.tapestry5.internal.PropertyOrderBean;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.transform.pages.ReadOnlyBean;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.services.BeanModelSource;
import org.easymock.EasyMock;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Tests for the bean editor model source itself, as well as the model classes.
 */
public class BeanModelSourceImplTest extends InternalBaseTestCase
{
    private BeanModelSource source;

    @BeforeClass
    public void setup()
    {
        source = getObject(BeanModelSource.class, null);
    }

    /**
     * Tests defaults for property names, labels and conduits.
     */
    @Test
    public void default_model_for_bean()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        assertSame(model.getBeanType(), SimpleBean.class);

        // Based on order of the getter methods (no longer alphabetical)

        assertEquals(model.getPropertyNames(), Arrays.asList("firstName", "lastName", "age"));

        assertEquals(model.toString(),
                     "BeanModel[org.apache.tapestry5.internal.services.SimpleBean properties:firstName, lastName, age]");

        PropertyModel age = model.get("age");

        assertEquals(age.getLabel(), "Age");
        assertSame(age.getPropertyType(), int.class);
        assertEquals(age.getDataType(), "number");

        PropertyModel firstName = model.get("firstName");

        assertEquals(firstName.getLabel(), "First Name");
        assertEquals(firstName.getPropertyType(), String.class);
        assertEquals(firstName.getDataType(), "text");

        assertEquals(model.get("lastName").getLabel(), "Last Name");

        PropertyConduit conduit = model.get("lastName").getConduit();

        SimpleBean instance = new SimpleBean();

        instance.setLastName("Lewis Ship");

        assertEquals(conduit.get(instance), "Lewis Ship");

        conduit.set(instance, "TapestryDude");

        assertEquals(instance.getLastName(), "TapestryDude");

        // Now, one with some type coercion.

        age.getConduit().set(instance, "40");

        assertEquals(instance.getAge(), 40);

        verify();
    }

    @Test
    public void include_properties()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        assertSame(model.getBeanType(), SimpleBean.class);

        model.include("lastname", "firstname");

        // Based on order of the getter methods (no longer alphabetical)

        assertEquals(model.getPropertyNames(), Arrays.asList("lastName", "firstName"));

        verify();
    }

    @Test
    public void add_before()
    {
        Messages messages = mockMessages();
        PropertyConduit conduit = mockPropertyConduit();

        Class propertyType = String.class;

        stub_contains(messages, false);

        expect(conduit.getPropertyType()).andReturn(propertyType).atLeastOnce();
        expect(conduit.getAnnotation(EasyMock.isA(Class.class))).andStubReturn(null);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        assertEquals(model.getPropertyNames(), Arrays.asList("firstName", "lastName", "age"));

        // Note the use of case insensitivity here.

        PropertyModel property = model.add(RelativePosition.BEFORE, "lastname", "middleInitial", conduit);

        assertEquals(model.getPropertyNames(), Arrays.asList("firstName", "middleInitial", "lastName", "age"));

        assertEquals(property.getPropertyName(), "middleInitial");
        assertSame(property.getConduit(), conduit);
        assertSame(property.getPropertyType(), propertyType);

        verify();
    }

    /**
     * TAPESTRY-2202
     */
    @Test
    public void new_instance()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel<SimpleBean> model = source.create(SimpleBean.class, true, messages);

        SimpleBean s1 = model.newInstance();

        assertNotNull(s1);

        SimpleBean s2 = model.newInstance();

        assertNotNull(s2);
        assertNotSame(s1, s2);

        verify();
    }

    @Test
    public void add_before_using_default_conduit()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        model.exclude("firstname");

        assertEquals(model.getPropertyNames(), Arrays.asList("lastName", "age"));

        // Note the use of case insensitivity here.

        PropertyModel property = model.add(RelativePosition.BEFORE, "lastname", "firstName");

        assertEquals(model.getPropertyNames(), Arrays.asList("firstName", "lastName", "age"));

        assertEquals(property.getPropertyName(), "firstName");
        assertSame(property.getPropertyType(), String.class);

        verify();
    }

    @Test
    public void add_after()
    {
        Messages messages = mockMessages();
        PropertyConduit conduit = mockPropertyConduit();

        Class propertyType = String.class;

        stub_contains(messages, false);

        expect(conduit.getPropertyType()).andReturn(propertyType).atLeastOnce();

        expect(conduit.getAnnotation(EasyMock.isA(Class.class))).andStubReturn(null);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        assertEquals(model.getPropertyNames(), Arrays.asList("firstName", "lastName", "age"));

        PropertyModel property = model.add(RelativePosition.AFTER, "firstname", "middleInitial", conduit);

        assertEquals(model.getPropertyNames(), Arrays.asList("firstName", "middleInitial", "lastName", "age"));

        assertEquals(property.getPropertyName(), "middleInitial");
        assertSame(property.getConduit(), conduit);
        assertSame(property.getPropertyType(), propertyType);

        verify();
    }

    @Test
    public void filtering_out_read_only_properties()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(ReadOnlyBean.class, true, messages);

        assertEquals(model.getPropertyNames(), Arrays.asList("value"));

        model = source.create(ReadOnlyBean.class, false, messages);

        assertEquals(model.getPropertyNames(), Arrays.asList("value", "readOnly"));

        verify();
    }

    @Test
    public void non_text_property()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(EnumBean.class, true, messages);

        assertEquals(model.getPropertyNames(), Arrays.asList("token"));

        assertEquals(model.get("token").getDataType(), "enum");

        verify();
    }

    @Test
    public void add_duplicate_property_name_is_failure()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        try
        {
            model.add("age");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "Bean editor model for org.apache.tapestry5.internal.services.SimpleBean already contains a property model for property \'age\'.");
        }

        verify();
    }

    @Test
    public void unknown_property_name()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        try
        {
            model.get("frobozz");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "Bean editor model for org.apache.tapestry5.internal.services.SimpleBean does not contain a property named \'frobozz\'.  " + "Available properties: age, firstName, lastName.");
        }

        verify();
    }

    @Test
    public void unknown_property_id()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        model.add("shrub.foo()", null);

        try
        {
            model.getById("frobozz");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "Bean editor model for org.apache.tapestry5.internal.services.SimpleBean does not contain a property with id \'frobozz\'.  "
                                 + "Available property ids: age, firstName, lastName, shrubfoo.");
        }

        verify();
    }

    @Test
    public void get_added_property_by_name()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        PropertyModel pm = model.add("shrub.foo()", null);

        assertSame(model.get("Shrub.Foo()"), pm);

        verify();
    }

    @Test
    public void get_added_property_by_id()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        PropertyModel pm = model.add("shrub.foo()", null);

        assertSame(model.getById("ShrubFoo"), pm);

        verify();

    }

    @Test
    public void order_via_annotation()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(StoogeBean.class, true, messages);

        assertEquals(model.getPropertyNames(), Arrays.asList("larry", "moe", "shemp", "curly"));

        verify();
    }

    @Test
    public void edit_property_label()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages).get("age").label("Decrepitude").model();

        assertEquals(model.get("age").getLabel(), "Decrepitude");

        verify();
    }

    @Test
    public void label_from_component_messages()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        train_contains(messages, "age-label", true);
        train_get(messages, "age-label", "Decrepitude");

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        assertEquals(model.get("age").getLabel(), "Decrepitude");

        verify();
    }

    @Test
    public void array_type_bean()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(StringArrayBean.class, true, messages);

        // There's not editor for string arrays yet, so it won't show up normally.

        PropertyModel propertyModel = model.add("array");

        assertSame(propertyModel.getPropertyType(), String[].class);

        String[] value = { "foo", "bar" };

        StringArrayBean bean = new StringArrayBean();

        PropertyConduit conduit = propertyModel.getConduit();

        conduit.set(bean, value);

        assertSame(bean.getArray(), value);

        assertSame(conduit.get(bean), value);

        verify();
    }

    @Test
    public void composite_bean()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        train_contains(messages, "simpleage-label", true);
        train_get(messages, "simpleage-label", "Years of Age");

        replay();

        BeanModel model = source.create(CompositeBean.class, true, messages);

        // No editor for CompositeBean, so this will be empty.

        assertEquals(model.getPropertyNames(), Collections.emptyList());

        // There's not editor for string arrays yet, so it won't show up normally.

        PropertyModel firstName = model.add("simple.firstName");

        assertEquals(firstName.getLabel(), "First Name");

        PropertyModel age = model.add("simple.age");
        assertEquals(age.getLabel(), "Years of Age");

        CompositeBean bean = new CompositeBean();

        firstName.getConduit().set(bean, "Fred");
        age.getConduit().set(bean, "97");

        assertEquals(bean.getSimple().getFirstName(), "Fred");
        assertEquals(bean.getSimple().getAge(), 97);

        bean.getSimple().setAge(24);

        assertEquals(age.getConduit().get(bean), new Integer(24));

        verify();
    }

    @Test
    public void default_properties_exclude_write_only()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(WriteOnlyBean.class, false, messages);

        assertEquals(model.getPropertyNames(), Arrays.asList("readOnly", "readWrite"));

        verify();
    }

    @Test
    public void add_synthetic_property()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        PropertyModel property = model.add("placeholder", null);

        assertFalse(property.isSortable());
        assertSame(property.getPropertyType(), Object.class);
        assertEquals(property.getLabel(), "Placeholder");

        verify();
    }

    @Test
    public void add_missing_property_is_failure()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        try
        {
            model.add("doesNotExist");
            unreachable();
        }
        catch (PropertyExpressionException ex)
        {
            assertMessageContains(ex, "does not contain a property named 'doesNotExist'");
        }

        verify();
    }

    @Test
    public void exclude_property()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        assertSame(model.exclude("age"), model);

        assertEquals(model.getPropertyNames(), Arrays.asList("firstName", "lastName"));

        verify();
    }

    @Test
    public void exclude_unknown_property_is_noop()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        assertSame(model.exclude("frobozz"), model);

        assertEquals(model.getPropertyNames(), Arrays.asList("firstName", "lastName", "age"));

        verify();
    }

    @Test
    public void nonvisual_properties_are_excluded()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(NonVisualBean.class, true, messages);

        assertEquals(model.getPropertyNames(), Arrays.asList("name"));

        verify();
    }

    @Test
    public void reorder()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(SimpleBean.class, true, messages);

        assertSame(model.getBeanType(), SimpleBean.class);

        // Based on order of the getter methods (no longer alphabetical)

        assertEquals(model.getPropertyNames(), Arrays.asList("firstName", "lastName", "age"));

        // Testing a couple of things here:
        // 1) case insensitive
        // 2) unreferenced property names added to the end.

        model.reorder("lastname", "AGE");

        assertEquals(model.getPropertyNames(), Arrays.asList("lastName", "age", "firstName"));

        verify();
    }

    @Test
    public void reoder_from_annotation()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        BeanModel model = source.create(PropertyOrderBean.class, true, messages);

        assertEquals(model.getPropertyNames(), Arrays.asList("third", "first", "second"));

        verify();
    }
}
