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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;
import org.apache.tapestry5.commons.util.IntegerRange;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * A range is valid wherever an expression is valid (TAP5-2808).
 */
public class RangeSubexpressionTest
{
    private static PropertyConduitSource source;

    @BeforeAll
    static void setup()
    {
        source = PropertyConduitSourceFixture.create();
    }

    private static Object get(String expression)
    {
        return source.create(Root.class, expression).get(new Root());
    }

    @Test
    void range_in_a_nested_list()
    {
        // ARRANGE
        String expression = "[1, 'two', true, testBean.intProperty, [1..2]]";

        // ACT
        List<?> list = (List<?>) get(expression);

        // ASSETR
        assertEquals(Collections.singletonList(new IntegerRange(1, 2)), list.get(4));
    }

    @Test
    void range_in_a_list_inside_a_map()
    {
        // ARRANGE
        String expression = "{'a': 1, 'b': testBean.stringProperty, 'c': true, 123: 'numKey', d: [1..2] }";

        // ACT
        Map<?, ?> map = (Map<?, ?>) get(expression);

        // ASSERT
        assertEquals(Collections.singletonList(new IntegerRange(1, 2)), map.get("d"));
    }

    @Test
    void negated_range()
    {
        // ARRANGE
        String expression = "!1..2";

        // ACT
        Object result = get(expression);

        // ASSEERT
        assertEquals(Boolean.FALSE, result);
    }

    @Test
    void range_as_map_value()
    {
        // ARRANGE
        String expression = "{'r': 1..3}";

        // ACT
        Map<?, ?> map = (Map<?, ?>) get(expression);

        // ASSERT
        assertEquals(new IntegerRange(1, 3), map.get("r"));
    }

    @Test
    void range_as_method_argument()
    {
        // ARRANGE
        String expression = "rangeArgument(1..3)";

        // ACT
        Object result = get(expression);

        // ASSERT
        assertEquals(new IntegerRange(1, 3), result);
    }

    @Test
    void range_as_only_list_element()
    {
        // ARRANGE
        String expression = "[1..2]";

        // ACT
        Object result = get(expression);

        // ASSERT
        assertEquals(Collections.singletonList(new IntegerRange(1, 2)), result);
    }

    @Test
    void range_followed_by_list_element()
    {
        // ARRANGE
        String expression = "[1..2, 3]";

        // ACT
        Object result = get(expression);

        // ASSERT
        assertEquals(Arrays.asList(new IntegerRange(1, 2), 3L), result);
    }

    @Test
    void range_with_property_endpoint_in_a_list()
    {
        // ARRANGE
        String expression = "[testBean.intProperty..125]";

        // ACT
        Object result = get(expression);

        // ASSERT
        assertEquals(Collections.singletonList(new IntegerRange(123, 125)), result);
    }

    public static class Root
    {
        private final TestBean testBean = new TestBean();

        public TestBean getTestBean()
        {
            return testBean;
        }

        public String getD()
        {
            return "d";
        }

        public IntegerRange rangeArgument(IntegerRange range)
        {
            return range;
        }
    }

    public static class TestBean
    {
        public Integer getIntProperty()
        {
            return 123;
        }

        public String getStringProperty()
        {
            return "beanString";
        }
    }
}
