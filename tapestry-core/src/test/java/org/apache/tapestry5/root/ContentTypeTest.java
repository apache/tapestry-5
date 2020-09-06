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

package org.apache.tapestry5.root;

import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.test.ioc.TestBase;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class ContentTypeTest extends TestBase
{
    @Test
    public void simple_equals()
    {
        ContentType master = new ContentType("text/html");

        assertFalse(master.equals(null));
        assertFalse(master.equals(this));
        assertTrue(master.equals(master));
        assertTrue(master.equals(new ContentType("text/html")));
        assertFalse(master.equals(new ContentType("foo/bar")));
        assertFalse(master.equals(new ContentType("text/plain")));
    }

    @Test
    public void equals_with_parameters()
    {
        ContentType master = new ContentType("text/html;charset=utf-8");

        assertFalse(master.equals(new ContentType("text/html")));
        assertTrue(master.equals(new ContentType("text/html;charset=utf-8")));
        assertFalse(master.equals(new ContentType("text/html;charset=utf-8;foo=bar")));

        // Check that keys are case insensitive

        assertTrue(master.equals(new ContentType("text/html;Charset=utf-8")));

        master = new ContentType("text/html;foo=bar;biff=bazz");

        assertTrue(master.equals(new ContentType("text/html;foo=bar;biff=bazz")));
        assertTrue(master.equals(new ContentType("text/html;Foo=bar;Biff=bazz")));
        assertTrue(master.equals(new ContentType("text/html;biff=bazz;foo=bar")));
    }

    @Test
    public void parse_with_parameters() throws Exception
    {
        ContentType contentType = new ContentType("text/html;charset=utf-8");

        assertEquals(contentType.getBaseType(), "text");

        assertEquals(contentType.getSubType(), "html");

        assertEquals(contentType.getMimeType(), "text/html");

        List<String> parameterNames = contentType.getParameterNames();
        assertEquals(parameterNames.size(), 1);

        assertEquals(parameterNames.get(0), "charset");

        assertEquals(contentType.getCharset(), "utf-8");

        assertTrue(contentType.hasParameters());

        String nonexistant = contentType.getParameter("nonexistant");

        assertTrue(nonexistant == null);
    }

    @DataProvider
    public Object[][] invalid_content_type_strings_data()
    {
        return new Object[][]{
                {""},
                {"foo/"},
                {"foo/bar;"},
                {"foo/bar;baz"},
                {"foo/bar;baz="},
                {"foo/bar;baz=biff;"}
        };
    }

    @Test(dataProvider = "invalid_content_type_strings_data")
    public void invalid_content_type_strings(String input)
    {
        try
        {
            new ContentType(input);

            unreachable();
        } catch (IllegalArgumentException ex)
        {

        }

    }

    @Test
    public void parse_without_parameters() throws Exception
    {
        ContentType contentType = new ContentType("text/html");

        assertEquals(contentType.getBaseType(), "text");

        assertEquals(contentType.getSubType(), "html");

        assertEquals(contentType.getMimeType(), "text/html");

        assertTrue(contentType.getParameterNames().isEmpty());

        assertFalse(contentType.hasParameters());
    }

    @Test
    public void unparse_with_parameters() throws Exception
    {
        ContentType contentType = new ContentType("text/html").withCharset("utf-8");

        assertEquals(contentType.toString(), "text/html;charset=utf-8");
    }

    @Test
    public void unparse_no_parameters() throws Exception
    {
        ContentType contentType = new ContentType("text/html");

        assertEquals(contentType.toString(), "text/html");
    }

    @Test
    public void add_charset() throws Exception
    {
        ContentType base = new ContentType("text/html");

        ContentType charset = base.withCharset("utf-8");

        assertTrue(charset.hasParameters());

        assertNotSame(base, charset);
        assertNotEquals(base, charset);

        assertEquals(base.toString(), "text/html");
        assertEquals(charset.toString(), "text/html;charset=utf-8");
    }
}
