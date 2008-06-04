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

import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

/**
 * Basic implementation of {@link org.apache.tapestry5.services.Request} that wraps around an {@link
 * javax.servlet.http.HttpServletRequest}.
 */
public class RequestImpl implements Request
{
    static final String REQUESTED_WITH_HEADER = "X-Requested-With";

    static final String XML_HTTP_REQUEST = "XMLHttpRequest";

    private final HttpServletRequest request;

    public RequestImpl(HttpServletRequest request)
    {
        this.request = request;
    }

    public List<String> getParameterNames()
    {
        return InternalUtils.toList(request.getParameterNames());
    }

    public List<String> getHeaderNames()
    {
        return InternalUtils.toList(request.getHeaderNames());
    }

    public String getParameter(String name)
    {
        return request.getParameter(name);
    }

    public String[] getParameters(String name)
    {
        return request.getParameterValues(name);
    }

    public String getHeader(String name)
    {
        return request.getHeader(name);
    }

    public String getPath()
    {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) return request.getServletPath();

        // Websphere 6.1 is a bit wonky (see TAPESTRY-1713), and tends to return the empty string
        // for the servlet path, and return the true path in pathInfo.

        return pathInfo.length() == 0 ? "/" : pathInfo;
    }

    public String getContextPath()
    {
        return request.getContextPath();
    }

    public Session getSession(boolean create)
    {
        HttpSession session = request.getSession(create);

        return session == null ? null : new SessionImpl(session);
    }

    public Locale getLocale()
    {
        return request.getLocale();
    }

    public long getDateHeader(String name)
    {
        return request.getDateHeader(name);
    }

    public void setEncoding(String requestEncoding)
    {
        try
        {
            request.setCharacterEncoding(requestEncoding);
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public boolean isXHR()
    {
        return XML_HTTP_REQUEST.equals(request.getHeader(REQUESTED_WITH_HEADER));
    }

    public boolean isSecure()
    {
        return request.isSecure();
    }

    public boolean isRequestedSessionIdValid()
    {
        return request.isRequestedSessionIdValid();
    }

    public Object getAttribute(String name)
    {
        return request.getAttribute(name);
    }

    public void setAttribute(String name, Object value)
    {
        request.setAttribute(name, value);
    }

    public String getServerName()
    {
        return request.getServerName();
    }
}
