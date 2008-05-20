// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.Resource;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Locale;

public class ClasspathResourceTest extends Assert
{
    private static final String RESOURCE_TXT_CONTENT = "content from resource.txt";

    private static final String FOLDER = "org/apache/tapestry5/ioc/internal/util";

    private static final String PATH = FOLDER + "/resource.txt";

    @Test
    public void get_resource_URL() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        assertEquals(content(r), RESOURCE_TXT_CONTENT);
    }

    @Test
    public void relative_to_root_resource() throws Exception
    {
        Resource r = new ClasspathResource("").forFile(PATH);

        assertEquals(content(r), RESOURCE_TXT_CONTENT);
    }

    @Test
    public void relative_to_root_resource_using_leading_slash() throws Exception
    {
        Resource r = new ClasspathResource("/").forFile(PATH);

        assertEquals(content(r), RESOURCE_TXT_CONTENT);
    }

    @Test
    public void leading_slash_on_path_relative_to_root_doesnt_matter() throws Exception
    {
        Resource r = new ClasspathResource("/").forFile("/" + PATH);

        assertEquals(content(r), RESOURCE_TXT_CONTENT);
    }

    @Test
    public void path_and_file()
    {
        Resource r = new ClasspathResource(PATH);

        assertEquals(r.getFolder(), FOLDER);
        assertEquals(r.getFile(), "resource.txt");

    }

    @Test
    public void for_file_in_same_folder() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        Resource n = r.forFile("same-folder.txt");

        assertEquals(content(n), "content from same-folder resource");
    }

    @Test
    public void for_file_single_dot() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        Resource n = r.forFile("./same-folder.txt");

        assertEquals(content(n), "content from same-folder resource");
    }

    @Test
    public void multiple_slashes_treated_as_single_slash() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        Resource n = r.forFile("././/.///same-folder.txt");

        assertEquals(content(n), "content from same-folder resource");
    }

    @Test
    public void for_file_in_subfolder() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        Resource n = r.forFile("sub/sub-folder.txt");

        assertEquals(content(n), "content from sub-folder resource");
    }

    @Test
    public void for_file_same_resource() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        assertSame(r.forFile("../util/resource.txt"), r);
    }

    @Test
    public void for_file_in_parent_folder() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        Resource n = r.forFile("../parent-folder.txt");

        assertEquals(content(n), "content from parent-folder resource");
    }

    @Test
    public void to_string() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        assertEquals(r.toString(), "classpath:" + PATH);
    }

    @Test
    public void get_URL_for_missing_resource() throws Exception
    {
        Resource r = new ClasspathResource(FOLDER + "/missing-resource.txt");

        assertNull(r.toURL());
    }

    @Test
    public void localization_of_resource() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        Resource l = r.forLocale(Locale.FRENCH);

        assertEquals(content(l), "french content");
    }

    @Test
    public void localization_to_closest_match() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        Resource l = r.forLocale(Locale.CANADA_FRENCH);

        assertEquals(content(l), "french content");
    }

    @Test
    public void localization_to_base_resource() throws Exception
    {
        Resource r = new ClasspathResource(PATH);

        Resource l = r.forLocale(Locale.JAPANESE);

        assertSame(l, r);
    }

    @Test
    public void with_extension_same_extension()
    {
        Resource r = new ClasspathResource(PATH);

        assertSame(r.withExtension("txt"), r);
    }

    @Test
    public void with_extension() throws Exception
    {
        Resource r = new ClasspathResource(PATH);
        Resource e = r.withExtension("ext");

        assertEquals(content(e), "ext content");
    }

    @Test
    public void with_extension_adds_extension() throws Exception
    {
        Resource r = new ClasspathResource(FOLDER + "/resource");
        Resource e = r.withExtension("ext");

        assertEquals(content(e), "ext content");
    }

    @Test
    public void with_extension_missing_resource_is_null()
    {
        Resource r = new ClasspathResource(PATH);
        Resource e = r.withExtension("does-not-exist");

        assertNull(e.toURL());
    }

    @Test
    public void localization_of_missing_resource() throws Exception
    {
        Resource r = new ClasspathResource(FOLDER + "/missing-resource.txt");

        assertNull(r.forLocale(Locale.FRENCH));
    }

    private String content(Resource resource) throws Exception
    {
        return content(resource.toURL());
    }

    private String content(URL url) throws Exception
    {
        InputStream is = new BufferedInputStream(url.openStream());
        Reader r = new InputStreamReader(is);

        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[2000];

        while (true)
        {
            int length = r.read(buffer, 0, buffer.length);

            if (length < 0) break;

            builder.append(buffer, 0, length);
        }

        r.close();

        return builder.toString().trim();
    }
}
