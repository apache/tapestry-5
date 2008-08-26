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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Field;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.TranslatorSource;
import org.apache.tapestry5.services.ValidationMessagesSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collection;

public class TranslatorSourceImplTest extends InternalBaseTestCase
{
    private TranslatorSource source;

    private ValidationMessagesSource messagesSource;

    @BeforeClass
    public void setup()
    {
        source = getService(TranslatorSource.class);
        messagesSource = getService(ValidationMessagesSource.class);
    }


    @Test
    public void found_translator_by_name()
    {
        Translator translator = mockTranslator("mock", String.class);

        Collection<Translator> configuration = CollectionFactory.newList(translator);

        replay();

        TranslatorSource source = new TranslatorSourceImpl(configuration);

        assertSame(source.get("mock"), translator);

        verify();
    }

    @Test
    public void unknown_translator_is_failure()
    {
        Translator fred = mockTranslator("fred", String.class);
        Translator barney = mockTranslator("barney", Long.class);

        Collection<Translator> configuration = CollectionFactory.newList(fred, barney);

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
        return new Object[][] {

                {Byte.class, (byte) 65, "65"},

                {Integer.class, 997, "997"},

                {Long.class, 12345l, "12345"},

                {Double.class, 123.45d, "123.45"},

                {String.class, "abcd", "abcd"},

                {Short.class, (short) 95, "95"},

                {Float.class, (float) -22.7, "-22.7"}
        };
    }

    @Test(dataProvider = "to_client_data")
    public void to_client(Class type, Object value, String expected)
    {
        Translator t = source.getByType(type);

        String actual = t.toClient(value);

        assertEquals(actual, expected);
    }

    @DataProvider(name = "parse_client_success_data")
    public Object[][] parse_client_success_data()
    {
        return new Object[][] {

                {Byte.class, " 23 ", (byte) 23},

                {Short.class, " -121 ", (short) -121},

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
        Translator t = source.getByType(type);

        Object actual = t.parseClient(null, input, null);

        assertEquals(actual, expected);
    }

    @DataProvider(name = "parse_client_failure_data")
    public Object[][] parse_client_failure_data()
    {
        String intError = "You must provide an integer value for Fred.";
        String floatError = "You must provide a numeric value for Fred.";

        return new Object[][] {

                {Byte.class, "fred", intError},

                {Integer.class, "fred", intError},

                {Long.class, "fred", intError},

                {Double.class, "fred", floatError},

                {Float.class, "fred", floatError},

                {Short.class, "fred", intError}
        };
    }

    @Test(dataProvider = "parse_client_failure_data")
    public void parse_client_failure(Class type, String input, String expectedMessage)
    {
        Translator t = source.getByType(type);
        Field field = mockField();

        replay();

        try
        {
            t.parseClient(field, input, expectedMessage);
            unreachable();
        }
        catch (ValidationException ex)
        {
            assertEquals(ex.getMessage(), expectedMessage);
        }

        verify();
    }

    @Test
    public void find_by_type()
    {
        Translator t = mockTranslator("string", String.class);
        Collection<Translator> configuration = CollectionFactory.newList(t);

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
        Translator string = mockTranslator("string", String.class);
        Translator bool = mockTranslator("bool", Boolean.class);

        Collection<Translator> configuration = CollectionFactory.newList(string, bool);

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
