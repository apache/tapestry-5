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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.http.internal.services.RequestImpl;
import org.apache.tapestry5.http.internal.services.TapestrySessionFactory;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
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
        TapestrySessionFactory sf = newMock(TapestrySessionFactory.class);

        expect(sf.getSession(false)).andReturn(null);

        replay();

        Request request = new RequestImpl(sr, CHARSET, sf);

        assertNull(request.getSession(false));

        verify();
    }

    @Test
    public void force_session_create()
    {
        HttpServletRequest sr = mockHttpServletRequest();
        TapestrySessionFactory sf = newMock(TapestrySessionFactory.class);
        Session session = mockSession();

        expect(sf.getSession(true)).andReturn(session);

        replay();

        Request request = new RequestImpl(sr, CHARSET, sf);

        assertSame(request.getSession(true), session);

        verify();
    }

    @Test
    public void used_encoding_from_request() throws Exception {
        HttpServletRequest sr = mockHttpServletRequest();

        expect(sr.getCharacterEncoding()).andReturn("request-encoding");

        sr.setCharacterEncoding("request-encoding");

        expect(sr.getParameterNames()).andReturn(Collections.enumeration(Collections.EMPTY_LIST));

        replay();

        new RequestImpl(sr, "app-encoding-is-ignored", null).getParameterNames();

        verify();
    }

    @Test
    public void set_encoding_success() throws Exception
    {
        HttpServletRequest sr = mockHttpServletRequest();

        String encoding = "the-encoding";

        sr.setCharacterEncoding(encoding);

        expect(sr.getCharacterEncoding()).andReturn(null);

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

        expect(sr.getCharacterEncoding()).andReturn(null);

        replay();

        try
        {
            new RequestImpl(sr, encoding, null).getParameterNames();
            unreachable();
        } catch (RuntimeException ex)
        {
            assertSame(ex.getCause(), exception);
        }

        verify();
    }

    @Test(dataProvider = "xhr_inputs")
    public void is_xhr_request(String headerValue, boolean expected)
    {
        HttpServletRequest sr = mockHttpServletRequest();

        expect(sr.getHeader(Request.REQUESTED_WITH_HEADER)).andReturn(headerValue);

        replay();

        Request request = new RequestImpl(sr, CHARSET, null);

        assertEquals(request.isXHR(), expected);

        verify();
    }

    @DataProvider
    public Object[][] xhr_inputs()
    {
        return new Object[][]
                {
                        {null, false},
                        {"", false},
                        {"some other value", false},
                        {"XMLHttpRequest", true}};
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


    protected final void train_getPathInfo(HttpServletRequest request, String pathInfo)
    {
        expect(request.getPathInfo()).andReturn(pathInfo).atLeastOnce();
    }

    @Test
    public void isSessionInvalidated_is_false_when_no_session_at_all()
    {
        HttpServletRequest sr = mockHttpServletRequest();

        TapestrySessionFactory sf = newMock(TapestrySessionFactory.class);

        expect(sf.getSession(false)).andReturn(null);

        replay();

        Request request = new RequestImpl(sr, CHARSET, sf);

        assertFalse(request.isSessionInvalidated());

        verify();
    }

    @Test
    public void isSessionInvalidated_is_false_when_session_exists_and_is_valid()
    {
        HttpServletRequest sr = mockHttpServletRequest();
        Session session = mockSession();

        TapestrySessionFactory sf = newMock(TapestrySessionFactory.class);

        expect(sf.getSession(false)).andReturn(session);
        expect(session.isInvalidated()).andReturn(false);

        replay();

        Request request = new RequestImpl(sr, CHARSET, sf);

        assertFalse(request.isSessionInvalidated());

        verify();
    }

    @Test
    public void isSessionInvalidated_is_true_when_session_is_invalid()
    {
        HttpServletRequest sr = mockHttpServletRequest();
        Session session = mockSession();

        TapestrySessionFactory sf = newMock(TapestrySessionFactory.class);

        expect(sf.getSession(false)).andReturn(session);
        expect(session.isInvalidated()).andReturn(true);

        replay();

        Request request = new RequestImpl(sr, CHARSET, sf);

        assertTrue(request.isSessionInvalidated());

        verify();
    }

    @Test
    public void request_secure_with_x_forwarded_proto() throws Exception
    {
        HttpServletRequest sr = mockHttpServletRequest();

        expect(sr.isSecure()).andReturn(false);
        expect(sr.getHeader(Request.X_FORWARDED_PROTO_HEADER)).andReturn("https");

        replay();

        Request request = new RequestImpl(sr, CHARSET, null);

        assertTrue(request.isSecure());

        verify();
    }
}
