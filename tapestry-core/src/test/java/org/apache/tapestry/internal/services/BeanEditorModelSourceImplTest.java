// Copyright 2007 The Apache Software Foundation
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

import java.util.Arrays;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.beaneditor.BeanEditorModel;
import org.apache.tapestry.beaneditor.PropertyConduit;
import org.apache.tapestry.beaneditor.PropertyEditModel;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.services.BeanEditorModelSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Tests for the bean editor model source itself, as well as the model classes. */
public class BeanEditorModelSourceImplTest extends InternalBaseTestCase
{
    private BeanEditorModelSource _source;

    @BeforeClass
    public void setup()
    {
        _source = getObject("infrastructure:BeanEditorModelSource", BeanEditorModelSource.class);
    }

    @AfterClass
    public void cleanup()
    {
        _source = null;
    }

    /** Tests defaults for property names, labels and conduits. */
    @Test
    public void default_model_for_bean()
    {
        ComponentResources resources = newComponentResources();
        Messages messages = newMessages();

        train_getMessages(resources, messages);
        stub_contains(messages, false);

        replay();

        BeanEditorModel model = _source.create(SimpleBean.class, resources);

        assertEquals(model.getPropertyNames(), Arrays.asList("age", "firstName", "lastName"));

        PropertyEditModel age = model.get("age");

        assertEquals(age.getLabel(), "Age");
        assertSame(age.getPropertyType(), int.class);
        assertEquals(age.getEditorType(), "text");

        PropertyEditModel firstName = model.get("firstName");

        assertEquals(firstName.getLabel(), "First Name");
        assertEquals(firstName.getPropertyType(), String.class);
        assertEquals(firstName.getEditorType(), "text");

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
    public void non_text_property()
    {
        ComponentResources resources = newComponentResources();
        Messages messages = newMessages();

        train_getMessages(resources, messages);
        stub_contains(messages, false);

        replay();

        BeanEditorModel model = _source.create(EnumBean.class, resources);

        assertEquals(model.getPropertyNames(), Arrays.asList("token"));

        assertEquals(model.get("token").getEditorType(), "enum");

        verify();
    }

    @Test
    public void add_duplicate_property_name_is_failure()
    {
        ComponentResources resources = newComponentResources();
        Messages messages = newMessages();

        train_getMessages(resources, messages);
        stub_contains(messages, false);

        replay();

        BeanEditorModel model = _source.create(SimpleBean.class, resources);

        try
        {
            model.add("age");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Bean editor model for org.apache.tapestry.internal.services.SimpleBean already contains a property model for property \'age\'.");
        }

        verify();
    }

    @Test
    public void unknown_property_name()
    {
        ComponentResources resources = newComponentResources();
        Messages messages = newMessages();

        train_getMessages(resources, messages);
        stub_contains(messages, false);

        replay();

        BeanEditorModel model = _source.create(SimpleBean.class, resources);

        try
        {
            model.get("frobozz");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Bean editor model for org.apache.tapestry.internal.services.SimpleBean does not contain a property named \'frobozz\'.  "
                            + "Available properties: age, firstName, lastName.");
        }

        verify();
    }

    /**
     * You can add anything you like as a property, but you'll have to fill in details such as type
     * and conduit.
     */
    @Test
    public void default_values_for_missing_property()
    {
        ComponentResources resources = newComponentResources();
        Messages messages = newMessages();

        train_getMessages(resources, messages);
        stub_contains(messages, false);

        replay();

        BeanEditorModel model = _source.create(SimpleBean.class, resources);

        PropertyEditModel pm = model.add("frobozz");

        assertEquals(pm.getLabel(), "Frobozz");
        assertEquals(pm.getOrder(), 0);
        assertNull(pm.getConduit());
        assertSame(pm.getPropertyType(), Object.class);

        verify();
    }

    @Test
    public void order_via_annotation()
    {
        ComponentResources resources = newComponentResources();
        Messages messages = newMessages();

        train_getMessages(resources, messages);
        stub_contains(messages, false);

        replay();

        BeanEditorModel model = _source.create(StoogeBean.class, resources);

        assertEquals(model.getPropertyNames(), Arrays.asList("larry", "moe", "shemp", "curly"));

        verify();
    }

    @Test
    public void edit_property_label()
    {
        ComponentResources resources = newComponentResources();
        Messages messages = newMessages();

        train_getMessages(resources, messages);
        stub_contains(messages, false);

        replay();

        BeanEditorModel model = _source.create(SimpleBean.class, resources).get("age").label(
                "Decrepitude").model();

        assertEquals(model.get("age").getLabel(), "Decrepitude");

        verify();
    }

    @Test
    public void label_from_component_messages()
    {
        ComponentResources resources = newComponentResources();
        Messages messages = newMessages();

        train_getMessages(resources, messages);
        stub_contains(messages, false);

        train_contains(messages, "age-label", true);
        train_get(messages, "age-label", "Decrepitude");

        replay();

        BeanEditorModel model = _source.create(SimpleBean.class, resources);

        assertEquals(model.get("age").getLabel(), "Decrepitude");

        verify();
    }

    @Test
    public void override_conduit()
    {
        ComponentResources resources = newComponentResources();
        Messages messages = newMessages();
        PropertyConduit conduit = newMock(PropertyConduit.class);

        train_getMessages(resources, messages);
        stub_contains(messages, false);

        replay();

        BeanEditorModel model = _source.create(SimpleBean.class, resources).get("age").conduit(
                conduit).model();

        assertSame(model.get("age").getConduit(), conduit);

        verify();
    }
}
