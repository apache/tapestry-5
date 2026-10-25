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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * A non-ASCII expression parses whatever the default charset is.
 * Runs in the {@code testDefaultCharset} task, under a US-ASCII default charset.
 */
@Tag("default-charset")
public class DefaultCharsetTest
{
    private static PropertyConduitSource source;

    @BeforeAll
    static void setup()
    {
        source = PropertyConduitSourceFixture.create();
    }

    @Test
    void default_charset_is_us_ascii()
    {
        assertEquals(StandardCharsets.US_ASCII, Charset.defaultCharset());
    }

    @Test
    void non_ascii_identifier_is_parsed()
    {
        assertEquals(Chinese.class, source.create(Root.class, "中文").get(new Root()).getClass());
    }

    @Test
    void non_ascii_identifier_heads_a_chain()
    {
        assertEquals("x-value", source.create(Root.class, "中文.x").get(new Root()));
    }

    public static class Root
    {
        public Chinese get中文()
        {
            return new Chinese();
        }
    }

    public static class Chinese
    {
        public String getX()
        {
            return "x-value";
        }
    }
}
