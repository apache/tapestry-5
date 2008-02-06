// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.Translator;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.services.TranslatorSource;
import org.apache.tapestry.services.ValidationMessagesSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public class TranslatorSourceImplTest extends InternalBaseTestCase
{
    private TranslatorSource _source;
    private ValidationMessagesSource _messagesSource;

    @BeforeClass
    public void setup()
    {
        _source = getService(TranslatorSource.class);
        _messagesSource = getService(ValidationMessagesSource.class);
    }


    @Test
    public void found_translator_by_name()
    {
        Translator translator = mockTranslator();

        train_getType(translator, String.class);

        Map<String, Translator> configuration = Collections.singletonMap("mock", translator);

        replay();

        TranslatorSource source = new TranslatorSourceImpl(configuration);

        assertSame(source.get("mock"), translator);

        verify();
    }

    protected final void train_getType(Translator translator, Class type)
    {
        expect(translator.getType()).andReturn(type).atLeastOnce();
    }

    @Test
    public void unknown_translator_is_failure()
    {
        Translator fred = mockTranslator();
        Translator barney = mockTranslator();

        train_getType(fred, Long.class);
        train_getType(barney, String.class);

        Map<String, Translator> configuration = CollectionFactory.newMap();

        configuration.put("fred", fred);
        configuration.put("barney", barney);

        replay();

        TranslatorSource source = new TranslatorSourceImpl(configuration);

        try
        {
            source.get("wilma");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Unknown translator type 'wilma'.  Configured translators are barney, fred.");
        }

    }


    @DataProvider(name = "to_client_data")
    public Object[][] to_client_data()
    {
        return new Object[][]{

                {Byte.class, (byte) 65, "65"},

                {Integer.class, 997, "997"},

                {Long.class, 12345l, "12345"},

                {Double.class, 123.45d, "123.45"},

                {String.class, "abcd", "abcd"},

                {Float.class, (float) -22.7, "-22.7"}

        };
    }

    @Test(dataProvider = "to_client_data")
    public void to_client(Class type, Object value, String expected)
    {
        Translator t = _source.getByType(type);

        String actual = t.toClient(value);

        assertEquals(actual, expected);
    }

    @DataProvider(name = "parse_client_success_data")
    public Object[][] parse_client_success_data()
    {
        return new Object[][]{

                {Byte.class, " 23 ", (byte) 23},

                {Integer.class, " 123 ", 123},

                {Long.class, "  -1234567 ", -1234567l},

                {Double.class, " 3.14 ", 3.14d},

                {String.class, " abcdef ", " abcdef "},

                {Float.class, " 28.95 ", (float) 28.95},

        };
    }

    @Test(dataProvider = "parse_client_success_data")
    public void parse_client(Class type, String input, Object expected) throws Exception
    {
        Translator t = _source.getByType(type);

        Object actual = t.parseClient(input, _messagesSource.getValidationMessages(Locale.ENGLISH));

        assertEquals(actual, expected);
    }

    @DataProvider(name = "parse_client_failure_data")
    public Object[][] parse_client_failure_data()
    {
        return new Object[][]{

                {Byte.class, "fred", "The input value 'fred' is not parseable as an integer value."},

                {Integer.class, "fred", "The input value 'fred' is not parseable as an integer value."},

                {Long.class, "fred", "The input value 'fred' is not parseable as an integer value."},

                {Double.class, "fred", "The input value 'fred' is not parseable as a numeric value."},

                {Float.class, "fred", "The input value 'fred' is not parseable as a numeric value."}

        };
    }

    @Test(dataProvider = "parse_client_failure_data")
    public void parse_client_failure(Class type, String input, String expectedMessage)
    {

        Translator t = _source.getByType(type);

        try
        {
            t.parseClient(input, _messagesSource.getValidationMessages(Locale.ENGLISH));
            unreachable();
        }
        catch (ValidationException ex)
        {
            assertEquals(ex.getMessage(), expectedMessage);
        }
    }

    @Test
    public void find_by_type()
    {
        Translator t = mockTranslator();
        Map<String, Translator> configuration = CollectionFactory.newMap();

        configuration.put("string", t);

        train_getType(t, String.class);

        replay();

        TranslatorSource source = new TranslatorSourceImpl(configuration);

        assertSame(source.getByType(String.class), t);
        assertSame(source.findByType(String.class), t);
        assertNull(source.findByType(Integer.class));

        verify();
    }

    @Test
    public void get_by_type_not_found()
    {
        Translator string = mockTranslator();
        Translator bool = mockTranslator();

        Map<String, Translator> configuration = CollectionFactory.newMap();

        configuration.put("string", string);
        configuration.put("boolean", bool);

        train_getType(string, String.class);
        train_getType(bool, Boolean.class);

        replay();

        TranslatorSource source = new TranslatorSourceImpl(configuration);

        try
        {
            source.getByType(Integer.class);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "No translator is defined for type java.lang.Integer.  Registered types: java.lang.Boolean, java.lang.String.");
        }

        verify();
    }
}
