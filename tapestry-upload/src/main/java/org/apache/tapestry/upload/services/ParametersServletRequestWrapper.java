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

package org.apache.tapestry.upload.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry.services.Dispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * Wrapper for HttpServletRequest that overrides the parameter methods of the wrapped request. i.e.
 * parameters are retrieved from the wrapper rather than the real request.
 */
public class ParametersServletRequestWrapper extends HttpServletRequestWrapper
{
    private final Map<String, ParameterValue> _parameters = newMap();

    public ParametersServletRequestWrapper(HttpServletRequest httpServletRequest)
    {
        super(httpServletRequest);
    }

    @Override
    public String getParameter(String name)
    {
        return getValueFor(name).single();
    }

    @Override
    public Map<String, Object> getParameterMap()
    {
        Map<String, Object> paramMap = newMap();

        for (Map.Entry<String, ParameterValue> e : _parameters.entrySet())
        {
            ParameterValue value = e.getValue();

            paramMap.put(e.getKey(), value.isMulti() ? value.multi() : value.single());
        }

        return paramMap;
    }

    @Override
    public Enumeration getParameterNames()
    {
        return Collections.enumeration(_parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name)
    {
        return getValueFor(name).multi();
    }

    public void addParameter(String name, String value)
    {
        ParameterValue pv = _parameters.get(name);
        if (pv == null)
        {
            pv = new ParameterValue(value);
            _parameters.put(name, pv);
        }
        else
        {
            pv.add(value);
        }
    }

    ParameterValue getValueFor(String name)
    {
        ParameterValue value = _parameters.get(name);

        return value == null ? ParameterValue.NULL : value;
    }

    /**
     * Ignores any attempt to set the character encoding. Tapestry attempts to set the encoding
     * <em>after</em> the page name has been identified by the correct {@link Dispatcher}, and
     * that's too late from the perspective of the Servlet API as HttpServlet.getInputStream() will
     * already have been called.
     */
    @Override
    public void setCharacterEncoding(String enc) throws UnsupportedEncodingException
    {

    }
}
