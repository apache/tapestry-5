// Copyright 2009 The Apache Software Foundation
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
package org.apache.tapestry5.urlrewriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.ioc.test.TestBase;
import org.apache.tapestry5.services.DelegatingRequest;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;
import org.testng.annotations.Test;

/**
 * Tests {@linkplain org.apache.tapestry5.urlrewritter.SimpleRequestWrapper}
 * and {@link DelegatingRequest}.
 */
public class SimpleRequestWrapperTest extends TestBase
{

    @Test
    public void delegating_methods() throws IOException
    {
        
        final String attributeName = "attributeName";
        final String attributeValue = "attributeValue";
        final String parameterName = "parameterName";
        final String parameterValue = "parameterValue";
        final String headerName = "headerName";
        final String headerValue = "headerValue";
        final String contextPath = "/contextPath";
        final String dateHeaderName = "dateHeader";
        final long dateHeader = 1234293875091l;
        final List<String> headerNames = new ArrayList<String>();
        final List<String> parameterNames = new ArrayList<String>();
        final Locale locale = new Locale("pt", "BR", "MG");
        final String method = "postget";
        final String[] parameters = new String[0];
        final Session session1 = newMock(Session.class);
        final Session session2 = newMock(Session.class);

        Request mock = newMock(Request.class);
        SimpleRequestWrapper request = new SimpleRequestWrapper(mock, "localhost", "path");
        
        mock.setAttribute(attributeName, attributeValue);
        expect(mock.getAttribute(attributeName)).andReturn(attributeValue);
        expect(mock.getContextPath()).andReturn(contextPath);
        expect(mock.getDateHeader(dateHeaderName)).andReturn(dateHeader);
        expect(mock.getHeader(headerName)).andReturn(headerValue);
        expect(mock.getHeaderNames()).andReturn(headerNames);
        expect(mock.getLocale()).andReturn(locale);
        expect(mock.getMethod()).andReturn(method);
        expect(mock.getParameter(parameterName)).andReturn(parameterValue);
        expect(mock.getParameterNames()).andReturn(parameterNames);
        expect(mock.getParameters(parameterName)).andReturn(parameters);
        expect(mock.getSession(false)).andReturn(session1);
        expect(mock.getSession(true)).andReturn(session2);
        expect(mock.isRequestedSessionIdValid()).andReturn(true);
        expect(mock.isXHR()).andReturn(false);
        expect(mock.isSecure()).andReturn(true);
        
        replay();

        request.setAttribute(attributeName, attributeValue);
        assertEquals(request.getAttribute(attributeName), attributeValue);
        assertEquals(request.getHeader(headerName), headerValue);
        assertEquals(request.getContextPath(), contextPath);
        assertEquals(request.getDateHeader(dateHeaderName), dateHeader);
        assertEquals(request.getHeaderNames(), headerNames);
        assertEquals(request.getLocale(), locale);
        assertEquals(request.getMethod(), method);
        assertEquals(request.getParameter(parameterName), parameterValue);
        assertEquals(request.getParameterNames(), parameterNames);
        assertEquals(request.getParameters(parameterName), parameters);
        assertEquals(request.getSession(false), session1);
        assertEquals(request.getSession(true), session2);
        assertEquals(request.isRequestedSessionIdValid(), true);
        assertEquals(request.isXHR(), false);
        assertEquals(request.isSecure(), true);
        
        verify();
        
    }

    @Test
    public void constructor_without_servername() {

        final String requestServerName = "tapestry.apache.org";
        final String path = "/tapestry/why";
        
        SimpleRequestWrapper request;
        Request mock = newMock(Request.class);
        
        expect(mock.getServerName()).andReturn(requestServerName);
        
        replay();
        
        request = new SimpleRequestWrapper(mock, path);
        
        verify();
        
        assertEquals(request.getServerName(), requestServerName);
        assertEquals(request.getPath(), path);

    }

    @Test
    public void constructor_with_servername() {

        final String serverName = "tapestry.apache.org";
        final String path = "/tapestry/why";
        
        SimpleRequestWrapper request;
        Request mock = newMock(Request.class);
        
        replay();
        
        request = new SimpleRequestWrapper(mock, serverName, path);
        
        assertEquals(request.getServerName(), serverName);
        assertEquals(request.getPath(), path);

    }

    @Test
    public void constructor_and_nulls() {

        Request request = newMock(Request.class);
        String serverName = "tapestry.apache.org";
        String path = "why";
        
        expect(request.getServerName()).andReturn(serverName).anyTimes();
        
        replay();
        
        testConstructorWithServerName(request, serverName, path, false);
        testConstructorWithServerName(null, serverName, path, true);
        testConstructorWithServerName(request, null, path, true);
        testConstructorWithServerName(request, serverName, null, true);
        
        testConstructorWithoutServerName(request, path, false);
        testConstructorWithoutServerName(null, path, true);
        testConstructorWithoutServerName(request, null, true);
        
        verify();
        
    }

    private void testConstructorWithServerName(Request request, String serverName, String path,
            boolean expectException)
    {
        
        boolean exceptionRaised = false;

        try {
            new SimpleRequestWrapper(request, serverName, path);
        }
        catch (RuntimeException e) {
            exceptionRaised = true;
        }

        assertEquals(expectException, exceptionRaised);
        
    }

    private void testConstructorWithoutServerName(Request request, String path,
            boolean expectException)
    {
        
        boolean exceptionRaised = false;

        try {
            new SimpleRequestWrapper(request, path);
        }
        catch (RuntimeException e) {
            exceptionRaised = true;
        }

        assertEquals(expectException, exceptionRaised);
        
    }

}
