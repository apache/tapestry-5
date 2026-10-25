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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.tapestry5.beanmodel.internal.services.PropertyExpressionException;
import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;
import org.apache.tapestry5.commons.util.IntegerRange;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class PropertyConduitTest
{
    private static PropertyConduitSource propertyConduitSource;

    private RootBean root;

    @BeforeAll
    static void setupSource()
    {
        propertyConduitSource = PropertyConduitSourceFixture.create();
    }

    @BeforeEach
    void setupRoot()
    {
        root = new RootBean();
        root.testBean = new TestBean();
        root.testBean.stringProperty = "beanString";
        root.testBean.intProperty = 123;
        root.testBean.booleanProperty = true;
        root.testBean.wrapperBooleanProperty = Boolean.TRUE;
        root.testBean.nestedBean = new TestBean("nestedBeanString");
        root.testBean.stringList = Arrays.asList("a", "b", "c");
        root.testBean.beanList = Arrays.asList(new TestBean("beanInList1"), new TestBean("beanInList2"));
        root.testBean.stringMap = new HashMap<>();
        root.testBean.stringMap.put("key1", "value1");
        root.testBean.stringMap.put("key2", "value2");
        root.testBean.beanMap = Collections.singletonMap("mapKey1", new TestBean("beanInMap1"));
        root.topLevelList = Arrays.asList(10, 20, 30);
        root.topLevelMap = new HashMap<>();
        root.topLevelMap.put("mapA", 100);
        root.topLevelMap.put("mapB", 200);
    }

    private PropertyConduit conduit(String expression)
    {
        return propertyConduitSource.create(RootBean.class, expression);
    }

    static Stream<Arguments> properties()
    {
        return Stream.of(
                Arguments.of("topLevelString", "top", String.class),
                Arguments.of("topLevelInt", 100, Integer.class),
                Arguments.of("topLevelBoolean", true, boolean.class),
                Arguments.of("testBean.stringProperty", "beanString", String.class),
                Arguments.of("testBean.intProperty", 123, Integer.class),
                Arguments.of("testBean.booleanProperty", true, boolean.class),
                Arguments.of("testBean.wrapperBooleanProperty", Boolean.TRUE, Boolean.class),
                Arguments.of("testBean.nestedBean.stringProperty", "nestedBeanString", String.class),
                Arguments.of("testBean.flag", true, boolean.class),
                Arguments.of("testBean.privateString", "initialPrivate", String.class));
    }

    @ParameterizedTest
    @MethodSource("properties")
    void property_access_reads_value(String expression, Object expectedValue, Class<?> expectedType)
    {
        assertEquals(expectedValue, conduit(expression).get(root));
    }

    @ParameterizedTest
    @MethodSource("properties")
    void property_access_has_type(String expression, Object expectedValue, Class<?> expectedType)
    {
        assertSame(expectedType, conduit(expression).getPropertyType());
    }

    @Test
    void safe_dereference_reads_through_non_null_bean()
    {
        assertEquals("beanString", conduit("testBean?.stringProperty").get(root));
    }

    @Test
    void safe_dereference_reads_through_non_null_nested_bean()
    {
        assertEquals("nestedBeanString", conduit("testBean.nestedBean?.stringProperty").get(root));
    }

    @Test
    void safe_dereference_yields_null_for_null_nested_bean()
    {
        root.testBean.nestedBean = null;

        assertNull(conduit("testBean.nestedBean?.stringProperty").get(root));
    }

    @Test
    void safe_dereference_yields_null_for_null_bean()
    {
        root.testBean = null;

        assertNull(conduit("testBean?.stringProperty").get(root));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "testBean?.stringProperty",
        "testBean.nestedBean?.stringProperty"
    })
    void safe_dereference_type_is_known_regardless_of_value(String expression)
    {
        assertSame(String.class, conduit(expression).getPropertyType());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "nullObject?.someProperty",
        "testBean?.nonExistentProperty"
    })
    void safe_dereference_fails_for_non_existent_properties(String expression)
    {
        assertThrows(PropertyExpressionException.class, () -> conduit(expression));
    }

    static Stream<Arguments> methods()
    {
        return Stream.of(
                Arguments.of("testBean.getStringMethod()", "methodResult", String.class),
                Arguments.of("testBean.getStringMethodWithArg('hello')", "methodResult:hello", String.class),
                Arguments.of("testBean.getStringMethodWithArg(testBean.stringProperty)", "methodResult:beanString",
                        String.class),
                Arguments.of("testBean.getIntMethodWithArgs(5, 3)", 8, int.class),
                Arguments.of("testBean.getIntMethodWithArgs(testBean.intProperty, 2)", 125, int.class),
                Arguments.of("getCalculated()", "calculatedValue", String.class));
    }

    @ParameterizedTest
    @MethodSource("methods")
    void method_invocation_returns_value(String expression, Object expectedValue, Class<?> expectedType)
    {
        assertEquals(expectedValue, conduit(expression).get(root));
    }

    @ParameterizedTest
    @MethodSource("methods")
    void method_invocation_has_return_type(String expression, Object expectedValue, Class<?> expectedType)
    {
        assertSame(expectedType, conduit(expression).getPropertyType());
    }

    @Test
    void keyword_this_evaluates_to_the_root_object()
    {
        assertSame(root, conduit("this").get(root));
    }

    @Test
    void keyword_this_has_root_type()
    {
        assertSame(RootBean.class, conduit("this").getPropertyType());
    }

    static Stream<Arguments> keywords()
    {
        return Stream.of(
                Arguments.of("null", null, Void.class),
                Arguments.of("true", true, Boolean.class),
                Arguments.of("false", false, Boolean.class));
    }

    @ParameterizedTest
    @MethodSource("keywords")
    void keyword_evaluates_to_constant(String expression, Object expectedValue, Class<?> expectedType)
    {
        assertEquals(expectedValue, conduit(expression).get(root));
    }

    @ParameterizedTest
    @MethodSource("keywords")
    void keyword_has_type(String expression, Object expectedValue, Class<?> expectedType)
    {
        assertSame(expectedType, conduit(expression).getPropertyType());
    }

    static Stream<Arguments> literals()
    {
        return Stream.of(
                Arguments.of("123", 123L, Long.class),
                Arguments.of("-45", -45L, Long.class),
                Arguments.of("1.23", 1.23d, Double.class),
                Arguments.of("-.5", -0.5d, Double.class),
                Arguments.of("'hello'", "hello", String.class),
                Arguments.of("''", "", String.class));
    }

    @ParameterizedTest
    @MethodSource("literals")
    void literal_evaluates_to_value_of_its_type(String expression, Object expectedValue, Class<?> expectedType)
    {
        assertEquals(expectedValue, conduit(expression).get(root));
    }

    @ParameterizedTest
    @MethodSource("literals")
    void literal_has_type(String expression, Object expectedValue, Class<?> expectedType)
    {
        assertSame(expectedType, conduit(expression).getPropertyType());
    }

    static Stream<Arguments> negations()
    {
        Consumer<RootBean> unchanged = root -> {
        };

        return Stream.of(
                Arguments.of("!true", unchanged, false),
                Arguments.of("!false", unchanged, true),
                Arguments.of("!topLevelBoolean", unchanged, false),
                Arguments.of("!topLevelBoolean", (Consumer<RootBean>) root -> root.topLevelBoolean = false, true),
                Arguments.of("!nullObject", unchanged, true),
                Arguments.of("!testBean", unchanged, false));
    }

    @ParameterizedTest
    @MethodSource("negations")
    void not_operator_evaluates_to_boolean(String expression, Consumer<RootBean> setupAction, boolean expectedValue)
    {
        setupAction.accept(root);

        assertEquals(expectedValue, conduit(expression).get(root));
    }

    @ParameterizedTest
    @MethodSource("negations")
    void not_operator_has_type_boolean(String expression, Consumer<RootBean> setupAction, boolean expectedValue)
    {
        assertSame(boolean.class, conduit(expression).getPropertyType());
    }

    static Stream<Arguments> ranges()
    {
        Consumer<RootBean> unchanged = root -> {
        };

        return Stream.of(
                Arguments.of("1..3", unchanged, Arrays.asList(1, 2, 3)),
                Arguments.of("topLevelInt .. 102", unchanged, Arrays.asList(100, 101, 102)),
                Arguments.of("topLevelInt .. 7", (Consumer<RootBean>) root -> root.topLevelInt = 5,
                        Arrays.asList(5, 6, 7)),
                Arguments.of("2..topLevelInt", (Consumer<RootBean>) root -> root.topLevelInt = 4,
                        Arrays.asList(2, 3, 4)),
                Arguments.of("120..testBean.intProperty", unchanged, Arrays.asList(120, 121, 122, 123)),
                Arguments.of("-2..0", unchanged, Arrays.asList(-2, -1, 0)));
    }

    @ParameterizedTest
    @MethodSource("ranges")
    void range_operator_evaluates_to_integer_range(String expression, Consumer<RootBean> setupAction,
            List<Integer> expectedList)
    {
        setupAction.accept(root);

        IntegerRange range = assertInstanceOf(IntegerRange.class, conduit(expression).get(root));

        List<Integer> actual = new ArrayList<>();
        range.forEach(actual::add);
        assertEquals(expectedList, actual);
    }

    @ParameterizedTest
    @MethodSource("ranges")
    void range_operator_has_type_integer_range(String expression, Consumer<RootBean> setupAction,
            List<Integer> expectedList)
    {
        assertSame(IntegerRange.class, conduit(expression).getPropertyType());
    }

    @Test
    void list_literal_evaluates_to_list_of_elements()
    {
        Object result = conduit("[1, 'two', true, testBean.intProperty]").get(root);

        assertEquals(Arrays.asList(1L, "two", true, 123), result);
    }

    @Test
    void list_literal_has_type_list()
    {
        assertSame(List.class, conduit("[1, 'two', true, testBean.intProperty]").getPropertyType());
    }

    @Test
    void map_literal_evaluates_to_map_of_entries()
    {
        Object result = conduit("{'a': 1, 'b': testBean.stringProperty, 'c': true }").get(root);

        Map<String, Object> expected = new HashMap<>();
        expected.put("a", 1L);
        expected.put("b", "beanString");
        expected.put("c", true);
        assertEquals(expected, result);
    }

    static Stream<Arguments> emptyLiterals()
    {
        return Stream.of(
                Arguments.of("[]", Collections.emptyList(), List.class),
                Arguments.of("{}", Collections.emptyMap(), Map.class));
    }

    @ParameterizedTest
    @MethodSource("emptyLiterals")
    void empty_literal_evaluates_to_empty_collection(String expression, Object expectedResult, Class<?> expectedType)
    {
        Object result = conduit(expression).get(root);

        assertEquals(expectedResult, result);
        assertInstanceOf(expectedType, result);
    }

    @ParameterizedTest
    @MethodSource("emptyLiterals")
    void empty_literal_has_collection_type(String expression, Object expectedResult, Class<?> expectedType)
    {
        assertSame(expectedType, conduit(expression).getPropertyType());
    }

    static Stream<Arguments> writes()
    {
        return Stream.of(
                Arguments.of("topLevelString", "newTop"),
                Arguments.of("topLevelInt", 999),
                Arguments.of("topLevelBoolean", false),
                Arguments.of("testBean.stringProperty", "newBeanString"),
                Arguments.of("testBean.intProperty", 789),
                Arguments.of("testBean.booleanProperty", false),
                Arguments.of("testBean.wrapperBooleanProperty", Boolean.FALSE),
                Arguments.of("testBean.nestedBean.stringProperty", "newNestedString"),
                Arguments.of("testBean.flag", false),
                Arguments.of("testBean.privateString", "newPrivate"));
    }

    @ParameterizedTest
    @MethodSource("writes")
    void set_property_updates_value(String expression, Object newValue)
    {
        PropertyConduit conduit = conduit(expression);
        Object originalValue = conduit.get(root);

        conduit.set(root, newValue);

        assertNotEquals(newValue, originalValue);
        assertEquals(newValue, conduit.get(root));
    }

    @Test
    void method_used_as_property_setter()
    {
        conduit("testBean.stringPropertyFromMethod").set(root, "directValue");

        assertEquals("setViaMethod:directValue", root.testBean.stringProperty);
    }

    @Test
    void final_property_is_read_only()
    {
        PropertyConduit conduit = conduit("finalProperty");

        assertThrows(RuntimeException.class, () -> conduit.set(root, "newValue"));
        assertEquals("finalValue", root.getFinalProperty());
    }

    @Test
    void write_only_property_type_is_based_on_setter()
    {
        assertSame(String.class, conduit("writeOnly").getPropertyType());
    }

    @Test
    void write_only_property_cannot_be_read()
    {
        PropertyConduit conduit = conduit("writeOnly");

        assertThrows(RuntimeException.class, () -> conduit.get(root));
    }

    @Test
    void write_only_property_can_be_set()
    {
        PropertyConduit conduit = conduit("writeOnly");

        assertDoesNotThrow(() -> conduit.set(root, "testSet"));
    }

    @Test
    void set_through_safe_dereference_on_valid_path()
    {
        conduit("testBean?.stringProperty").set(root, "safeSet");

        assertEquals("safeSet", root.testBean.stringProperty);
    }

    @Test
    void set_through_safe_dereference_on_null_path_is_ignored()
    {
        PropertyConduit conduit = conduit("testBean?.stringProperty");
        root.testBean = null;

        assertDoesNotThrow(() -> conduit.set(root, "ignoredSet"));
    }

    static Stream<Arguments> readOnlyLiterals()
    {
        return Stream.of(
                Arguments.of("'aString'", "new"),
                Arguments.of("123", 456),
                Arguments.of("1..3", Arrays.asList(1, 2)),
                Arguments.of("[1,2]", Arrays.asList(3, 4)),
                Arguments.of("{'a':1}", Collections.singletonMap("b", 2)),
                Arguments.of("true", false),
                Arguments.of("null", "not null"));
    }

    @ParameterizedTest
    @MethodSource("readOnlyLiterals")
    void literals_are_read_only(String expression, Object newValue)
    {
        PropertyConduit conduit = conduit(expression);

        assertThrows(RuntimeException.class, () -> conduit.set(root, newValue));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "nonExistentProperty",
        "testBean.nonExistent",
        "topLevelString.nonExistentPart"
    })
    void non_existent_property_path_fails(String expression)
    {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> conduit(expression).get(root));

        String message = ex.getMessage().toLowerCase();
        assertTrue(message.contains("property") || message.contains("could not find")
                || message.contains("unable to parse") || message.contains("no read property"), ex.getMessage());
    }

    @Test
    void method_call_with_wrong_number_of_arguments_fails()
    {
        assertThrows(PropertyExpressionException.class, () -> conduit("testBean.getIntMethodWithArgs(5)"));
    }

    @Test
    void method_call_with_wrong_type_of_arguments_is_coerced()
    {
        assertEquals("methodResult:123", conduit("testBean.getStringMethodWithArg(testBean.intProperty)").get(root));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        // parses, but RootBean has no property 'a'
        "a.b..c",
        // trailing dot
        "foo.",
        // leading dot not part of number
        ".foo",
        // unparseable sequence
        "a b c",
        // unmatched parenthesis
        "foo(",
        // trailing comma in map
        "{'a':1,}",
        // trailing comma in list
        "[1,2,]"
    })
    void invalid_expression_fails(String expression)
    {
        assertThrows(RuntimeException.class, () -> conduit(expression));
    }

    public static class TestBean
    {
        private String stringProperty;

        private Integer intProperty;

        private boolean booleanProperty;

        private Boolean wrapperBooleanProperty;

        private TestBean nestedBean;

        private List<String> stringList;

        private List<TestBean> beanList;

        private Map<String, String> stringMap;

        private Map<String, TestBean> beanMap;

        private String privateString = "initialPrivate";

        public TestBean()
        {
        }

        public TestBean(String stringProperty)
        {
            this.stringProperty = stringProperty;
        }

        public String getStringProperty()
        {
            return stringProperty;
        }

        public void setStringProperty(String stringProperty)
        {
            this.stringProperty = stringProperty;
        }

        public Integer getIntProperty()
        {
            return intProperty;
        }

        public void setIntProperty(Integer intProperty)
        {
            this.intProperty = intProperty;
        }

        public boolean isBooleanProperty()
        {
            return booleanProperty;
        }

        public void setBooleanProperty(boolean booleanProperty)
        {
            this.booleanProperty = booleanProperty;
        }

        public Boolean getWrapperBooleanProperty()
        {
            return wrapperBooleanProperty;
        }

        public void setWrapperBooleanProperty(Boolean wrapperBooleanProperty)
        {
            this.wrapperBooleanProperty = wrapperBooleanProperty;
        }

        public TestBean getNestedBean()
        {
            return nestedBean;
        }

        public void setNestedBean(TestBean nestedBean)
        {
            this.nestedBean = nestedBean;
        }

        public List<String> getStringList()
        {
            return stringList;
        }

        public void setStringList(List<String> stringList)
        {
            this.stringList = stringList;
        }

        public List<TestBean> getBeanList()
        {
            return beanList;
        }

        public void setBeanList(List<TestBean> beanList)
        {
            this.beanList = beanList;
        }

        public Map<String, String> getStringMap()
        {
            return stringMap;
        }

        public void setStringMap(Map<String, String> stringMap)
        {
            this.stringMap = stringMap;
        }

        public Map<String, TestBean> getBeanMap()
        {
            return beanMap;
        }

        public void setBeanMap(Map<String, TestBean> beanMap)
        {
            this.beanMap = beanMap;
        }

        public String getStringMethod()
        {
            return "methodResult";
        }

        public String getStringMethodWithArg(String arg)
        {
            return "methodResult:" + arg;
        }

        public int getIntMethodWithArgs(int a, int b)
        {
            return a + b;
        }

        public void setStringPropertyFromMethod(String value)
        {
            this.stringProperty = "setViaMethod:" + value;
        }

        public boolean isFlag()
        {
            return booleanProperty;
        }

        public void setFlag(boolean flag)
        {
            this.booleanProperty = flag;
        }

        public String getPrivateString()
        {
            return privateString;
        }

        public void setPrivateString(String privateString)
        {
            this.privateString = privateString;
        }
    }

    public static class RootBean
    {
        private static String staticProperty = "staticValue";

        private final String finalProperty = "finalValue";

        private TestBean testBean = new TestBean();

        private String topLevelString = "top";

        private Integer topLevelInt = 100;

        private boolean topLevelBoolean = true;

        private List<Integer> topLevelList = Arrays.asList(10, 20, 30);

        private Map<String, Integer> topLevelMap;

        private Object nullObject;

        public static String getStaticProperty()
        {
            return staticProperty;
        }

        public static void setStaticProperty(String staticProperty)
        {
            RootBean.staticProperty = staticProperty;
        }

        public String getFinalProperty()
        {
            return finalProperty;
        }

        public TestBean getTestBean()
        {
            return testBean;
        }

        public void setTestBean(TestBean testBean)
        {
            this.testBean = testBean;
        }

        public String getTopLevelString()
        {
            return topLevelString;
        }

        public void setTopLevelString(String topLevelString)
        {
            this.topLevelString = topLevelString;
        }

        public Integer getTopLevelInt()
        {
            return topLevelInt;
        }

        public void setTopLevelInt(Integer topLevelInt)
        {
            this.topLevelInt = topLevelInt;
        }

        public boolean isTopLevelBoolean()
        {
            return topLevelBoolean;
        }

        public void setTopLevelBoolean(boolean topLevelBoolean)
        {
            this.topLevelBoolean = topLevelBoolean;
        }

        public List<Integer> getTopLevelList()
        {
            return topLevelList;
        }

        public void setTopLevelList(List<Integer> topLevelList)
        {
            this.topLevelList = topLevelList;
        }

        public Map<String, Integer> getTopLevelMap()
        {
            return topLevelMap;
        }

        public void setTopLevelMap(Map<String, Integer> topLevelMap)
        {
            this.topLevelMap = topLevelMap;
        }

        public Object getNullObject()
        {
            return nullObject;
        }

        public void setNullObject(Object nullObject)
        {
            this.nullObject = nullObject;
        }

        public String getCalculated()
        {
            return "calculatedValue";
        }

        public void setWriteOnly(String value)
        {
        }
    }
}
