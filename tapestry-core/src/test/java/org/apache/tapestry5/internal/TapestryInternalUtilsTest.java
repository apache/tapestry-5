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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.*;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.runtime.ComponentResourcesAware;
import org.apache.tapestry5.services.BeanModelSource;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;
import java.util.regex.Pattern;

public class TapestryInternalUtilsTest extends InternalBaseTestCase
{
    private ClassFactory classFactory;

    private PropertyAccess access;

    private BeanModelSource beanModelSource;

    @BeforeClass
    public void setup()
    {
        classFactory = getService("ClassFactory", ClassFactory.class);
        access = getService("PropertyAccess", PropertyAccess.class);
        beanModelSource = getService(BeanModelSource.class);
    }

    @AfterClass
    public void cleanup()
    {
        access = null;
        classFactory = null;
        beanModelSource = null;
    }


    @Test(dataProvider = "to_user_presentable_data")
    public void to_user_presentable(String input, String expected)
    {
        assertEquals(TapestryInternalUtils.toUserPresentable(input), expected);
    }

    @DataProvider
    public Object[][] to_user_presentable_data()
    {
        return new Object[][] { { "hello", "Hello" }, { "userId", "User Id" }, { "useHTML", "Use HTML" },
                { "underscored_name", "Underscored Name" }, };
    }

    @Test
    public void map_from_keys_and_values()
    {
        Map<String, String> map = TapestryInternalUtils.mapFromKeysAndValues("fred", "flintstone", "barney", "rubble");

        assertEquals(map.size(), 2);
        assertEquals(map.get("fred"), "flintstone");
        assertEquals(map.get("barney"), "rubble");
    }

    @Test
    public void string_to_option_model_just_label()
    {
        OptionModel model = TapestryInternalUtils.toOptionModel("Just A Label");

        assertEquals(model.getLabel(), "Just A Label");
        assertEquals(model.getValue(), "Just A Label");
    }

    @Test
    public void string_to_option_model()
    {
        OptionModel model = TapestryInternalUtils.toOptionModel("my-value=Some Label");

        assertEquals(model.getLabel(), "Some Label");
        assertEquals(model.getValue(), "my-value");
    }

    @Test
    public void string_to_option_models()
    {
        List<OptionModel> options = TapestryInternalUtils.toOptionModels("UK,USA,DE=Germany");

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
        OptionModel model = TapestryInternalUtils.toOptionModel(entry);

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

        List<OptionModel> options = TapestryInternalUtils.toOptionModels(map);

        assertEquals(options.size(), 3);

        assertEquals(options.get(0).getLabel(), "A");
        assertEquals(options.get(0).getValue(), 1);

        assertEquals(options.get(1).getLabel(), "");
        assertEquals(options.get(1).getValue(), 2);

        assertEquals(options.get(2).getLabel(), "C");
        assertEquals(options.get(2).getValue(), 3);
    }

    @Test
    public void null_map_key_is_null_option_value()
    {

        Map<Integer, String> map = new HashMap<Integer, String>();
        map.put(null, "Label");

        List<OptionModel> options = TapestryInternalUtils.toOptionModels(map);

        assertEquals(options.size(), 1);

        assertEquals(options.get(0).getLabel(), "Label");
        assertEquals(options.get(0).getValue(), null);
    }

    @Test
    public void object_to_option_model()
    {
        Object object = new Integer(27);
        OptionModel model = TapestryInternalUtils.toOptionModel(object);

        assertEquals(model.getLabel(), "27");
        assertEquals(model.getValue(), object);
    }

    @Test
    public void list_to_option_models()
    {
        List<String> list = new ArrayList<String>();
        list.add("A");
        list.add(null);
        list.add("C");

        List<OptionModel> options = TapestryInternalUtils.toOptionModels(list);

        assertEquals(options.size(), 3);

        assertEquals(options.get(0).getLabel(), "A");
        assertEquals(options.get(0).getValue(), "A");

        assertEquals(options.get(1).getLabel(), "");
        assertEquals(options.get(1).getValue(), null);

        assertEquals(options.get(2).getLabel(), "C");
        assertEquals(options.get(2).getValue(), "C");
    }

    @Test
    public void whitespace_around_terms_is_trimmed()
    {
        List<OptionModel> options = TapestryInternalUtils.toOptionModels(" UK , USA , DE=Germany ");

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
        KeyValue kv = TapestryInternalUtils.parseKeyValue("foo=bar");

        assertEquals(kv.getKey(), "foo");
        assertEquals(kv.getValue(), "bar");
    }

