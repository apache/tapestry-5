// Copyright 2007 The Apache Software Foundation
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

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.services.Session;

public class TestableRequestImpl implements TestableRequest
{
    private final String _contextPath;

    private final Map<String, String> _parameters = newMap();

    private Session _session;

    public TestableRequestImpl()
    {
        this("/foo");
    }

    public TestableRequestImpl(String contextPath)
    {
        _contextPath = contextPath;
    }

    private void nyi(String methodName)
    {
        throw new RuntimeException(String.format(
                "Request: method %s() not yet implemented by TestableRequestImpl.",
                methodName));
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
        nyi("getHeader");

        return null;
    }

    public List<String> getHeaderNames()
    {
        nyi("getHeaderNames");

        return null;
    }

    public Locale getLocale()
    {
        nyi("getLocale");

        return null;
    }

    public List<String> getParameterNames()
    {
        return InternalUtils.sortedKeys(_parameters);
    }

    public String[] getParameters(String name)
    {
        nyi("getParameters");

        return null;
    }

    public String getPath()
    {
        nyi("getPath");

        return null;
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

}
