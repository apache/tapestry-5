// Copyright 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.Context;
import org.apache.tapestry5.services.ObjectRenderer;
import org.apache.tapestry5.services.Request;

import java.util.List;

public class RequestRenderer implements ObjectRenderer<Request>
{
    private final Context context;

    private final ObjectRenderer masterObjectRenderer;

    public RequestRenderer(@Primary ObjectRenderer masterObjectRenderer, Context context)
    {
        this.masterObjectRenderer = masterObjectRenderer;
        this.context = context;
    }

    public void render(Request request, MarkupWriter writer)
    {
        writer.element("dl");

        dt(writer, "Context Path");

        writer.element("dd");

        String contextPath = request.getContextPath();

        if (contextPath.equals(""))
        {
            writer.element("em");
            writer.write("none (deployed as root)");
            writer.end();
        }
        else
        {
            writer.write(contextPath);
        }
        writer.end(); // dd

        dt(writer, "Request Path");
        dd(writer, request.getPath());

        dt(writer, "Locale");
        dd(writer, request.getLocale().toString());

        dt(writer, "Secure");
        dd(writer, Boolean.toString(request.isSecure()));

        dt(writer, "Server Name");
        dd(writer, request.getServerName());

        writer.end();

        parameters(request, writer);
        headers(request, writer);
        context(writer);
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
            }
            else
            {
                writer.write(values[0]);
            }

            writer.end(); // dd
        }

        writer.end(); // dl
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
        writer.element("div", "class", InternalConstants.OBJECT_RENDER_DIV_SECTION);
        writer.write(name);
        writer.end();
    }

    private void headers(Request request, MarkupWriter writer)
    {
        section(writer, "Headers");

        writer.element("dl");

        for (String name : request.getHeaderNames())
        {
            dt(writer, name);
            dd(writer, request.getHeader(name));
        }

        writer.end(); // dl

    }

}
