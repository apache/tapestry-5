// Copyright 2007, 2008, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.renderers;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Context;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ObjectRenderer;

import java.util.List;

public class RequestRenderer implements ObjectRenderer<Request>
{
    private final Context context;

    private final String contextPath;

    private final ObjectRenderer masterObjectRenderer;

    public RequestRenderer(@Primary ObjectRenderer masterObjectRenderer, Context context, @Symbol(TapestryHttpSymbolConstants.CONTEXT_PATH) String contextPath)
    {
        this.masterObjectRenderer = masterObjectRenderer;
        this.context = context;
        this.contextPath = contextPath;
    }

    public void render(Request request, MarkupWriter writer)
    {
        coreProperties(request, writer);
        parameters(request, writer);
        headers(request, writer);
        attributes(request, writer);
        context(writer);
    }

    private void coreProperties(Request request, MarkupWriter writer)
    {
        writer.element("dl", "class", "dl-horizontal");

        dt(writer, "Context Path");

        writer.element("dd");

        if (contextPath.equals(""))
        {
            writer.element("em");
            writer.write("none (deployed as root)");
            writer.end();
        } else
        {
            writer.write(contextPath);
        }

        writer.end(); // dd

        dt(writer, "Path", request.getPath());

        dt(writer, "Locale", request.getLocale().toString());

        dt(writer, "Server Name", request.getServerName());


        List<String> flags = CollectionFactory.newList();
        if (request.isSecure())
        {
            flags.add("secure");
        }

        if (request.isXHR())
        {
            flags.add("XHR");
        }

        if (request.isRequestedSessionIdValid())
        {
            flags.add("requested session id valid");
        }

        if (request.isSessionInvalidated())
        {
            flags.add("session invalidated");
        }

        if (!flags.isEmpty())
        {
            dt(writer, "Flags", InternalUtils.join(flags));
        }

        dt(writer, "Ports (local/server)",
                String.format("%d / %d", request.getLocalPort(), request.getServerPort()));

        dt(writer, "Method", request.getMethod());

        writer.end();
    }

    private void context(MarkupWriter writer)
    {
        List<String> attributeNames = context.getAttributeNames();

        if (attributeNames.isEmpty()) return;

        section(writer, "Context Attributes");

        writer.element("dl");

        for (String name : attributeNames)
        {
            dt(writer, name);

            writer.element("dd");

            masterObjectRenderer.render(context.getAttribute(name), writer);

            writer.end(); // dd
        }

        writer.end(); // dl
    }

    private void parameters(Request request, MarkupWriter writer)
    {
        List<String> parameterNames = request.getParameterNames();

        if (parameterNames.isEmpty())
            return;

        section(writer, "Query Parameters");

        writer.element("dl");

        for (String name : parameterNames)
        {
            String[] values = request.getParameters(name);

            dt(writer, name);

            writer.element("dd");

            if (values.length > 1)
            {
                writer.element("ul");

                for (String value : values)
                {
                    writer.element("li");
                    writer.write(value);
                    writer.end();
                }

                writer.end(); // ul
            } else
            {
                writer.write(values[0]);
            }

            writer.end(); // dd
        }

        writer.end(); // dl
    }

    private void dt(MarkupWriter writer, String name, String value)
    {
        if (value != null)
        {
            dt(writer, name);
            dd(writer, value);
        }
    }

    private void dt(MarkupWriter writer, String name)
    {
        writer.element("dt");
        writer.write(name);
        writer.end();
    }

    private void dd(MarkupWriter writer, String name)
    {
        writer.element("dd");
        writer.write(name);
        writer.end();
    }

    private void section(MarkupWriter writer, String name)
    {
        writer.element("h3");
        writer.write(name);
        writer.end();
    }

    private void headers(Request request, MarkupWriter writer)
    {
        section(writer, "Headers");

        writer.element("dl", "class", "dl-horizontal");

        for (String name : request.getHeaderNames())
        {
            dt(writer, name, request.getHeader(name));
        }

        writer.end(); // dl

    }

    private void attributes(Request request, MarkupWriter writer)
    {
        List<String> attributeNames = request.getAttributeNames();

        if (attributeNames.isEmpty())
        {
            return;
        }

        section(writer, "Attributes");

        writer.element("dl");

        for (String name : attributeNames)
        {
            dt(writer, name, String.valueOf(request.getAttribute(name)));
        }

        writer.end(); // dl
    }

}
