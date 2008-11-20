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

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ContextPathEncoder;

import java.util.List;
import java.util.Map;

/**
 * Represents an invocation for a page render or a component event, in the current application.
 */
public class ComponentInvocationImpl implements ComponentInvocation
{
    private final ContextPathEncoder encoder;

    private final String eventContextPath;

    private final InvocationTarget target;

    private final String pageActivationContextPath;

    private final boolean forForm;

    private Map<String, String> parameters;

    /**
     * @param encoder
     * @param target                identifies the target of the event: a component with a page
     * @param eventContext          event activation context (or null for a page render invocation)
     * @param pageActivationContext page activation context (may be null)
     * @param forForm               if true, the URL is rendered for a form
     */
    public ComponentInvocationImpl(ContextPathEncoder encoder, InvocationTarget target, Object[] eventContext,
                                   Object[] pageActivationContext,
                                   boolean forForm)
    {
        this.encoder = encoder;
        this.target = target;
        this.forForm = forForm;
        this.eventContextPath = eventContext == null ? null : encoder.encodeIntoPath(eventContext);
        this.pageActivationContextPath = encoder.encodeIntoPath(pageActivationContext);

        // For component events, the page activation context (if it exists) is a query parameter
        // not path info.

        if (eventContext != null && !InternalUtils.isBlank(this.pageActivationContextPath))
            addParameter(InternalConstants.PAGE_CONTEXT_NAME, this.pageActivationContextPath);
    }


    public String buildURI()
    {
        String path = getPath();

        if (forForm || parameters == null) return path;

        StringBuilder builder = new StringBuilder();

        builder.append(path);

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

    /**
     * Return the path which identifies the page (and perhaps component) plus the event or page activation context. This
     * is the {@linkplain InvocationTarget#getPath() target path} plus any extra path info.
     */
    private String getPath()
    {
        // For component event requests, the extra path info the the event context.  For page render requests,
        // the extra path info is the page activation context.

        String extraPath =
                eventContextPath != null ? eventContextPath : pageActivationContextPath;

        String targetPath = target.getPath();

        int lastSlash = targetPath.lastIndexOf('/');

        // Omit the "index" if the path ends with "/index".

        if (targetPath.substring(lastSlash + 1).equalsIgnoreCase("index"))
            targetPath = lastSlash < 0 ? "" : targetPath.substring(0, lastSlash);

        if (InternalUtils.isBlank(extraPath)) return targetPath;

        if (targetPath.length() == 0) return extraPath;

        return targetPath + "/" + extraPath;
    }

    public EventContext getEventContext()
    {
        return encoder.decodePath(eventContextPath);
    }

    public EventContext getPageActivationContext()
    {
        return encoder.decodePath(pageActivationContextPath);
    }

    public void addParameter(String parameterName, String value)
    {
        notBlank(parameterName, "parameterName");
        notBlank(value, "value");

        if (parameters == null) parameters = CollectionFactory.newMap();

        if (parameters.containsKey(parameterName)) throw new IllegalArgumentException(
                ServicesMessages.parameterNameMustBeUnique(parameterName, parameters.get(parameterName)));

        parameters.put(parameterName, value);
    }

    public List<String> getParameterNames()
    {
        return InternalUtils.sortedKeys(parameters);
    }

    public String getParameterValue(String name)
    {
        return InternalUtils.get(parameters, name);
    }

    public InvocationTarget getTarget()
    {
        return target;
    }
}
