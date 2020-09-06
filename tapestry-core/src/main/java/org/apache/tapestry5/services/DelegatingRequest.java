// Copyright 2009, 2010, 2011, 2012 The Apache Software Foundation
//
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

package org.apache.tapestry5.services;

import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Session;

/**
 * Class that wraps an {@linkplain Request}, delegating all its methods.
 * 
 * @since 5.1.0.1
 */
public class DelegatingRequest implements Request
{

    private Request request;

    /**
     * No-arg constructor. It should only be used for testing purposes.
     */
    public DelegatingRequest()
    {
    }

    /**
     * Constructor that receives a {@linkplain Request}.
     * 
     * @param request
     *            a {@link Request}. It cannot be null.
     */
    public DelegatingRequest(Request request)
    {
        setRequest(request);
    }

    /**
     * Sets the delegate request.
     * 
     * @param request
     *            a {@link Request}. It cannot be null.
     */
    public void setRequest(Request request)
    {
        assert request != null;
        this.request = request;
    }

    public Object getAttribute(String name)
    {
        return request.getAttribute(name);
    }

    public List<String> getAttributeNames()
    {
        return request.getAttributeNames();
    }

    public String getContextPath()
    {
        return request.getContextPath();
    }

    public long getDateHeader(String name)
    {
        return request.getDateHeader(name);
    }

    public String getHeader(String name)
    {
        return request.getHeader(name);
    }

    public List<String> getHeaderNames()
    {
        return request.getHeaderNames();
    }

    public Locale getLocale()
    {
        return request.getLocale();
    }

    public String getMethod()
    {
        return request.getMethod();
    }

    public String getParameter(String name)
    {
        return request.getParameter(name);
    }

    public List<String> getParameterNames()
    {
        return request.getParameterNames();
    }

    public String[] getParameters(String name)
    {
        return request.getParameters(name);
    }

    public String getPath()
    {
        return request.getPath();
    }

    public String getServerName()
    {
        return request.getServerName();
    }

    public Session getSession(boolean create)
    {
        return request.getSession(create);
    }

    public boolean isRequestedSessionIdValid()
    {
        return request.isRequestedSessionIdValid();
    }

    public boolean isSecure()
    {
        return request.isSecure();
    }

    public boolean isXHR()
    {
        return request.isXHR();
    }

    public void setAttribute(String name, Object value)
    {
        request.setAttribute(name, value);
    }

    public int getLocalPort()
    {
        return request.getLocalPort();
    }

    public int getServerPort()
    {
        return request.getServerPort();
    }

    public String getRemoteHost()
    {
        return request.getRemoteHost();
    }

    public boolean isSessionInvalidated()
    {
        return request.isSessionInvalidated();
    }
}
