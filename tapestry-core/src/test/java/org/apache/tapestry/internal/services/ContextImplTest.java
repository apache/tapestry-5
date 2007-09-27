// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.services.Context;
import org.testng.annotations.Test;

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

        train_getResourcePaths(
                servletContext,
                "/foo",
                "/foo/alpha.tml",
                "/foo/beta/",
                "/foo/gamma.tml");
        train_getResourcePaths(
                servletContext,
                "/foo/beta/",
                "/foo/beta/b.tml",
                "/foo/beta/a.tml",
                "/foo/beta/c/");
        train_getResourcePaths(servletContext, "/foo/beta/c/", "/foo/beta/c/c.tml");

        replay();

        List<String> actual = new ContextImpl(servletContext).getResourcePaths("/foo");

        assertEquals(actual, Arrays.asList(
                "/foo/alpha.tml",
                "/foo/beta/a.tml",
                "/foo/beta/b.tml",
                "/foo/beta/c/c.tml",
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

    /** Tomcat 5.5.20 appears to sometimes return null if it can't find a match. */
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

    protected final ServletContext newServletContext()
    {
        return newMock(ServletContext.class);
    }

    protected final void train_getResourcePaths(ServletContext context, String path,
            String... paths)
    {
        Set<String> set = CollectionFactory.newSet(Arrays.asList(paths));

        expect(context.getResourcePaths(path)).andReturn(set);
    }
}
