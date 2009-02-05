// Copyright 2009 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.Response;

import java.util.List;
import java.util.Map;

public class LinkImpl implements Link
{
    private Map<String, String> parameters;

    private final String absoluteURI;

    private final boolean optimizable;

    private final boolean forForm;

    private final Response response;

    private final RequestPathOptimizer optimizer;

    private String anchor;

    public LinkImpl(String absoluteURI, boolean optimizable, boolean forForm, Response response,
                     RequestPathOptimizer optimizer)
    {
        this.absoluteURI = absoluteURI;
        this.optimizable = optimizable;
        this.forForm = forForm;
        this.response = response;
        this.optimizer = optimizer;
    }

    public void addParameter(String parameterName, String value)
    {
        Defense.notBlank(parameterName, "parameterName");
        Defense.notBlank(value, "value");

        if (parameters == null)
            parameters = CollectionFactory.newMap();

        parameters.put(parameterName, value);
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
        return InternalUtils.get(parameters, name);
    }

    public void setAnchor(String anchor)
    {
        this.anchor = anchor;
    }

    public String toAbsoluteURI()
    {
        return appendAnchor(response.encodeURL(buildURI()));
    }

    public String toRedirectURI()
    {
        return appendAnchor(response.encodeRedirectURL(buildURI()));
    }

    public String toURI()
    {
        String path = buildURI();

        if (optimizable)
            path = optimizer.optimizePath(path);

        return appendAnchor(response.encodeURL(path));
    }

    private String appendAnchor(String path)
    {
        return InternalUtils.isBlank(anchor)
               ? path
               : path + "#" + anchor;
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
    private String buildURI()
    {
        if (forForm || parameters == null)
            return absoluteURI;

        StringBuilder builder = new StringBuilder(absoluteURI.length() * 2);

        builder.append(absoluteURI);

        String sep = "?";

        for (String name : getParameterNames())
        {
            String value = parameters.get(name);

            builder.append(sep);

            // We assume that the name is URL safe and that the value will already have been URL
            // encoded if it is not known to be URL safe.

            builder.append(name);
            builder.append("=");
            builder.append(value);

            sep = "&";
        }

        return builder.toString();
    }
}
