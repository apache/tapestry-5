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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.Context;
import org.testng.annotations.Test;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ContextImplTest extends InternalBaseTestCase
{
    @Test
    public void get_resource_exists() throws Exception
    {
        String path = "/foo";
        URL url = getClass().getResource("ContextImplTest.class");

        ServletContext servletContext = newServletContext();

        expect(servletContext.getResource(path)).andReturn(url);

        replay();

        URL result = new ContextImpl(servletContext).getResource(path);

        assertSame(result, url);

        verify();
    }

    @Test
    public void get_resource_exception() throws Exception
    {
        String path = "/foo";
        Throwable t = new MalformedURLException("/foo is not a URL.");

        ServletContext servletContext = newServletContext();

        expect(servletContext.getResource(path)).andThrow(t);

        replay();

        try
        {
            new ContextImpl(servletContext).getResource(path);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "java.net.MalformedURLException: /foo is not a URL.");
            assertSame(ex.getCause(), t);
        }

        verify();
    }

    @Test
    public void get_resource_paths() throws Exception
    {
        ServletContext servletContext = newServletContext();

        train_getResourcePaths(servletContext, "/foo", "/foo/alpha.tml", "/foo/beta/", "/foo/gamma.tml");
        train_getResourcePaths(servletContext, "/foo/beta/", "/foo/beta/b.tml", "/foo/beta/a.tml", "/foo/beta/c/");
        train_getResourcePaths(servletContext, "/foo/beta/c/", "/foo/beta/c/c.tml");

        replay();

        List<String> actual = new ContextImpl(servletContext).getResourcePaths("/foo");

        assertEquals(actual, Arrays.asList("/foo/alpha.tml", "/foo/beta/a.tml", "/foo/beta/b.tml", "/foo/beta/c/c.tml",
                                           "/foo/gamma.tml"));

        verify();
    }

    @Test
    public void get_attribute()
    {
        String name = "foo";
        Object value = new Object();

        ServletContext servletContext = newServletContext();

        expect(servletContext.getAttribute(name)).andReturn(value);

        replay();

        Context context = new ContextImpl(servletContext);

        assertSame(context.getAttribute(name), value);

        verify();
    }

    /**
     * Tomcat 5.5.20 appears to sometimes return null if it can't find a match.
     */
    @Test
    public void ignore_null_from_get_resource_paths() throws Exception
    {
        ServletContext servletContext = newServletContext();

        expect(servletContext.getResourcePaths("/foo")).andReturn(null);

        replay();

        List<String> actual = new ContextImpl(servletContext).getResourcePaths("/foo");

        assertTrue(actual.isEmpty());

        verify();

    }

    @Test
    public void get_real_file_exists() throws IOException
    {
        String path = "/foo.gif";
        File file = File.createTempFile("foo", "gif");
        String realPath = file.getPath();

        ServletContext servletContext = newServletContext();

        train_getRealPath(servletContext, path, realPath);

        replay();

        Context c = new ContextImpl(servletContext);

        File f = c.getRealFile(path);

        assertEquals(f, file);


        verify();
    }

    @Test
    public void get_real_file_missing()
    {
        String path = "/foo.gif";

        ServletContext servletContext = newServletContext();

        train_getRealPath(servletContext, path, null);

        replay();

        Context c = new ContextImpl(servletContext);

        assertNull(c.getRealFile(path));

        verify();
    }

    private void train_getRealPath(ServletContext servletContext, String path, String realPath)
    {
        expect(servletContext.getRealPath(path)).andReturn(realPath);
    }

    protected final ServletContext newServletContext()
    {
        return newMock(ServletContext.class);
    }

    protected final void train_getResourcePaths(ServletContext context, String path, String... paths)
    {
        Set<String> set = CollectionFactory.newSet(Arrays.asList(paths));

        expect(context.getResourcePaths(path)).andReturn(set);
    }
}
