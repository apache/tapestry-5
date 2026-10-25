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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * An identifier is a Java identifier without identifier-ignorable characters, scanned by code point.
 */
public class JavaIdentifierTest
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

    private static void assertParseError(String expression)
    {
        RuntimeException ex = assertThrows(RuntimeException.class, () -> source.create(Root.class, expression));

        String prefix = "Error parsing property expression '" + expression + "': ";
        assertTrue(ex.getMessage().startsWith(prefix), ex.getMessage());
    }

    @Disabled("The ANTLR lexer accepts only a fixed list of character ranges")
    @ParameterizedTest
    @ValueSource(strings = {"한글", "µx", "€y", "㴮z", "𠀀w"})
    void java_identifier_start_characters_are_accepted(String expression)
    {
        assertEquals(expression + "-value", get(expression));
    }

    @Disabled("The ANTLR lexer accepts only a fixed list of character ranges")
    @Test
    void hangul_identifier_heads_a_chain()
    {
        assertEquals("b-value", get("가.b"));
    }

    @Disabled("The ANTLR lexer accepts U+1680 and U+0378 in identifiers")
    @ParameterizedTest
    @ValueSource(strings = {"\u1680x", "\u0378y"})
    void characters_that_are_no_java_identifier_start_are_rejected(String expression)
    {
        assertParseError(expression);
    }

    @ParameterizedTest
    @ValueSource(strings = {"\u0000", "\u0007", "\u007f", "\u00ad", "\u200b", "\u200d", "\ufeff"})
    void identifier_ignorable_characters_are_rejected(String ignorable)
    {
        assertParseError("foo" + ignorable);
    }

    public static class Root
    {
        public String get한글()
        {
            return "한글-value";
        }

        public String getµx()
        {
            return "µx-value";
        }

        public String get€y()
        {
            return "€y-value";
        }

        public String get㴮z()
        {
            return "㴮z-value";
        }

        public String get𠀀w()
        {
            return "𠀀w-value";
        }

        public Hangul get가()
        {
            return new Hangul();
        }

        public String getFoo()
        {
            return "foo-value";
        }
    }

    public static class Hangul
    {
        public String getB()
        {
            return "b-value";
        }
    }
}
