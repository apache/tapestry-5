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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.services.Context;
import org.testng.annotations.Test;

import java.net.URL;

public class ContextResourceTest extends InternalBaseTestCase
{
    @Test
    public void get_url() throws Exception
    {
        URL url = getClass().getResource("ContextResourceTest.class");

        Context context = mockContext();

        expect(context.getResource("/foo/Bar.txt")).andReturn(url);

        replay();

        Resource r = new ContextResource(context, "foo/Bar.txt");

        assertSame(r.toURL(), url);

        verify();
    }

    @Test
    public void to_string()
    {
        Context context = mockContext();

        replay();

        Resource r = new ContextResource(context, "foo/Bar.txt");

        assertEquals(r.toString(), "context:foo/Bar.txt");

        verify();
    }

    @Test
    public void hash_code()
    {
        Context context1 = mockContext();
        Context context2 = mockContext();

        replay();

        Resource r1 = new ContextResource(context1, "foo");
        Resource r2 = new ContextResource(context1, "foo");
        Resource r3 = new ContextResource(context2, "foo");
        Resource r4 = new ContextResource(context1, "bar");

        assertTrue(r1.hashCode() == r2.hashCode());
        assertFalse(r1.hashCode() == r3.hashCode());
        assertFalse(r1.hashCode() == r4.hashCode());

        verify();
    }

    @Test
    public void equals()
    {
        Context context1 = mockContext();
        Context context2 = mockContext();
        Resource r = mockResource();

        replay();

        Resource r1 = new ContextResource(context1, "foo");
        Resource r2 = new ContextResource(context1, "foo");
        Resource r3 = new ContextResource(context2, "foo");
        Resource r4 = new ContextResource(context1, "bar");

        assertTrue(r1.equals(r2));
        assertFalse(r1.equals(r3));
        assertFalse(r1.equals(r4));

        assertFalse(r1.equals(null));
        assertTrue(r1.equals(r1));

        assertFalse(r1.equals(r));

        verify();
    }
}