    @Test
    public void bad_format_key_value_pair()
    {
        String input = "abraxas";

        try
        {
            TapestryInternalUtils.parseKeyValue(input);
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
        KeyValue kv = TapestryInternalUtils.parseKeyValue("  mykey = myvalue ");

        assertEquals(kv.getKey(), "mykey");
        assertEquals(kv.getValue(), "myvalue");
    }

    @Test
    public void extract_id_from_property_expression()
    {
        assertEquals(TapestryInternalUtils.extractIdFromPropertyExpression("simpleName"), "simpleName");
        assertEquals(TapestryInternalUtils.extractIdFromPropertyExpression("complex.name().withStuff"),
                     "complexnamewithStuff");
        assertEquals(TapestryInternalUtils.extractIdFromPropertyExpression("number99.withABullet"),
                     "number99withABullet");
    }

    @Test
    public void default_label_key_found()
    {
        Messages messages = mockMessages();
        train_contains(messages, "myid-label", true);
        train_get(messages, "myid-label", "My Id");

        replay();

        assertEquals(TapestryInternalUtils.defaultLabel("myid", messages, "myid-name-not-used"), "My Id");

        verify();
    }

    @Test
    public void default_label_from_name()
    {
        Messages messages = mockMessages();

        stub_contains(messages, false);

        replay();

        assertEquals(TapestryInternalUtils.defaultLabel("foobarbazbiff", messages, "foo.bar().baz.biff()"), "Biff");

        verify();
    }

    @Test
    public void null_equals_null()
    {
        assertTrue(TapestryInternalUtils.isEqual(null, null));
    }

    @Test
    public void non_null_never_equals_null()
    {
        assertFalse(TapestryInternalUtils.isEqual(this, null));
    }

    @Test
    public void same_is_equal()
    {
        assertTrue(TapestryInternalUtils.isEqual(this, this));
    }

    @Test
    public void is_equal_with_objects()
    {
        String left = "left";
        String right = "right";

        assertFalse(TapestryInternalUtils.isEqual(left, right));
        assertTrue(TapestryInternalUtils.isEqual(left, new String(left)));
    }

    @Test
    public void type_coersion_string_to_pattern()
    {
        TypeCoercer coercer = getObject(TypeCoercer.class, null);

        String input = "\\s+";

        Pattern pattern = coercer.coerce(input, Pattern.class);

        assertEquals(pattern.toString(), input);
    }

    @Test
    public void type_coersion_from_component_resources_aware_to_component_resources()
    {
        ComponentResourcesAware input = newMock(ComponentResourcesAware.class);
        ComponentResources resources = mockComponentResources();

        expect(input.getComponentResources()).andReturn(resources);

        TypeCoercer coercer = getObject(TypeCoercer.class, null);

        replay();

        ComponentResources actual = coercer.coerce(input, ComponentResources.class);

        assertSame(actual, resources);

        verify();
    }

    @Test
    public void to_class_attribute_value_empty()
    {
        List<String> classes = Collections.emptyList();

        assertNull(TapestryInternalUtils.toClassAttributeValue(classes));
    }

    @Test
    public void to_class_attribute_value_normal()
    {
        List<String> classes = CollectionFactory.newList("fred", "barney", "wilma");

        assertEquals(TapestryInternalUtils.toClassAttributeValue(classes), "fred barney wilma");
    }

    @DataProvider
    public Object[][] split_at_commas_data()
    {
        return new Object[][]
                {
                        { "foo", new String[] { "foo" } },
                        { "foo, bar", new String[] { "foo", "bar" } },
                        { "  foo, \nbar\t\t", new String[] { "foo", "bar" } },
                        { "", new String[0] },
                        { null, new String[0] }
                };
    }


    @Test(dataProvider = "split_at_commas_data")
    public void split_at_commas(String input, String[] output)
    {
        assertArraysEqual(TapestryInternalUtils.splitAtCommas(input), output);

    }

    @DataProvider
    public Object[][] to_base64_data()
    {
        return new Object[][] {
                { 0L, "AA" },
                { 1L, "AQ" },
                { 0xab54a98ceb1f0ad2L, "q1SpjOsfCtI" }
        };
    }

    @Test
    public void to_asset2_no_wrapper_needed()
    {
        Asset2 asset2 = mockAsset2();

        replay();

        assertSame(TapestryInternalUtils.toAsset2(asset2), asset2);

        verify();
    }

    @Test
    public void asset_to_asset2_wrapper()
    {
        Asset asset = mockAsset();
        Resource resource = mockResource();
        String clientURL = "clientURL";

        train_toClientURL(asset, clientURL);

        expect(asset.getResource()).andReturn(resource);

        replay();

        Asset2 asset2 = TapestryInternalUtils.toAsset2(asset);

        assertFalse(asset2.isInvariant());

        assertSame(asset2.toClientURL(), clientURL);
        assertSame(asset2.toString(), asset.toString());
        assertSame(asset2.getResource(), resource);

        verify();
    }
}
