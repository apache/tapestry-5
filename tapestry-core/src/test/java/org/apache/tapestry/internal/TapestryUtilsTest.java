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

package org.apache.tapestry.internal;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.tapestry.OptionModel;
import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.beaneditor.Order;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TapestryUtilsTest extends InternalBaseTestCase
{
    private ClassFactory _classFactory;

    private PropertyAccess _access;

    @BeforeClass
    public void setup()
    {
        _classFactory = getObject("service:tapestry.ioc.ClassFactory", ClassFactory.class);
        _access = getObject("service:tapestry.ioc.PropertyAccess", PropertyAccess.class);
    }

    @AfterClass
    public void cleanup()
    {
        _access = null;
        _classFactory = null;
    }

    @Test
    public void close_null_is_noop()
    {
        TapestryUtils.close(null);
    }

    @Test
    public void close_success() throws Exception
    {
        Closeable c = newMock(Closeable.class);

        c.close();

        replay();

        TapestryUtils.close(c);

        verify();
    }

    @Test
    public void close_ignores_exceptions() throws Exception
    {
        Closeable c = newMock(Closeable.class);

        c.close();
        setThrowable(new IOException());

        replay();

        TapestryUtils.close(c);

        verify();
    }

    @Test(dataProvider = "decapitalize_inputs")
    public void decapitalize(String input, String expected)
    {
        assertEquals(TapestryUtils.decapitalize(input), expected);
    }

    @DataProvider(name = "decapitalize_inputs")
    public Object[][] decaptialize_inputs()
    {
        return new Object[][]
        {
        { "Alpha", "alpha" },
        { "beta", "beta" },
        { "A", "a" },
        { "z", "z" },
        { "0abc", "0abc" } };
    }

    @Test(dataProvider = "to_user_presentable")
    public void to_user_presentable(String input, String expected)
    {
        assertEquals(TapestryUtils.toUserPresentable(input), expected);
    }

    @DataProvider(name = "to_user_presentable")
    public Object[][] to_user_presentable_data()
    {
        return new Object[][]
        {
        { "hello", "Hello" },
        { "userId", "User Id" },
        { "useHTML", "Use HTML" },
        { "underscored_name", "Underscored Name" }, };
    }

    @Test
    public void map_from_keys_and_values()
    {
        Map<String, String> map = TapestryUtils.mapFromKeysAndValues(
                "fred",
                "flintstone",
                "barney",
                "rubble");

        assertEquals(map.size(), 2);
        assertEquals(map.get("fred"), "flintstone");
        assertEquals(map.get("barney"), "rubble");
    }

    @Test
    public void string_to_option_model_just_label()
    {
        OptionModel model = TapestryUtils.toOptionModel("Just A Label");

        assertEquals(model.getLabel(), "Just A Label");
        assertEquals(model.getValue(), "Just A Label");
    }

    @Test
    public void string_to_option_model()
    {
        OptionModel model = TapestryUtils.toOptionModel("my-value=Some Label");

        assertEquals(model.getLabel(), "Some Label");
        assertEquals(model.getValue(), "my-value");
    }

    @Test
    public void string_to_option_models()
    {
        List<OptionModel> options = TapestryUtils.toOptionModels("UK,USA,DE=Germany");

        assertEquals(options.size(), 3);

        assertEquals(options.get(0).getLabel(), "UK");
        assertEquals(options.get(0).getValue(), "UK");

        assertEquals(options.get(1).getLabel(), "USA");
        assertEquals(options.get(1).getValue(), "USA");

        assertEquals(options.get(2).getLabel(), "Germany");
        assertEquals(options.get(2).getValue(), "DE");
    }

    @Test
    public void map_entry_to_option_model()
    {
        Map<String, String> map = Collections.singletonMap("key", "value");
        Map.Entry entry = map.entrySet().iterator().next();
        OptionModel model = TapestryUtils.toOptionModel(entry);

        assertEquals(model.getLabel(), "value");
        assertEquals(model.getValue(), "key");
    }

    @Test
    public void map_to_option_models()
    {
        Map<Integer, String> map = new TreeMap<Integer, String>();
        map.put(1, "A");
        map.put(2, null);
        map.put(3, "C");

        List<OptionModel> options = TapestryUtils.toOptionModels(map);

        assertEquals(options.size(), 3);

        assertEquals(options.get(0).getLabel(), "A");
        assertEquals(options.get(0).getValue(), "1");

        assertEquals(options.get(1).getLabel(), "");
        assertEquals(options.get(1).getValue(), "2");

        assertEquals(options.get(2).getLabel(), "C");
        assertEquals(options.get(2).getValue(), "3");
    }

    @Test
    public void whitespace_around_terms_is_trimmed()
    {
        List<OptionModel> options = TapestryUtils.toOptionModels(" UK , USA , DE=Germany ");

        assertEquals(options.size(), 3);

        assertEquals(options.get(0).getLabel(), "UK");
        assertEquals(options.get(0).getValue(), "UK");

        assertEquals(options.get(1).getLabel(), "USA");
        assertEquals(options.get(1).getValue(), "USA");

        assertEquals(options.get(2).getLabel(), "Germany");
        assertEquals(options.get(2).getValue(), "DE");
    }

    @Test
    public void string_to_select_model_type_coercion_integration()
    {
        TypeCoercer coercer = getService(TypeCoercer.class);

        SelectModel selectModel = coercer.coerce(" UK , USA , DE=Germany ", SelectModel.class);

        assertNull(selectModel.getOptionGroups());
        assertEquals(selectModel.getOptions().size(), 3);

        // Waste of effort to re-test each individual option model.
    }

    @Test
    public void parse_key_value()
    {
        KeyValue kv = TapestryUtils.parseKeyValue("foo=bar");

        assertEquals(kv.getKey(), "foo");
        assertEquals(kv.getValue(), "bar");
    }

    @Test
    public void bad_format_key_value_pair()
    {
        String input = "abraxas";

        try
        {
            TapestryUtils.parseKeyValue(input);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), InternalMessages.badKeyValue(input));
        }
    }

    @Test
    public void whitespace_trimmed_for_key_value()
    {
        KeyValue kv = TapestryUtils.parseKeyValue("  mykey = myvalue ");

        assertEquals(kv.getKey(), "mykey");
        assertEquals(kv.getValue(), "myvalue");
    }

    @Test
    public void default_order_no_annotation()
    {
        PropertyConduit conduit = newPropertyConduit();

        train_getAnnotation(conduit, Order.class, null);

        replay();

        assertEquals(TapestryUtils.defaultOrder(conduit), 0);

        verify();
    }

    @Test
    public void default_order_with_annotation()
    {
        PropertyConduit conduit = newPropertyConduit();
        Order order = newMock(Order.class);

        train_getAnnotation(conduit, Order.class, order);

        expect(order.value()).andReturn(99);

        replay();

        assertEquals(TapestryUtils.defaultOrder(conduit), 99);

        verify();
    }

    @Test
    public void extract_id_from_property_expression()
    {
        assertEquals(TapestryUtils.extractIdFromPropertyExpression("simpleName"), "simpleName");
        assertEquals(
                TapestryUtils.extractIdFromPropertyExpression("complex.name().withStuff"),
                "complexnamewithStuff");
        assertEquals(
                TapestryUtils.extractIdFromPropertyExpression("number99.withABullet"),
                "number99withABullet");
    }

    @Test
    public void default_label_key_found()
    {
        Messages messages = newMessages();
        train_contains(messages, "myid-label", true);
        train_get(messages, "myid-label", "My Id");

        replay();

        assertEquals(TapestryUtils.defaultLabel("myid", messages, "myid-name-not-used"), "My Id");

        verify();
    }

    @Test
    public void default_label_from_name()
    {
        Messages messages = newMessages();

        stub_contains(messages, false);

        replay();

        assertEquals(
                TapestryUtils.defaultLabel("foobarbazbiff", messages, "foo.bar().baz.biff()"),
                "Biff");

        verify();
    }

    @Test
    public void property_order_basic()
    {
        ClassPropertyAdapter adapter = _access.getAdapter(DataBean.class);

        List<String> names = adapter.getPropertyNames();

        names.remove("class");

        List<String> sorted = TapestryUtils.orderProperties(adapter, _classFactory, names);

        assertEquals(sorted, Arrays.asList("firstName", "lastName", "age"));
    }

    @Test
    public void property_order_on_subclass()
    {
        ClassPropertyAdapter adapter = _access.getAdapter(DataBeanSubclass.class);

        List<String> names = adapter.getPropertyNames();

        names.remove("class");

        List<String> sorted = TapestryUtils.orderProperties(adapter, _classFactory, names);

        // Subclass properties listed after superclass properties, as desired.

        assertEquals(sorted, Arrays.asList(
                "firstName",
                "lastName",
                "age",
                "street",
                "city",
                "state",
                "zip"));
    }

    @Test
    public void properties_with_order_annotation_filtered()
    {
        ClassPropertyAdapter adapter = _access.getAdapter(PropertyOrderBean.class);

        List<String> names = adapter.getPropertyNames();

        names.remove("class");

        List<String> sorted = TapestryUtils.orderProperties(adapter, _classFactory, names);

        // Property third has an explicit @Order

        assertEquals(sorted, Arrays.asList("first", "second"));
    }
}
