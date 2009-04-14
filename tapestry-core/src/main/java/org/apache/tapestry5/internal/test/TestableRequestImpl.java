// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.Session;

import java.util.*;

public class TestableRequestImpl implements TestableRequest
{
    private final String contextPath;

    private final Map<String, Object> parameters = CollectionFactory.newMap();

    private final Map<String, Object> attributes = CollectionFactory.newMap();

    private Session session;

    private String path = "/";

    private Locale locale = Locale.getDefault();

    @Inject
    public TestableRequestImpl()
    {
        this("/foo");
    }

    public TestableRequestImpl(String contextPath)
    {
        this.contextPath = contextPath;
    }

    public TestableRequest clear()
    {
        parameters.clear();

        return this;
    }

    public TestableRequest setPath(String path)
    {
        this.path = path;

        return this;
    }

    public TestableRequest setLocale(Locale locale)
    {
        this.locale = locale;

        return this;
    }

    public TestableRequest loadParameter(String parameterName, String parameterValue)
    {
        Object existing = parameters.get(parameterName);

        if (existing == null)
        {
            parameters.put(parameterName, parameterValue);
            return this;
        }

        if (existing instanceof List)
        {
            ((List) existing).add(parameterValue);
            return this;
        }

        // Convert from a single String to a List of Strings.

        List list = new ArrayList();
        list.add(existing);
        list.add(parameterValue);

        parameters.put(parameterName, list);

        return this;
    }

    public TestableRequest overrideParameter(String parameterName, String parameterValue)
    {
        parameters.put(parameterName, parameterValue);

        return this;
    }

    public long getDateHeader(String name)
    {
        return 0;
    }

    /**
     * Returns null.
     */
    public String getHeader(String name)
    {
        return null;
    }

    /**
     * Returns an empty list.
     */
    public List<String> getHeaderNames()
    {
        return Collections.emptyList();
    }

    public Locale getLocale()
    {
        return locale;
    }

    public List<String> getParameterNames()
    {
        return InternalUtils.sortedKeys(parameters);
    }

    public String[] getParameters(String name)
    {
        Object value = parameters.get(name);

        if (value == null) return null;

        if (value instanceof String)
            return new String[] { (String) value };

        List list = (List) value;

        return (String[]) list.toArray(new String[list.size()]);
    }

    public String getPath()
    {
        return path;
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public String getParameter(String name)
    {
        Object value = parameters.get(name);

        if (value == null || value instanceof String) return (String) value;

        List<String> list = (List<String>) value;

        return list.get(0);
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

    /**
     * Returns "localhost" which is sufficient for testing purposes.
     */
    public String getServerName()
    {
        return "localhost";
    }

    /**
     * Always returns POST, to keep the Form component happy.
     */
    public String getMethod()
    {
        return "POST";
    }
}
