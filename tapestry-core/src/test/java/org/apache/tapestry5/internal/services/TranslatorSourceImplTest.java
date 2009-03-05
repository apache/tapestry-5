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

import org.apache.tapestry5.Field;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.translator.BigDecimalNumericFormatter;
import org.apache.tapestry5.internal.translator.BigIntegerNumericFormatter;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.TranslatorSource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;

public class TranslatorSourceImplTest extends InternalBaseTestCase
{
    private TranslatorSource source;

    @BeforeClass
    public void setup()
    {
        source = getService(TranslatorSource.class);

        getService(ThreadLocale.class).setLocale(Locale.ENGLISH);
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


    @DataProvider
    public Object[][] to_client_data()
    {
        return new Object[][] {

                { Byte.class, (byte) 65, "65" },

                { Integer.class, 997, "997" },

                { Long.class, 12345l, "12345" },

                // Is this a bug?  We seem to be using a JDK- or locale-defined level of precision.
                // Maybe translators need room for configuration just like validators, so that
                // the correct decimal format string could be specified in the message catalog.

                { Double.class, 3.1428571429d, "3.143" },

                { String.class, "abcd", "abcd" },

                { Short.class, (short) 95, "95" },

                { Float.class, (float) -22.7, "-22.7" },

                { BigInteger.class, new BigInteger("123456789012345678901234567890"),
                        "123456789012345678901234567890" },

                { BigDecimal.class, new BigDecimal("-9876543219876543321987654321.12345123451234512345"),
                        "-9876543219876543321987654321.12345123451234512345" }
        };
    }

    @Test(dataProvider = "to_client_data")
    public void to_client(Class type, Object value, String expected)
    {
        Translator t = source.getByType(type);

        String actual = t.toClient(value);

        assertEquals(actual, expected);
    }

    @DataProvider
    public Object[][] parse_client_success_data()
    {
        return new Object[][] {

                { Byte.class, " 23 ", (byte) 23 },

                { Short.class, " -121 ", (short) -121 },

                { Integer.class, " 123 ", 123 },

                { Integer.class, " 20,000 ", 20000 },

                { Long.class, "  -1234567 ", -1234567l },

                { Double.class, "3.1428571429", 3.1428571429d },

                { String.class, " abcdef ", " abcdef " },

                { Float.class, " 28.95 ", (float) 28.95 },

                { BigInteger.class, " -123456789012345678901234567890",
                        new BigInteger("-123456789012345678901234567890") },

                { BigDecimal.class, "-9,876,543,219,876,543,321,987,654,321.12345123451234512345",
                        new BigDecimal("-9876543219876543321987654321.12345123451234512345") }
        };
    }

    @Test(dataProvider = "parse_client_success_data")
    public void parse_client(Class type, String input, Object expected) throws Exception
    {
        Translator t = source.getByType(type);

        Object actual = t.parseClient(null, input, null);

        assertEquals(actual, expected);
    }

    @DataProvider
    public Object[][] parse_client_failure_data()
    {
        String intError = "You must provide an integer value for Fred.";
        String floatError = "You must provide a numeric value for Fred.";

        return new Object[][] {

                { Byte.class, "fred", intError },

                { Integer.class, "fred", intError },

                { Long.class, "fred", intError },

                { Double.class, "fred", floatError },

                { Float.class, "fred", floatError },

                { Short.class, "fred", intError }
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

    @Test
    public void biginteger_with_localized_symbols() throws ParseException
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setGroupingSeparator('_');
        symbols.setMinusSign('*');

        BigIntegerNumericFormatter f = new BigIntegerNumericFormatter(symbols);

        BigInteger big = new BigInteger("-123456");

        assertEquals(f.parse("*123_456"), big);

        assertEquals(f.toClient(big), "*123456");
    }

    @Test
    public void bigdecimal_with_localized_symbols() throws ParseException
    {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.ENGLISH);
        symbols.setGroupingSeparator('_');
        symbols.setMinusSign('*');
        symbols.setDecimalSeparator('#');

        BigDecimalNumericFormatter f = new BigDecimalNumericFormatter(symbols);

        BigDecimal big = new BigDecimal("-123456.797956563434");

        assertEquals(f.parse("*123_456#797956563434"), big);

        assertEquals(f.toClient(big), "*123456#797956563434");
    }


}
