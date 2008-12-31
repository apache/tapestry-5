// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.services.Session;
import org.testng.annotations.Test;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

public class SessionImplTest extends InternalBaseTestCase
{
    @Test
    public void get_attribute_names()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney"));
        HttpSession hs = mockHttpSession();

        expect(hs.getAttributeNames()).andReturn(e);

        replay();

        Session session = new SessionImpl(hs, null);

        assertEquals(session.getAttributeNames(), Arrays.asList("barney", "fred"));

        verify();
    }

    @Test
    public void get_attribute_names_by_prefix()
    {
        Enumeration e = Collections.enumeration(Arrays.asList("fred", "barney", "fanny"));
        HttpSession hs = mockHttpSession();

        expect(hs.getAttributeNames()).andReturn(e);

        replay();

        Session session = new SessionImpl(hs, null);

        assertEquals(session.getAttributeNames("f"), Arrays.asList("fanny", "fred"));

        verify();
    }

    @Test
    public void invalidate()
    {
        HttpSession hs = mockHttpSession();

        hs.invalidate();

        replay();

        Session session = new SessionImpl(hs, null);

        session.invalidate();

        verify();
    }

    @Test
    public void set_max_inactive()
    {
        HttpSession hs = mockHttpSession();
        int seconds = 999;

        hs.setMaxInactiveInterval(seconds);

        replay();

        Session session = new SessionImpl(hs, null);

        session.setMaxInactiveInterval(seconds);

        verify();
    }

    @Test
    public void get_max_inactive()
    {
        HttpSession hs = mockHttpSession();
        int seconds = 999;

        expect(hs.getMaxInactiveInterval()).andReturn(seconds);

        replay();

        Session session = new SessionImpl(hs, null);

        assertEquals(session.getMaxInactiveInterval(), seconds);

        verify();
    }
}
