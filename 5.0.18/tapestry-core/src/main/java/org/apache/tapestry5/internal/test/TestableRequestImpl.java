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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Session;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TestableRequestImpl implements TestableRequest
{
    private final String contextPath;

    private final Map<String, String> parameters = CollectionFactory.newMap();

    private final Map<String, Object> attributes = CollectionFactory.newMap();

    private Session session;

    @Inject
    public TestableRequestImpl()
    {
        this("/foo");
    }

    public TestableRequestImpl(String contextPath)
    {
        this.contextPath = contextPath;
    }

    private <T> T nyi(String methodName)
    {
        throw new RuntimeException(
                String.format("Request: method %s() not yet implemented by TestableRequestImpl.", methodName));
    }

    public void clear()
    {
        parameters.clear();
    }

    public void loadParameter(String parameterName, String parameterValue)
    {
        parameters.put(parameterName, parameterValue);
    }

    public void loadParameters(Map<String, String> parameterValues)
    {
        parameters.putAll(parameterValues);
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
        return InternalUtils.sortedKeys(parameters);
    }

    public String[] getParameters(String name)
    {
        String value = getParameter(name);

        return value == null ? null : new String[] {value};
    }

    public String getPath()
    {
        return nyi("getPath");
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public String getParameter(String name)
    {
        return parameters.get(name);
    }

    public Session getSession(boolean create)
    {
        if (!create) return session;

        if (session == null) session = new PageTesterSession();

        return session;
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
        return attributes.get(name);
    }

    public void setAttribute(String name, Object value)
    {
        attributes.put(name, value);
    }

    public String getServerName()
    {
        return nyi("getServerName");
    }

    /**
     * Always returns POST, to keep the Form component happy.
     */
    public String getMethod()
    {
        return "POST";
    }
}
