// Copyright 2009-2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.LinkSecurity;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ContextPathEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LinkImpl implements Link
{
    private Map<String, List<String>> parameters;

    private final String basePath;

    private final boolean forForm;

    private LinkSecurity defaultSecurity;

    private final Response response;

    private final ContextPathEncoder contextPathEncoder;

    private final BaseURLSource baseURLSource;

    private String anchor;

    public LinkImpl(String basePath, boolean forForm, LinkSecurity defaultSecurity, Response response,
                    ContextPathEncoder contextPathEncoder, BaseURLSource baseURLSource)
    {
        assert basePath != null;

        this.basePath = basePath;
        this.forForm = forForm;
        this.defaultSecurity = defaultSecurity;
        this.response = response;
        this.contextPathEncoder = contextPathEncoder;
        this.baseURLSource = baseURLSource;
    }

    public Link copyWithBasePath(String basePath)
    {
        LinkImpl copy = new LinkImpl(basePath, forForm, defaultSecurity, response, contextPathEncoder, baseURLSource);

        copy.anchor = anchor;

        for (String name : getParameterNames())
        {
            copy.addParameter(name, getParameterValues(name));
        }

        return copy;
    }

    private void addParameter(String parameterName, String[] value)
    {
        assert InternalUtils.isNonBlank(parameterName);
        if (parameters == null)
            parameters = new TreeMap<String, List<String>>();

        parameters.put(parameterName, Arrays.asList(value));
    }

    public Link addParameter(String parameterName, String value)
    {
        assert InternalUtils.isNonBlank(parameterName);

        if (parameters == null)
        {
            parameters = new TreeMap<String, List<String>>();
        }

        InternalUtils.addToMapList(parameters, parameterName, value == null ? "" : value);

        return this;
    }

    public String getBasePath()
    {
        return basePath;
    }

    public Link removeParameter(String parameterName)
    {
        assert InternalUtils.isNonBlank(parameterName);

        if (parameters != null)
        {
            parameters.remove(parameterName);
        }

        return this;
    }

    public String getAnchor()
    {
        return anchor;
    }

    public List<String> getParameterNames()
    {
        return InternalUtils.sortedKeys(parameters);
    }

    public String getParameterValue(String name)
    {
        List<String> values = InternalUtils.get(parameters, name);
        return values != null && !values.isEmpty() ? values.get(0) : null;
    }

    public Link setAnchor(String anchor)
    {
        this.anchor = anchor;

        return this;
    }

    public String toAbsoluteURI()
    {
        return buildAnchoredURI(defaultSecurity.promote());
    }

    public String toAbsoluteURI(boolean secure)
    {
        return buildAnchoredURI(secure ? LinkSecurity.FORCE_SECURE : LinkSecurity.FORCE_INSECURE);
    }

    public void setSecurity(LinkSecurity newSecurity)
    {
        assert newSecurity != null;

        defaultSecurity = newSecurity;
    }

    public LinkSecurity getSecurity()
    {
        return defaultSecurity;
    }

    public String toRedirectURI()
    {
        return appendAnchor(response.encodeRedirectURL(buildURI(defaultSecurity)));
    }

    public String toURI()
    {
        return buildAnchoredURI(defaultSecurity);
    }

    private String appendAnchor(String path)
    {
        return InternalUtils.isBlank(anchor) ? path : path + "#" + anchor;
    }

    private String buildAnchoredURI(LinkSecurity security)
    {
        return appendAnchor(response.encodeURL(buildURI(security)));
    }

    /**
     * Returns the value from {@link #toURI()}
     */
    @Override
    public String toString()
    {
        return toURI();
    }

    /**
     * Extends the absolute path with any query parameters. Query parameters are never added to a forForm link.
     *
     * @return absoluteURI appended with query parameters
     */
    private String buildURI(LinkSecurity security)
    {

        if (!security.isAbsolute() && (forForm || parameters == null))
            return basePath;

        StringBuilder builder = new StringBuilder(basePath.length() * 2);

        switch (security)
        {
            case FORCE_SECURE:
                builder.append(baseURLSource.getBaseURL(true));
                break;
            case FORCE_INSECURE:
                builder.append(baseURLSource.getBaseURL(false));
                break;
            default:
        }

        // The base URL (from BaseURLSource) does not end with a slash.
        // The basePath does (the context path begins with a slash or is blank, then there's
        // always a slash before the local name or page name.

        builder.append(basePath);

        if (!forForm)
        {
            String sep = basePath.contains("?") ? "&" : "?";

            for (String name : getParameterNames())
            {
                List<String> values = parameters.get(name);

                for (String value : values)
                {
                    builder.append(sep);

                    // We assume that the name is URL safe and that the value will already have been URL
                    // encoded if it is not known to be URL safe.

                    builder.append(name);
                    builder.append('=');
                    builder.append(value);

                    sep = "&";
                }
            }
        }

        return builder.toString();
    }

    public Link addParameterValue(String parameterName, Object value)
    {
        addParameter(parameterName, contextPathEncoder.encodeValue(value));

        return this;
    }

    public String[] getParameterValues(String parameterName)
    {
        List<String> values = InternalUtils.get(parameters, parameterName);
        return values.toArray(new String[values.size()]);
    }

}
