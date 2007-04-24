// Copyright 2006, 2007 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry.Link;

/**
 * Starting implementation of {@link Link}. Currently does not support query parameters.
 */
public class LinkImpl implements Link
{
    private final URLEncoder _encoder;

    private final String _contextPath;

    private final ComponentInvocation _invocation;

    private final boolean _forForm;

    public LinkImpl(URLEncoder encoder, String contextPath, String targetPath)
    {
        this(encoder, contextPath, targetPath, false);
    }

    public LinkImpl(URLEncoder encoder, String contextPath, String targetPath, boolean forForm)
    {
        this(encoder, contextPath, new ComponentInvocation(new OpaqueConstantTarget(targetPath),
                new String[0], null), forForm);
    }

    public LinkImpl(URLEncoder encoder, String contextPath, ComponentInvocation invocation,
            boolean forForm)
    {
        _contextPath = contextPath;
        _encoder = encoder;
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
        return _encoder.encodeURL(buildURI());
    }

    private String buildURI()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(_contextPath);
        builder.append("/");
        builder.append(_invocation.buildURI(_forForm));
        return builder.toString();
    }

    public String toRedirectURI()
    {
        return _encoder.encodeRedirectURL(buildURI());
    }

    public ComponentInvocation getInvocation()
    {
        return _invocation;
    }

    @Override
    public String toString()
    {
        return toURI();
    }
}
