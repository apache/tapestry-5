// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.Collections;

public class RequestImplTest extends InternalBaseTestCase
{
    public static final String CHARSET = "UTF-8";

    @Test
    public void get_session_doesnt_exist()
    {
        HttpServletRequest sr = mockHttpServletRequest();

        train_getSession(sr, false, null);

        replay();

        Request request = new RequestImpl(sr, CHARSET, null);

        assertNull(request.getSession(false));

        verify();
    }

    @Test
    public void force_session_create()
    {
        HttpServletRequest sr = mockHttpServletRequest();
        HttpSession ss = mockHttpSession();

        train_getSession(sr, true, ss);

        train_getAttribute(ss, "foo", "bar");

        replay();

        Request request = new RequestImpl(sr, CHARSET, null);
        Session session = request.getSession(true);

        assertEquals(session.getAttribute("foo"), "bar");

        verify();
    }

    @Test
    public void set_encoding_success() throws Exception
    {
        HttpServletRequest sr = mockHttpServletRequest();

        String encoding = "the-encoding";

        sr.setCharacterEncoding(encoding);

        expect(sr.getParameterNames()).andReturn(Collections.enumeration(Collections.EMPTY_LIST));

        replay();

        new RequestImpl(sr, encoding, null).getParameterNames();

        verify();
    }

    @Test
    public void set_encoding_failure() throws Exception
    {
        HttpServletRequest sr = mockHttpServletRequest();

        String encoding = "the-encoding";
        UnsupportedEncodingException exception = new UnsupportedEncodingException("Oops.");

        sr.setCharacterEncoding(encoding);
        setThrowable(exception);

        replay();

        try
        {
            new RequestImpl(sr, encoding, null).getParameterNames();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertSame(ex.getCause(), exception);
        }

        verify();
    }

    @Test(dataProvider = "xhr_inputs")
    public void is_xhr_request(String headerValue, boolean expected)
    {
        HttpServletRequest sr = mockHttpServletRequest();

        expect(sr.getHeader(RequestImpl.REQUESTED_WITH_HEADER)).andReturn(headerValue);

        replay();

        Request request = new RequestImpl(sr, CHARSET, null);

        assertEquals(request.isXHR(), expected);

        verify();
    }

    @DataProvider
    public Object[][] xhr_inputs()
    {
        return new Object[][] { { null, false }, { "", false }, { "some other value", false },
                { "XMLHttpRequest", true } };
    }

    @Test
    public void get_path_for_normal_servlet_container()
    {
        String path = "/foo/bar";

        HttpServletRequest sr = mockHttpServletRequest();

        train_getPathInfo(sr, null);
        expect(sr.getServletPath()).andReturn(path);

        replay();

        Request request = new RequestImpl(sr, CHARSET, null);

        assertEquals(request.getPath(), path);

        verify();
    }

    /**
     * TAPESTRY-1713
     */
    @Test
    public void get_path_for_websphere_with_empty_path()
    {
        String path = "/foo/bar";

        HttpServletRequest sr = mockHttpServletRequest();

        train_getPathInfo(sr, path);

        replay();

        Request request = new RequestImpl(sr, CHARSET, null);

        assertEquals(request.getPath(), path);

        verify();
    }

    /**
     * TAPESTRY-1713
     */
    @Test
    public void get_path_for_websphere_with_nonempty_path()
    {
        HttpServletRequest sr = mockHttpServletRequest();

        train_getPathInfo(sr, "");

        replay();

        Request request = new RequestImpl(sr, CHARSET, null);

        assertEquals(request.getPath(), "/");

        verify();
    }

    @Test
    public void get_session_returns_null_if_invalid()
    {
        HttpServletRequest sr = mockHttpServletRequest();
        HttpSession hsession = mockHttpSession();

        train_getSession(sr, true, hsession);

        hsession.invalidate();

        replay();

        Request request = new RequestImpl(sr, CHARSET, null);

        Session session = request.getSession(true);

        session.invalidate();

        assertNull(request.getSession(false));

        assertSame(request.getSession(true), session);

        verify(); 
    }



    protected final void train_getPathInfo(HttpServletRequest request, String pathInfo)
    {
        expect(request.getPathInfo()).andReturn(pathInfo).atLeastOnce();
    }
}
