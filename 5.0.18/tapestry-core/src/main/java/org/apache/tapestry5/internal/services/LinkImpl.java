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

import org.apache.tapestry5.Link;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.Response;

import java.util.List;

/**
 * Default implementation of {@link org.apache.tapestry5.Link}.
 */
public class LinkImpl implements Link
{
    private static final int BUFFER_SIZE = 100;

    private final String baseURL;

    private final String contextPath;

    private final Response response;

    private final RequestPathOptimizer optimizer;

    private final ComponentInvocation invocation;

    private String anchor;

    /**
     * Creates a new Link.  Links may be full or optimized; optimization involves creating a relative URI from the
     * request's URI to the Link's URI.
     *
     * @param response    used to encode the response when necessary
     * @param optimizer   optimizes complete URLs to appropriate relative URLs
     * @param baseURL     base URL prefix (before the context path), used when switching between secure and non-secure
     * @param contextPath path for the context {@link org.apache.tapestry5.services.Request#getContextPath()}
     * @param invocation  abstraction around the type of link (needed by {@link org.apache.tapestry5.test.PageTester})
     */
    public LinkImpl(Response response, RequestPathOptimizer optimizer, String baseURL, String contextPath,
                    ComponentInvocation invocation)
    {
        this.response = response;
        this.optimizer = optimizer;
        this.baseURL = baseURL;
        this.contextPath = contextPath;
        this.invocation = invocation;
    }

    public void addParameter(String parameterName, String value)
    {
        invocation.addParameter(parameterName, value);
    }

    public List<String> getParameterNames()
    {
        return invocation.getParameterNames();
    }

    public String getParameterValue(String name)
    {
        return invocation.getParameterValue(name);
    }

    public String toURI()
    {
        return response.encodeURL(buildURI(false));
    }

    public String toAbsoluteURI()
    {
        return response.encodeURL(buildURI(true));
    }

    private String buildURI(boolean full)
    {
        boolean absolute = full | baseURL != null;

        StringBuilder builder = new StringBuilder(BUFFER_SIZE);

        if (baseURL != null) builder.append(baseURL);

        builder.append(contextPath);

        String invocationURI = invocation.buildURI();

        if (invocationURI.length() > 0 || contextPath.length() == 0)
        {
            builder.append("/");

            builder.append(invocationURI);
        }

        if (InternalUtils.isNonBlank(anchor))
        {
            builder.append("#");
            builder.append(anchor);
        }

        String fullURI = builder.toString();

        return absolute ? fullURI : optimizer.optimizePath(fullURI);
    }

    public String toRedirectURI()
    {
        return response.encodeRedirectURL(buildURI(true));
    }

    public String getAnchor()
    {
        return anchor;
    }

    public void setAnchor(String anchor)
    {
        this.anchor = anchor;
    }

    @Override
    public String toString()
    {
        return toURI();
    }
}
