// Copyright 2006 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.test.pagelevel.PageTester;

/**
 * Represents an invocation for a page or a component in the current application. This information
 * is extracted from incoming URLs for a running application (or created by the {@link PageTester}.
 * Each invocation may provide a context (Object[]) and parameters to the invocation target.
 */
public class ComponentInvocation
{
    private final Object[] _context;

    private Map<String, String> _parameters;

    private InvocationTarget _target;

    public ComponentInvocation(InvocationTarget target, Object[] context)
    {
        _target = target;
        _context = context;
    }

    /**
     * @return A path taking the format <em>target-path</em>/e1/e2?&q1=v1&q2=v2. where the
     *         <em>target-path</em> is the path provided by the invocation target; e1 and e2 are
     *         elements of the context; q1 and q2 are the parameters.
     */
    public String buildURI(boolean isForm)
    {
        String path = getPath();
        if (isForm || _parameters == null)
            return path;

        StringBuilder builder = new StringBuilder();

        builder.append(path);

        try
        {
            URLCodec codec = new URLCodec();

            String sep = "?";

            for (String name : getParameterNames())
            {
                String value = _parameters.get(name);

                builder.append(sep);

                // TODO: encode the parameter name?

                builder.append(name);
                builder.append("=");
                builder.append(codec.encode(value));

                sep = "&";
            }
        }
        catch (EncoderException ex)
        {
            throw new RuntimeException(ex);
        }

        return builder.toString();
    }

    /**
     * @return Just like the return value of {@link #buildURI(boolean)} except that parameters are
     *         not included.
     */
    private String getPath()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(_target.getPath());

        for (Object id : _context)
        {
            builder.append("/");

            // TODO: Need to encode this for URLs? What if the string contains slashes, etc.?

            builder.append(id.toString());
        }
        return builder.toString();
    }

    public Object[] getContext()
    {
        return _context;
    }

    public void addParameter(String parameterName, String value)
    {
        if (_parameters == null)
            _parameters = newMap();

        if (_parameters.containsKey(parameterName))
            throw new IllegalArgumentException(ServicesMessages.parameterNameMustBeUnique(
                    parameterName,
                    _parameters.get(parameterName)));

        _parameters.put(parameterName, value);
    }

    public List<String> getParameterNames()
    {
        return InternalUtils.sortedKeys(_parameters);
    }

    public String getParameterValue(String name)
    {
        return InternalUtils.get(_parameters, name);
    }

    public InvocationTarget getTarget()
    {
        return _target;
    }
}
