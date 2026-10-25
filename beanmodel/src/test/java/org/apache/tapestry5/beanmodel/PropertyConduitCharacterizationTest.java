// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.beanmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.tapestry5.beanmodel.internal.services.Invariant;
import org.apache.tapestry5.beanmodel.internal.services.PropertyExpressionException;
import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;
import org.apache.tapestry5.commons.util.IntegerRange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pins down every feature of the property expression language.
 */
public class PropertyConduitCharacterizationTest
{
    private static final String PARSE_ERROR_PREFIX = "Error parsing property expression '%s': ";

    private static PropertyConduitSource source;

    @BeforeAll
    static void setup()
    {
        source = PropertyConduitSourceFixture.create();
    }

    private static PropertyConduit conduit(String expression)
    {
        return source.create(Root.class, expression);
    }

    private static Object get(String expression)
    {
        return conduit(expression).get(new Root());
    }

    private static void assertReadOnly(String expression)
    {
        PropertyConduit conduit = conduit(expression);
        Root root = new Root();

        assertThrows(RuntimeException.class, () -> conduit.set(root, null));
    }

    private static List<Object> list(Object... elements)
    {
        return new ArrayList<>(Arrays.asList(elements));
    }

    private static Map<Object, Object> map(Object... keysAndValues)
    {
        Map<Object, Object> result = new HashMap<>();

        for (int i = 0; i < keysAndValues.length; i += 2)
        {
            result.put(keysAndValues[i], keysAndValues[i + 1]);
        }

        return result;
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("integer literals")
    class IntegerLiterals
    {
        Stream<Arguments> literals()
        {
            return Stream.of(
                    Arguments.of("1", 1L),
                    Arguments.of("0", 0L),
                    Arguments.of("-0", 0L),
                    Arguments.of("007", 7L),
                    Arguments.of("+1", 1L),
                    Arguments.of("-1", -1L),
                    Arguments.of("12345", 12345L));
        }

        @ParameterizedTest
        @MethodSource("literals")
        void evaluates_to_long(String expression, Long expected)
        {
            assertEquals(expected, get(expression));
        }

        @ParameterizedTest
        @MethodSource("literals")
        void has_type_long(String expression, Long expected)
        {
            assertSame(Long.class, conduit(expression).getPropertyType());
        }

        @ParameterizedTest
        @MethodSource("literals")
        void is_read_only(String expression, Long expected)
        {
            assertReadOnly(expression);
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("Decimal literals")
    class DecimalLiterals
    {
        Stream<Arguments> literals()
        {
            return Stream.of(
                    Arguments.of("1.23", 1.23d),
                    Arguments.of("-1.23", -1.23d),
                    Arguments.of("+1.5", 1.5d),
                    Arguments.of(".5", 0.5d),
                    Arguments.of("-.5", -0.5d),
                    Arguments.of("+.5", 0.5d),
                    Arguments.of("1.", 1.0d));
        }

        @ParameterizedTest
        @MethodSource("literals")
        void evaluates_to_double(String expression, Double expected)
        {
            assertEquals(expected, get(expression));
        }

        @ParameterizedTest
        @MethodSource("literals")
        void has_type_double(String expression, Double expected)
        {
            assertSame(Double.class, conduit(expression).getPropertyType());
        }

        @ParameterizedTest
        @MethodSource("literals")
        void is_read_only(String expression, Double expected)
        {
            assertReadOnly(expression);
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("string literals")
    class StringLiterals
    {
        Stream<Arguments> literals()
        {
            return Stream.of(
                    Arguments.of("'hello'", "hello"),
                    Arguments.of("''", ""),
                    Arguments.of("'has space'", "has space"),
                    Arguments.of("'..'", ".."),
                    Arguments.of("'tab\there'", "tab\there"),
                    Arguments.of("'line\nbreak'", "line\nbreak"),
                    Arguments.of("'back\\slash'", "back\\slash"));
        }

        @ParameterizedTest
        @MethodSource("literals")
        void evaluates_to_content_without_quotes(String expression, String expected)
        {
            assertEquals(expected, get(expression));
        }

        @ParameterizedTest
        @MethodSource("literals")
        void has_type_string(String expression, String expected)
        {
            assertSame(String.class, conduit(expression).getPropertyType());
        }

        @ParameterizedTest
        @MethodSource("literals")
        void is_read_only(String expression, String expected)
        {
            assertReadOnly(expression);
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("boolean and null keywords, any case")
    class Keywords
    {
        Stream<Arguments> keywords()
        {
            return Stream.of(
                    Arguments.of("true", Boolean.TRUE, Boolean.class),
                    Arguments.of("TRUE", Boolean.TRUE, Boolean.class),
                    Arguments.of("false", Boolean.FALSE, Boolean.class),
                    Arguments.of("FALSE", Boolean.FALSE, Boolean.class),
                    Arguments.of("null", null, Void.class),
                    Arguments.of("nUlL", null, Void.class));
        }

        @ParameterizedTest
        @MethodSource("keywords")
        void evaluates_to_constant(String expression, Object expected, Class<?> type)
        {
            assertEquals(expected, get(expression));
        }

        @ParameterizedTest
        @MethodSource("keywords")
        void has_constant_type(String expression, Object expected, Class<?> type)
        {
            assertSame(type, conduit(expression).getPropertyType());
        }

        @ParameterizedTest
        @MethodSource("keywords")
        void is_read_only(String expression, Object expected, Class<?> type)
        {
            assertReadOnly(expression);
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("this keyword")
    class This
    {
        @ParameterizedTest
        @ValueSource(strings = {"this", "THIS"})
        void evaluates_to_root_object(String expression)
        {
            Root root = new Root();

            assertSame(root, conduit(expression).get(root));
        }

        @ParameterizedTest
        @ValueSource(strings = {"this", "THIS"})
        void has_root_type(String expression)
        {
            assertSame(Root.class, conduit(expression).getPropertyType());
        }

        @ParameterizedTest
        @ValueSource(strings = {"this", "THIS"})
        void is_read_only(String expression)
        {
            assertReadOnly(expression);
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("identifiers that start like keywords")
    class KeywordPrefixedIdentifiers
    {
        @ParameterizedTest
        @ValueSource(strings = {"nullify", "truest", "thisIsIt"})
        void resolve_as_properties(String expression)
        {
            assertEquals(expression + "-value", get(expression));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("identifier characters")
    class IdentifierCharacters
    {
        @ParameterizedTest
        @ValueSource(strings = {"$dollar", "_under", "a1", "café", "日本語"})
        void are_read_as_properties(String expression)
        {
            assertEquals(expression + "-value", get(expression));
        }

        @ParameterizedTest
        @ValueSource(strings = {"$dollar", "_under", "a1", "café", "日本語"})
        void are_written_as_properties(String expression)
        {
            PropertyConduit conduit = conduit(expression);
            Root root = new Root();

            conduit.set(root, "changed");

            assertEquals("changed", conduit.get(root));
        }

        @Test
        void head_a_chain()
        {
            assertEquals("child-property", get("naïve.property"));
        }

        @Test
        void write_through_a_chain()
        {
            PropertyConduit conduit = conduit("naïve.property");
            Root root = new Root();

            conduit.set(root, "changed");

            assertEquals("changed", root.getNaïve().getProperty());
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("whitespace between tokens")
    class Whitespace
    {
        Stream<Arguments> pairs()
        {
            return Stream.of(
                    Arguments.of("child . name", "child.name"),
                    Arguments.of("child ?. name", "child?.name"),
                    Arguments.of("getValue ()", "getValue()"),
                    Arguments.of("pick( )", "pick()"),
                    Arguments.of("sum( 1 , 2 , 3 )", "sum(1,2,3)"),
                    Arguments.of("! flag", "!flag"),
                    Arguments.of("[ ]", "[]"),
                    Arguments.of("{ }", "{}"),
                    Arguments.of("[ 1 , 2 ]", "[1,2]"),
                    Arguments.of("{ 'a' : 1 }", "{'a':1}"),
                    Arguments.of("1 .. 3", "1..3"),
                    Arguments.of(" number ", "number"),
                    Arguments.of("child\t.\tname", "child.name"),
                    Arguments.of("child\n.\nname", "child.name"),
                    Arguments.of("child\r\n.\r\nname", "child.name"));
        }

        @ParameterizedTest
        @MethodSource("pairs")
        void is_ignored(String spaced, String compact)
        {
            assertEquals(get(compact), get(spaced));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("dereference chains")
    class DereferenceChains
    {
        Stream<Arguments> chains()
        {
            return Stream.of(
                    Arguments.of("child.name", "child-name", String.class),
                    Arguments.of("child.leaf.text", "leaf-text", String.class),
                    Arguments.of("child.size", 3, int.class),
                    Arguments.of("holder.get().name", "held-name", String.class),
                    Arguments.of("child.leaf().inner.describe()", "inner", String.class));
        }

        @ParameterizedTest
        @MethodSource("chains")
        void evaluate_the_last_term(String expression, Object expected, Class<?> type)
        {
            assertEquals(expected, get(expression));
        }

        @ParameterizedTest
        @MethodSource("chains")
        void have_the_type_of_the_last_term(String expression, Object expected, Class<?> type)
        {
            assertSame(type, conduit(expression).getPropertyType());
        }

        @Test
        void write_through_properties()
        {
            Root root = new Root();

            conduit("child.leaf.text").set(root, "changed");

            assertEquals("changed", root.getChild().getLeaf().getText());
        }

        @Test
        void write_through_a_method_result()
        {
            Root root = new Root();

            conduit("holder.get().name").set(root, "changed");

            assertEquals("changed", root.getHolder().get().getName());
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("null inside a '.' chain")
    class NullInDereferenceChain
    {
        @Test
        void read_throws_null_pointer_exception_naming_term_and_expression()
        {
            PropertyConduit conduit = conduit("child.name");
            Root root = new Root();
            root.setChild(null);

            NullPointerException ex = assertThrows(NullPointerException.class, () -> conduit.get(root));

            assertTrue(ex.getMessage().contains("Property 'child' (within property expression 'child.name', of"),
                    ex.getMessage());
            assertTrue(ex.getMessage().endsWith(") is null."), ex.getMessage());
        }

        @Test
        void write_throws_null_pointer_exception()
        {
            PropertyConduit conduit = conduit("child.name");
            Root root = new Root();
            root.setChild(null);

            assertThrows(NullPointerException.class, () -> conduit.set(root, "changed"));
        }

        @Test
        void last_term_may_be_null()
        {
            Root root = new Root();
            root.getChild().setName(null);

            assertNull(conduit("child.name").get(root));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("safe dereference")
    class SafeDereference
    {
        @Test
        void reads_through_non_null_terms()
        {
            assertEquals("leaf-text", get("child?.leaf?.text"));
        }

        @Test
        void yields_null_when_first_term_is_null()
        {
            Root root = new Root();
            root.setChild(null);

            assertNull(conduit("child?.leaf?.text").get(root));
        }

        @Test
        void yields_null_when_middle_term_is_null()
        {
            Root root = new Root();
            root.getChild().setLeaf(null);

            assertNull(conduit("child?.leaf?.text").get(root));
        }

        @Test
        void mixed_chain_yields_null_when_safe_term_is_null()
        {
            Root root = new Root();
            root.getChild().setLeaf(null);

            assertNull(conduit("child.leaf?.text").get(root));
        }

        @Test
        void mixed_chain_throws_when_unsafe_term_is_null()
        {
            PropertyConduit conduit = conduit("child?.leaf.text");
            Root root = new Root();
            root.getChild().setLeaf(null);

            assertThrows(NullPointerException.class, () -> conduit.get(root));
        }

        @Test
        void has_the_type_of_the_last_term()
        {
            assertSame(String.class, conduit("child?.leaf?.text").getPropertyType());
        }

        @Test
        void writes_through_non_null_terms()
        {
            Root root = new Root();

            conduit("child?.name").set(root, "changed");

            assertEquals("changed", root.getChild().getName());
        }

        @Test
        void write_is_ignored_when_a_term_is_null()
        {
            Root root = new Root();
            root.setChild(null);

            conduit("child?.name").set(root, "changed");

            assertNull(root.getChild());
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("null root object")
    class NullRoot
    {
        @Test
        void dereference_throws_null_pointer_exception_naming_the_expression()
        {
            PropertyConduit conduit = conduit("child.name");

            NullPointerException ex = assertThrows(NullPointerException.class, () -> conduit.get(null));

            assertEquals("Root object of property expression 'child.name' is null.", ex.getMessage());
        }

        @Test
        void safe_dereference_throws_null_pointer_exception_naming_the_expression()
        {
            PropertyConduit conduit = conduit("child?.name");

            NullPointerException ex = assertThrows(NullPointerException.class, () -> conduit.get(null));

            assertEquals("Root object of property expression 'child?.name' is null.", ex.getMessage());
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("method invocation")
    class MethodInvocation
    {
        @Test
        void without_arguments()
        {
            assertEquals("value", get("getValue()"));
        }

        @Test
        void without_arguments_has_return_type()
        {
            assertSame(String.class, conduit("getValue()").getPropertyType());
        }

        @Test
        void with_several_arguments()
        {
            assertEquals(6, get("sum(1, 2, 3)"));
        }

        @Test
        void with_several_arguments_has_return_type()
        {
            assertSame(int.class, conduit("sum(1, 2, 3)").getPropertyType());
        }

        Stream<Arguments> arguments()
        {
            return Stream.of(
                    Arguments.of("identity(1)", 1L),
                    Arguments.of("identity(-2.5)", -2.5d),
                    Arguments.of("identity('text')", "text"),
                    Arguments.of("identity(true)", Boolean.TRUE),
                    Arguments.of("identity(false)", Boolean.FALSE),
                    Arguments.of("identity(null)", null),
                    Arguments.of("identity(child.name)", "child-name"),
                    Arguments.of("identity([1, 'two'])", list(1L, "two")),
                    Arguments.of("identity({1: 2})", map(1L, 2L)),
                    Arguments.of("identity(!flag)", Boolean.FALSE));
        }

        @ParameterizedTest
        @MethodSource("arguments")
        void passes_argument(String expression, Object expected)
        {
            assertEquals(expected, get(expression));
        }

        @Test
        void passes_this_as_argument()
        {
            Root root = new Root();

            assertSame(root, conduit("identity(this)").get(root));
        }

        @Test
        void invokes_object_methods()
        {
            assertEquals("root", get("toString()"));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("method chaining")
    class MethodChaining
    {
        @Test
        void invokes_method_on_method_result()
        {
            assertEquals("child-name", get("getChild().getName()"));
        }

        @Test
        void has_the_return_type_of_the_last_method()
        {
            assertSame(String.class, conduit("getChild().getName()").getPropertyType());
        }

        @Test
        void reads_property_of_method_result()
        {
            assertEquals("held-name", get("getHolder().get().name"));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("overloads and coercion")
    class OverloadsAndCoercion
    {
        @ParameterizedTest
        @MethodSource
        void arity_selects_the_overload(String expression, String expected)
        {
            assertEquals(expected, get(expression));
        }

        Stream<Arguments> arity_selects_the_overload()
        {
            return Stream.of(
                    Arguments.of("pick()", "none"),
                    Arguments.of("pick(1)", "one"),
                    Arguments.of("pick(1, 2)", "two"));
        }

        @Test
        void type_coercer_converts_string_argument_to_int()
        {
            assertEquals(42, get("twice('21')"));
        }

        @Test
        void type_coercer_converts_integer_argument_to_string()
        {
            assertEquals("[5]", get("bracket(number)"));
        }

        @Test
        void wrong_arity_throws_property_expression_exception()
        {
            assertThrows(PropertyExpressionException.class, () -> conduit("twice(1, 2)"));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("case-insensitive resolution")
    class CaseInsensitiveResolution
    {
        @ParameterizedTest
        @ValueSource(strings = {"NAME", "Name", "nAmE"})
        void property_names(String expression)
        {
            assertEquals("root-name", get(expression));
        }

        @Test
        void method_names()
        {
            assertEquals("child-name", get("GETCHILD().name"));
        }

        @Test
        void chained_property_names()
        {
            assertEquals("leaf-text", get("CHILD.Leaf.TEXT"));
        }

        @Test
        void write_through_upper_case_method_name()
        {
            Root root = new Root();

            conduit("GETCHILD().name").set(root, "changed");

            assertEquals("changed", root.getChild().getName());
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("ranges")
    class Ranges
    {
        Stream<Arguments> ranges()
        {
            return Stream.of(
                    Arguments.of("1..10", new IntegerRange(1, 10)),
                    Arguments.of("-2..0", new IntegerRange(-2, 0)),
                    Arguments.of("+7..+20", new IntegerRange(7, 20)),
                    Arguments.of("number..99", new IntegerRange(5, 99)),
                    Arguments.of("10..number", new IntegerRange(10, 5)),
                    Arguments.of("child.size..number", new IntegerRange(3, 5)),
                    Arguments.of("120..child.size", new IntegerRange(120, 3)),
                    Arguments.of("getOne()..3", new IntegerRange(1, 3)));
        }

        @ParameterizedTest
        @MethodSource("ranges")
        void evaluate_to_integer_range(String expression, IntegerRange expected)
        {
            assertEquals(expected, get(expression));
        }

        @ParameterizedTest
        @MethodSource("ranges")
        void have_type_integer_range(String expression, IntegerRange expected)
        {
            assertSame(IntegerRange.class, conduit(expression).getPropertyType());
        }

        @ParameterizedTest
        @MethodSource("ranges")
        void are_read_only(String expression, IntegerRange expected)
        {
            assertReadOnly(expression);
        }

        @Test
        void folded_and_built_forms_agree()
        {
            assertEquals(get("1..5"), get("one..5"));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("list literals")
    class ListLiterals
    {
        Stream<Arguments> lists()
        {
            return Stream.of(
                    Arguments.of("[]", list()),
                    Arguments.of("[1]", list(1L)),
                    Arguments.of("[1, 2]", list(1L, 2L)),
                    Arguments.of("[[1],[2]]", list(list(1L), list(2L))),
                    Arguments.of("[!flag]", list(false)),
                    Arguments.of("[null, 2.5]", list(null, 2.5d)),
                    Arguments.of("[1, 'two', true, number]", list(1L, "two", true, 5)),
                    Arguments.of("[{'a': 1}]", list(map("a", 1L))));
        }

        @ParameterizedTest
        @MethodSource("lists")
        void evaluate_to_array_list_with_the_elements(String expression, List<Object> expected)
        {
            Object actual = get(expression);

            assertEquals(ArrayList.class, actual.getClass());
            assertEquals(expected, actual);
        }

        @ParameterizedTest
        @MethodSource("lists")
        void have_type_list(String expression, List<Object> expected)
        {
            assertSame(List.class, conduit(expression).getPropertyType());
        }

        @ParameterizedTest
        @MethodSource("lists")
        void are_read_only(String expression, List<Object> expected)
        {
            assertReadOnly(expression);
        }

        @Test
        void hold_this()
        {
            Root root = new Root();

            List<?> actual = (List<?>) conduit("[this]").get(root);

            assertSame(root, actual.get(0));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("map literals")
    class MapLiterals
    {
        Stream<Arguments> maps()
        {
            return Stream.of(
                    Arguments.of("{}", map()),
                    Arguments.of("{'a': 1}", map("a", 1L)),
                    Arguments.of("{'a': 1, 'b': 2}", map("a", 1L, "b", 2L)),
                    Arguments.of("{1: 'one'}", map(1L, "one")),
                    Arguments.of("{2.0: 'two'}", map(2.0d, "two")),
                    Arguments.of("{null: 1}", map(null, 1L)),
                    Arguments.of("{true: 1}", map(true, 1L)),
                    Arguments.of("{child.name: 1}", map("child-name", 1L)),
                    Arguments.of("{'a': [1, 2]}", map("a", list(1L, 2L))),
                    Arguments.of("{'a': {'b': 1}}", map("a", map("b", 1L))),
                    Arguments.of("{'a': number}", map("a", 5)));
        }

        @ParameterizedTest
        @MethodSource("maps")
        void evaluate_to_hash_map_with_the_entries(String expression, Map<Object, Object> expected)
        {
            Object actual = get(expression);

            assertEquals(HashMap.class, actual.getClass());
            assertEquals(expected, actual);
        }

        @ParameterizedTest
        @MethodSource("maps")
        void have_type_map(String expression, Map<Object, Object> expected)
        {
            assertSame(Map.class, conduit(expression).getPropertyType());
        }

        @ParameterizedTest
        @MethodSource("maps")
        void are_read_only(String expression, Map<Object, Object> expected)
        {
            assertReadOnly(expression);
        }

        @Test
        void accept_this_as_key()
        {
            Root root = new Root();

            Map<?, ?> actual = (Map<?, ?>) conduit("{this: 1}").get(root);

            assertEquals(1L, actual.get(root));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("not operator")
    class NotOperator
    {
        Stream<Arguments> negations()
        {
            return Stream.of(
                    Arguments.of("!flag", false),
                    Arguments.of("!!flag", true),
                    Arguments.of("!true", false),
                    Arguments.of("!false", true),
                    Arguments.of("!null", true),
                    Arguments.of("!nothing", true),
                    Arguments.of("!child", false),
                    Arguments.of("!name", false),
                    Arguments.of("!'false'", true),
                    Arguments.of("!''", true),
                    Arguments.of("!items", false),
                    Arguments.of("!emptyItems", true),
                    Arguments.of("!0", true),
                    Arguments.of("!number", false),
                    Arguments.of("![]", true));
        }

        @ParameterizedTest
        @MethodSource("negations")
        void evaluates_to_boolean(String expression, Boolean expected)
        {
            assertEquals(expected, get(expression));
        }

        @ParameterizedTest
        @MethodSource("negations")
        void has_type_boolean(String expression, Boolean expected)
        {
            assertSame(boolean.class, conduit(expression).getPropertyType());
        }

        @ParameterizedTest
        @MethodSource("negations")
        void is_read_only(String expression, Boolean expected)
        {
            assertReadOnly(expression);
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("writes")
    class Writes
    {
        @Test
        void settable_property_is_written()
        {
            Root root = new Root();

            conduit("name").set(root, "changed");

            assertEquals("changed", root.getName());
        }

        @Test
        void read_only_property_rejects_write()
        {
            PropertyConduit conduit = conduit("readOnly");
            Root root = new Root();

            RuntimeException ex = assertThrows(RuntimeException.class, () -> conduit.set(root, "changed"));

            assertEquals("Expression 'readOnly' for class " + Root.class.getName() + " is read-only.", ex.getMessage());
        }

        @Test
        void write_only_property_rejects_read()
        {
            PropertyConduit conduit = conduit("writeOnly");
            Root root = new Root();

            RuntimeException ex = assertThrows(RuntimeException.class, () -> conduit.get(root));

            assertEquals("Expression 'writeOnly' for class " + Root.class.getName() + " is write-only.", ex.getMessage());
        }

        @Test
        void write_only_property_is_written()
        {
            Root root = new Root();

            conduit("writeOnly").set(root, "changed");

            assertEquals("changed", root.writeOnlyValue);
        }

        @Test
        void write_only_property_has_setter_type()
        {
            assertSame(String.class, conduit("writeOnly").getPropertyType());
        }

        @Test
        void setter_without_getter_is_written()
        {
            Root root = new Root();

            conduit("nameFromSetter").set(root, "changed");

            assertEquals("set:changed", root.getName());
        }

        @Test
        void method_invocation_rejects_write()
        {
            assertReadOnly("getValue()");
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("public fields")
    class PublicFields
    {
        @AfterEach
        void reset_static_field()
        {
            Fields.shared = "shared";
        }

        @Test
        void instance_field_is_read()
        {
            assertEquals("field", source.create(Fields.class, "text").get(new Fields()));
        }

        @Test
        void instance_field_is_written()
        {
            Fields fields = new Fields();

            source.create(Fields.class, "text").set(fields, "changed");

            assertEquals("changed", fields.text);
        }

        @Test
        void primitive_field_is_read()
        {
            assertEquals(7, source.create(Fields.class, "count").get(new Fields()));
        }

        @Test
        void primitive_field_is_written()
        {
            Fields fields = new Fields();

            source.create(Fields.class, "COUNT").set(fields, 37);

            assertEquals(37, fields.count);
        }

        @Test
        void primitive_field_has_primitive_type()
        {
            assertSame(int.class, source.create(Fields.class, "count").getPropertyType());
        }

        @Test
        void static_field_is_read_without_root()
        {
            assertEquals("shared", source.create(Fields.class, "shared").get(null));
        }

        @Test
        void static_field_is_written_without_root()
        {
            source.create(Fields.class, "shared").set(null, "changed");

            assertEquals("changed", Fields.shared);
        }

        @Test
        void final_static_field_is_read()
        {
            assertEquals("constant", source.create(Fields.class, "constant").get(null));
        }

        @Test
        void final_static_field_is_read_only()
        {
            PropertyConduit conduit = source.create(Fields.class, "constant");

            RuntimeException ex = assertThrows(RuntimeException.class, () -> conduit.set(null, "changed"));

            assertEquals("Expression 'constant' for class " + Fields.class.getName() + " is read-only.",
                    ex.getMessage());
        }

        @Test
        void field_in_a_chain_is_read()
        {
            assertEquals("next-field", source.create(Fields.class, "next.text").get(new Fields()));
        }

        @Test
        void field_in_a_chain_is_written()
        {
            Fields fields = new Fields();

            source.create(Fields.class, "next.text").set(fields, "changed");

            assertEquals("changed", fields.next.text);
        }

        @Test
        void static_field_in_a_list()
        {
            assertEquals(list("constant"), source.create(Fields.class, "[constant]").get(new Fields()));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("generic and covariant types")
    class GenericTypes
    {
        @Test
        void generic_property_has_raw_type()
        {
            assertSame(List.class, source.create(Generics.class, "names").getPropertyType());
        }

        @Test
        void generic_property_has_generic_type()
        {
            PropertyConduit2 conduit = (PropertyConduit2) source.create(Generics.class, "names");

            assertEquals("java.util.List<java.lang.String>", conduit.getPropertyGenericType().getTypeName());
        }

        @Test
        void parameterized_base_class_resolves_type_variable()
        {
            assertSame(String.class, source.create(StringBox.class, "item").getPropertyType());
        }

        @Test
        void parameterized_base_class_value_is_read()
        {
            StringBox box = new StringBox();
            box.setItem("boxed");

            assertEquals("boxed", source.create(StringBox.class, "item").get(box));
        }

        @Test
        void generic_interface_resolves_return_type()
        {
            assertSame(Long.class, source.create(LongProducer.class, "produce()").getPropertyType());
        }

        @Test
        void covariant_return_type_is_used()
        {
            assertSame(Bar.class, source.create(Foo.class, "bar").getPropertyType());
        }

        @Test
        void abstract_return_type_is_used_on_the_abstract_class()
        {
            assertSame(AbstractBar.class, source.create(AbstractFoo.class, "bar").getPropertyType());
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("annotations")
    class Annotations
    {
        @Test
        void annotation_on_field_is_visible()
        {
            Marker marker = conduit("annotatedField").getAnnotation(Marker.class);

            assertEquals("field", marker.value());
        }

        @Test
        void annotation_on_getter_is_visible()
        {
            Marker marker = conduit("annotatedGetter").getAnnotation(Marker.class);

            assertEquals("getter", marker.value());
        }

        @Test
        void annotation_on_last_term_of_chain_is_visible()
        {
            Marker marker = conduit("child.marked").getAnnotation(Marker.class);

            assertEquals("child", marker.value());
        }

        @Test
        void missing_annotation_is_null()
        {
            assertNull(conduit("name").getAnnotation(Marker.class));
        }

        @Test
        void literal_conduit_has_invariant_annotation()
        {
            assertNotNull(conduit("12345").getAnnotation(Invariant.class));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("code-generation failures")
    class CodeGenerationFailures
    {
        @ParameterizedTest
        @ValueSource(strings = {
            "missing",
            "missing()",
            "child.missing",
            "child.missing()",
            "name.missing",
            "child?.missing",
            "[missing]",
            "{'a': missing}",
            "!missing",
            "missing..3"
        })
        void unknown_member_throws_property_expression_exception(String expression)
        {
            assertThrows(PropertyExpressionException.class, () -> conduit(expression));
        }
    }

    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    @DisplayName("rejected input")
    class RejectedInput
    {
        @ParameterizedTest
        @ValueSource(strings = {
            // lexical errors
            "fred #",
            "'unterminated",
            "'multi",
            "?",
            "a? . b",
            "1 + 2",
            "a - 3",
            "-",
            // syntax errors
            "this.foo",
            "true.foo",
            "null()",
            ".foo",
            ".",
            "..",
            "?.",
            "foo.",
            "foo(",
            "getValue(",
            "a b c",
            "a-3",
            "1a",
            "1.x",
            "1.foo",
            "1e5",
            "0x10",
            "'a''b'",
            "1..2..3",
            "a...b",
            "a....b",
            "1..'a'",
            "'a'..'b'",
            "1.5..2.5",
            "this..2",
            "foo()()",
            "[1,2,]",
            "[,]",
            "[1 2]",
            "foo(1 2)",
            "{'a':1,}",
            "{:}",
            "{'a'}",
            "{'a':}",
            "{[1]: 2}",
            "{!x: 1}",
            "!"
        })
        void throws_with_parse_error_prefix(String expression)
        {
            RuntimeException ex = assertThrows(RuntimeException.class, () -> conduit(expression));

            String prefix = String.format(PARSE_ERROR_PREFIX, expression);
            assertTrue(ex.getMessage().startsWith(prefix), ex.getMessage());
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Marker
    {
        String value();
    }

    public static class Root
    {
        private String name = "root-name";

        private Integer number = 5;

        private Child child = new Child();

        private Holder holder = new Holder();

        private Object nothing;

        private boolean flag = true;

        private List<String> items = new ArrayList<>(Collections.singletonList("item"));

        private String writeOnlyValue;

        @Marker("field")
        private String annotatedField;

        private String nullify = "nullify-value";

        private String truest = "truest-value";

        private String thisIsIt = "thisIsIt-value";

        private String $dollar = "$dollar-value";

        private String _under = "_under-value";

        private String a1 = "a1-value";

        private String café = "café-value";

        private String 日本語 = "日本語-value";

        private Child naïve = new Child();

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public Integer getNumber()
        {
            return number;
        }

        public void setNumber(Integer number)
        {
            this.number = number;
        }

        public int getOne()
        {
            return 1;
        }

        public Child getChild()
        {
            return child;
        }

        public void setChild(Child child)
        {
            this.child = child;
        }

        public Holder getHolder()
        {
            return holder;
        }

        public Object getNothing()
        {
            return nothing;
        }

        public boolean isFlag()
        {
            return flag;
        }

        public void setFlag(boolean flag)
        {
            this.flag = flag;
        }

        public List<String> getItems()
        {
            return items;
        }

        public List<String> getEmptyItems()
        {
            return Collections.emptyList();
        }

        public String getReadOnly()
        {
            return "read-only";
        }

        public void setWriteOnly(String value)
        {
            writeOnlyValue = value;
        }

        public void setNameFromSetter(String value)
        {
            name = "set:" + value;
        }

        public String getAnnotatedField()
        {
            return annotatedField;
        }

        public void setAnnotatedField(String annotatedField)
        {
            this.annotatedField = annotatedField;
        }

        @Marker("getter")
        public String getAnnotatedGetter()
        {
            return "annotated";
        }

        public String getNullify()
        {
            return nullify;
        }

        public String getTruest()
        {
            return truest;
        }

        public String getThisIsIt()
        {
            return thisIsIt;
        }

        public String get$dollar()
        {
            return $dollar;
        }

        public void set$dollar(String value)
        {
            $dollar = value;
        }

        public String get_under()
        {
            return _under;
        }

        public void set_under(String value)
        {
            _under = value;
        }

        public String getA1()
        {
            return a1;
        }

        public void setA1(String value)
        {
            a1 = value;
        }

        public String getCafé()
        {
            return café;
        }

        public void setCafé(String value)
        {
            café = value;
        }

        public String get日本語()
        {
            return 日本語;
        }

        public void set日本語(String value)
        {
            日本語 = value;
        }

        public Child getNaïve()
        {
            return naïve;
        }

        public String getValue()
        {
            return "value";
        }

        public int sum(int a, int b, int c)
        {
            return a + b + c;
        }

        public Object identity(Object value)
        {
            return value;
        }

        public String pick()
        {
            return "none";
        }

        public String pick(Object first)
        {
            return "one";
        }

        public String pick(Object first, Object second)
        {
            return "two";
        }

        public int twice(int value)
        {
            return 2 * value;
        }

        public String bracket(String value)
        {
            return "[" + value + "]";
        }

        @Override
        public String toString()
        {
            return "root";
        }
    }

    public static class Child
    {
        private String name = "child-name";

        private String property = "child-property";

        private Leaf leaf = new Leaf();

        @Marker("child")
        private String marked;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getProperty()
        {
            return property;
        }

        public void setProperty(String property)
        {
            this.property = property;
        }

        public int getSize()
        {
            return 3;
        }

        public Leaf getLeaf()
        {
            return leaf;
        }

        public void setLeaf(Leaf leaf)
        {
            this.leaf = leaf;
        }

        public Leaf leaf()
        {
            return leaf;
        }

        public String getMarked()
        {
            return marked;
        }

        public void setMarked(String marked)
        {
            this.marked = marked;
        }
    }

    public static class Leaf
    {
        private String text = "leaf-text";

        public String getText()
        {
            return text;
        }

        public void setText(String text)
        {
            this.text = text;
        }

        public Inner getInner()
        {
            return new Inner();
        }
    }

    public static class Inner
    {
        public String describe()
        {
            return "inner";
        }
    }

    public static class Holder
    {
        private final Child held = new Child();

        public Holder()
        {
            held.setName("held-name");
        }

        public Child get()
        {
            return held;
        }
    }

    public static class Fields
    {
        public static String shared = "shared";

        public static final String CONSTANT = "constant";

        public String text = "field";

        public int count = 7;

        public Fields next;

        public Fields()
        {
            this(true);
        }

        private Fields(boolean withNext)
        {
            if (withNext)
            {
                next = new Fields(false);
                next.text = "next-field";
            }
        }
    }

    public static class Generics
    {
        private List<String> names = new ArrayList<>();

        public List<String> getNames()
        {
            return names;
        }

        public void setNames(List<String> names)
        {
            this.names = names;
        }
    }

    public static class Box<T>
    {
        private T item;

        public T getItem()
        {
            return item;
        }

        public void setItem(T item)
        {
            this.item = item;
        }
    }

    public static class StringBox extends Box<String>
    {
    }

    public interface Producer<T>
    {
        T produce();
    }

    public static class LongProducer implements Producer<Long>
    {
        @Override
        public Long produce()
        {
            return 1L;
        }
    }

    public abstract static class AbstractBar
    {
    }

    public static class Bar extends AbstractBar
    {
    }

    public abstract static class AbstractFoo
    {
        public abstract AbstractBar getBar();
    }

    public static class Foo extends AbstractFoo
    {
        @Override
        public Bar getBar()
        {
            return new Bar();
        }
    }
}
