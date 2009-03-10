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
package org.apache.tapestry5.urlrewriter;

import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;

/**
 * Class that wraps a {@linkplain Request}. It delegates all methods except ones related to URL
 * rewriting.
 */
public class SimpleRequestWrapper implements Request
{

    final private Request request;

    final private String path;

    final private String serverName;

    /**
     * Constructor that receives a request, a server name and a path.
     * 
     * @param request
     *            a {@link Request}. It cannot be null.
     * @param serverName
     *            a {@link String}.
     * @param path
     *            a {@link String}. It cannot be null.
     */
    public SimpleRequestWrapper(Request request, String serverName, String path)
    {
        Defense.notNull(request, "request");
        Defense.notNull(serverName, "serverName");
        Defense.notNull(path, "path");

        this.request = request;
        this.serverName = serverName;
        this.path = path;

    }

    /**
     * Constructor that receives a request and a path. The server name used is got
     * from the request.
     * 
     * @param request
     *            a {@link Request}. It cannot be null.
     * @param path
     *            a {@link String}. It cannot be null.
     */
    public SimpleRequestWrapper(Request request, String path) {
        
        Defense.notNull(request, "request");
        final String serverName = request.getServerName();
        Defense.notNull(serverName, "serverName");
        Defense.notNull(path, "path");

        this.request = request;
        this.serverName = serverName;
        this.path = path;
        
    }
    
    public String getPath()
    {
        return path;
    }

    public String getServerName()
    {
        return serverName;
    }

    public Object getAttribute(String name)
    {
        return request.getAttribute(name);
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

}
