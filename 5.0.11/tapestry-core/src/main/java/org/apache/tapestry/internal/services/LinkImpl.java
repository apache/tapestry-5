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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Link;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.services.Response;

import java.util.List;

/**
 * Default implementation of {@link Link}.
 */
public class LinkImpl implements Link
{
    private static final int BUFFER_SIZE = 100;

    private final String _baseURL;

    private final String _contextPath;

    private final Response _response;

    private final RequestPathOptimizer _optimizer;

    private final ComponentInvocation _invocation;

    private final boolean _forForm;

    private String _anchor;

    LinkImpl(Response response, RequestPathOptimizer optimizer, String contextPath, String targetPath)
    {
        this(response, optimizer, contextPath, targetPath, false);
    }

    LinkImpl(Response response, RequestPathOptimizer optimizer, String contextPath, String targetPath, boolean forForm)
    {
        this(response, optimizer, null, contextPath,
             new ComponentInvocationImpl(new OpaqueConstantTarget(targetPath), new String[0], null), forForm);
    }

    /**
     * Creates a new Link.  Links may be full or optimized; optimization involves creating a relative URI from the
     * request's URI to the Link's URI.
     *
     * @param response    used to encode the response when necessary
     * @param optimizer   optimizes complete URLs to appropriate relative URLs
     * @param baseURL     base URL prefix (before the context path), used when switching between secure and non-secure
     * @param contextPath path for the context {@link org.apache.tapestry.services.Request#getContextPath()}
     * @param invocation  abstraction around the type of link (needed by {@link org.apache.tapestry.test.PageTester})
     * @param forForm     if true, then a Form has requested the Link, in which case, the link should not generated
     */
    public LinkImpl(Response response, RequestPathOptimizer optimizer, String baseURL, String contextPath,
                    ComponentInvocation invocation, boolean forForm)
    {
        _response = response;
        _optimizer = optimizer;
        _baseURL = baseURL;
        _contextPath = contextPath;
        _invocation = invocation;
        _forForm = forForm;
    }

    public void addParameter(String parameterName, String value)
    {
        _invocation.addParameter(parameterName, value);
    }

    public List<String> getParameterNames()
    {
        return _invocation.getParameterNames();
    }

    public String getParameterValue(String name)
    {
        return _invocation.getParameterValue(name);
    }

    public String toURI()
    {
        return _response.encodeURL(buildURI(false));
    }

    public String toAbsoluteURI()
    {
        return _response.encodeURL(buildURI(true));
    }

    private String buildURI(boolean full)
    {
        boolean absolute = full | _baseURL != null;

        StringBuilder builder = new StringBuilder(BUFFER_SIZE);

        if (_baseURL != null) builder.append(_baseURL);

        builder.append(_contextPath);
        builder.append("/");
        builder.append(_invocation.buildURI(_forForm));

        if (InternalUtils.isNonBlank(_anchor))
        {
            builder.append("#");
            builder.append(_anchor);
        }

        String fullURI = builder.toString();

        return absolute ? fullURI : _optimizer.optimizePath(fullURI);
    }

    public String toRedirectURI()
    {
        return _response.encodeRedirectURL(buildURI(true));
    }

    public String getAnchor()
    {
        return _anchor;
    }

    public void setAnchor(String anchor)
    {
        _anchor = anchor;
    }

    @Override
    public String toString()
    {
        return toURI();
    }
}
