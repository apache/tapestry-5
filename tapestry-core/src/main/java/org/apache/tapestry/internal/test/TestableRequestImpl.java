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

package org.apache.tapestry.internal.test;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.services.Session;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TestableRequestImpl implements TestableRequest
{
    private final String _contextPath;

    private final Map<String, String> _parameters = newMap();

    private final Map<String, Object> _attributes = newMap();

    private Session _session;

    public TestableRequestImpl()
    {
        this("/foo");
    }

    public TestableRequestImpl(String contextPath)
    {
        _contextPath = contextPath;
    }

    private <T> T nyi(String methodName)
    {
        throw new RuntimeException(
                String.format("Request: method %s() not yet implemented by TestableRequestImpl.", methodName));
    }

    public void clear()
    {
        _parameters.clear();
    }

    public void loadParameter(String parameterName, String parameterValue)
    {
        _parameters.put(parameterName, parameterValue);
    }

    public void loadParameters(Map<String, String> parameterValues)
    {
        _parameters.putAll(parameterValues);
    }

    public long getDateHeader(String name)
    {
        nyi("getDateHeader");
        return 0;
    }

    public String getHeader(String name)
    {
        return nyi("getHeader");
    }

    public List<String> getHeaderNames()
    {
        return nyi("getHeaderNames");
    }

    public Locale getLocale()
    {
        return nyi("getLocale");
    }

    public List<String> getParameterNames()
    {
        return InternalUtils.sortedKeys(_parameters);
    }

    public String[] getParameters(String name)
    {
        return nyi("getParameters");
    }

    public String getPath()
    {
        return nyi("getPath");
    }

    public String getContextPath()
    {
        return _contextPath;
    }

    public String getParameter(String name)
    {
        return _parameters.get(name);
    }

    public Session getSession(boolean create)
    {
        if (!create) return _session;

        if (_session == null) _session = new PageTesterSession();

        return _session;
    }

    public void setEncoding(String requestEncoding)
    {
    }

    /**
     * Always returns false. If you need to test Ajax functionality, you need to be using Selenium.
     */
    public boolean isXHR()
    {
        return false;
    }

    public boolean isSecure()
    {
        return false;
    }

    /**
     * Always returns true.
     */
    public boolean isRequestedSessionIdValid()
    {
        return true;
    }

    public Object getAttribute(String name)
    {
        return _attributes.get(name);
    }

    public void setAttribute(String name, Object value)
    {
        _attributes.put(name, value);
    }

    public String getServerName()
    {
        return nyi("getServerName");
    }
}
