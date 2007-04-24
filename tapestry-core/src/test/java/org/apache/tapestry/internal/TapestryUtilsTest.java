// Copyright 2006 The Apache Software Foundation
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
import java.util.List;
import java.util.Map;

import org.apache.tapestry.OptionModel;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TapestryUtilsTest extends InternalBaseTestCase
{
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
    public void to_option_models()
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
}
