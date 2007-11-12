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

import org.apache.tapestry.Link;
import org.apache.tapestry.services.Response;

import java.util.List;

/**
 * Default implementation of {@link Link}.
 */
public class LinkImpl implements Link
{
    private final Response _response;

    private final String _contextPath;

    private final ComponentInvocation _invocation;

    private final boolean _forForm;

    private String _anchor;

    public LinkImpl(Response encoder, String contextPath, String targetPath)
    {
        this(encoder, contextPath, targetPath, false);
    }

    public LinkImpl(Response encoder, String contextPath, String targetPath, boolean forForm)
    {
        this(encoder, contextPath, new ComponentInvocation(new OpaqueConstantTarget(targetPath),
                                                           new String[0], null), forForm);
    }

    public LinkImpl(Response encoder, String contextPath, ComponentInvocation invocation,
                    boolean forForm)
    {
        _contextPath = contextPath;
        _response = encoder;
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
        return _response.encodeURL(buildURI());
    }

    private String buildURI()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(_contextPath);
        builder.append("/");
        builder.append(_invocation.buildURI(_forForm));
        if (_anchor != null && _anchor.length() > 0)
        {
            builder.append("#");
            builder.append(_anchor);
        }
        return builder.toString();
    }

    public String toRedirectURI()
    {
        return _response.encodeRedirectURL(buildURI());
    }

    public String getAnchor()
    {
        return _anchor;
    }

    public void setAnchor(String anchor)
    {
        _anchor = anchor;
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
