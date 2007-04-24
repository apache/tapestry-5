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

package org.apache.tapestry.ioc.internal.services;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.XMLReader;

public class TypeCoercerImplTest extends IOCInternalTestCase
{
    private TypeCoercer _coercer;

    @BeforeClass
    public void setup_coercer()
    {
        _coercer = getService(TypeCoercer.class);
    }

    @AfterClass
    public void cleanup_coercer()
    {
        _coercer = null;
    }

    @Test
    public void builtin_coercion()
    {
        // String to Double

        assertEquals(_coercer.coerce("-15", Double.class), new Double(-15));

        // Now a second pass through, to exercise the internal cache

        assertEquals(_coercer.coerce("2.27", Double.class), new Double(2.27));
    }

    @Test
    public void primitive_type_as_target()
    {
        assertEquals(_coercer.coerce(227l, int.class), new Integer(227));
    }

    @Test
    public void no_coercion_necessary()
    {
        Object input = new Integer(-37);

        assertSame(_coercer.coerce(input, Number.class), input);
    }

    @Test
    public void combined_coercion()
    {
        StringBuilder builder = new StringBuilder("12345");

        // This should trigger Object -> String, String -> Integer

        assertEquals(_coercer.coerce(builder, int.class), new Integer(12345));

        // This should trigger String -> Double, Number -> Integer

        assertEquals(_coercer.coerce("52", Integer.class), new Integer(52));
    }

    @Test
    public void no_coercion_found()
    {
        try
        {
            _coercer.coerce("", Map.class);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue(ex.getMessage().contains(
                    "Could not find a coercion from type java.lang.String to type java.util.Map"));
        }
    }

    @Test
    public void coercion_failure()
    {
        try
        {
            _coercer.coerce(Collections.EMPTY_MAP, Float.class);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertTrue(ex
                    .getMessage()
                    .contains(
                            "Coercion of {} to type java.lang.Float (via Object --> String, String --> Double, Double --> Float) failed"));
            assertTrue(ex.getCause() instanceof NumberFormatException);
        }
    }

    @SuppressWarnings("unchecked")
    @Test(dataProvider = "coercions_inputs")
    public void builtin_coercions(Object input, Class targetType, Object expected)
    {
        Object actual = _coercer.coerce(input, targetType);

        assertEquals(actual, expected);
    }

    @DataProvider(name = "coercions_inputs")
    public Object[][] coercions_inputs()
    {
        String bigDecimalValue = "12345656748352435842385234598234958234574358723485.35843534285293857298457234587";
        String bigIntegerValue = "12384584574874385743";

        Object object = new Object();
        // Over time, some of these may evolve from testing specific tuples to
        // compound tuples (built around specific tuples).

        Float floatValue = new Float(31.14);
        return new Object[][]
        {
        // There's a lot of these!

                { this, String.class, toString() },
                { 55l, Integer.class, 55 },
                { "", Boolean.class, false },
                { "  ", Boolean.class, false },
                { "x", Boolean.class, true },
                { " z ", Boolean.class, true },
                { "false", Boolean.class, false },
                { "  False ", Boolean.class, false },
                { null, Boolean.class, false },
                { new Double(256), Integer.class, new Integer(256) },
                { new Double(22.7), Integer.class, new Integer(22) },
                { new Integer(0), Boolean.class, false },
                { new Long(32838), Boolean.class, true },
                { new Integer(127), Byte.class, new Byte("127") },
                { new Double(58), Short.class, new Short("58") },
                { new Integer(33), Long.class, new Long(33) },
                { new Integer(22), Float.class, new Float(22) },
                { new Integer(1234), Double.class, new Double(1234) },
                { floatValue, Double.class, floatValue.doubleValue() },
                { Collections.EMPTY_LIST, Boolean.class, false },
                { Collections.singleton(this), Boolean.class, true },
                { bigDecimalValue, BigDecimal.class, new BigDecimal(bigDecimalValue) },
                { new BigDecimal(bigDecimalValue), Double.class, 1.2345656748352436E49 },
                { bigIntegerValue, BigInteger.class, new BigInteger(bigIntegerValue) },
                { new BigInteger("12345678"), Long.class, 12345678l },
                { -12345678l, BigInteger.class, new BigInteger("-12345678") },
                { object, List.class, Collections.singletonList(object) },
                { null, Iterable.class, null },
                { null, List.class, null },
                { null, Collection.class, null },
                { null, String.class, null },
                { new Object[]
                { "a", 123 }, List.class, Arrays.asList("a", 123) },
                { new String[]
                { "a", "b" }, List.class, Arrays.asList("a", "b") },

                // null to arbitrary object is still null

                { null, XMLReader.class, null } };
    }

    @Test(dataProvider = "explain_inputs")
    public <S, T> void explain(Class<S> inputType, Class<T> outputType, String expected)
    {
        assertEquals(_coercer.explain(inputType, outputType), expected);
    }

    @DataProvider(name = "explain_inputs")
    public Object[][] explain_inputs()
    {
        return new Object[][]
        {
                { StringBuffer.class, Integer.class,
                        "Object --> String, String --> Long, Long --> Integer" },
                { void.class, Map.class, "null --> null" },
                { void.class, Boolean.class, "null --> Boolean" },
                { String[].class, List.class, "Object[] --> java.util.List" },
                { Float.class, Double.class, "Float --> Double" },
                { Double.class, BigDecimal.class,
                        "Object --> String, String --> java.math.BigDecimal" },

        };
    }

}
