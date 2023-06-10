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
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Context;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ObjectRenderer;
import org.apache.tapestry5.services.pageload.PageClassLoaderContext;
import org.apache.tapestry5.services.pageload.PageClassLoaderContextManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RequestRenderer implements ObjectRenderer<Request>
{
    private final Context context;

    private final String contextPath;

    private final ObjectRenderer masterObjectRenderer;
    
    private final boolean productionMode;
    
    private final PageClassLoaderContextManager pageClassLoaderContextManager;
    
    private final PageSource pageSource;
    
    private final ComponentClassResolver componentClassResolver;

    public RequestRenderer(
            @Primary ObjectRenderer masterObjectRenderer, 
            Context context, 
            @Symbol(TapestryHttpSymbolConstants.CONTEXT_PATH) String contextPath,
            @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode,
            PageClassLoaderContextManager pageClassLoaderContextManager,
            PageSource pageSource,
            ComponentClassResolver componentClassResolver)
    {
        this.masterObjectRenderer = masterObjectRenderer;
        this.context = context;
        this.contextPath = contextPath;
        this.productionMode = productionMode;
        this.pageClassLoaderContextManager = pageClassLoaderContextManager;
        this.pageSource = pageSource;
        this.componentClassResolver = componentClassResolver;
    }

    public void render(Request request, MarkupWriter writer)
    {
        coreProperties(request, writer);
        parameters(request, writer);
        headers(request, writer);
        attributes(request, writer);
        context(writer);
        
//        pageClassloaderContext(writer);
//        pages(writer);
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

//    private void pageClassloaderContext(MarkupWriter writer) 
//    {
//        if (!productionMode)
//        {
//            section(writer, "Page Classloader Context");
//            writer.element("ul");
//            render(pageClassLoaderContextManager.getRoot(), writer);
//            writer.end(); // ul
//        }
//    }
//
//    private void render(PageClassloaderContext context, MarkupWriter writer) 
//    {
//        if (context != null)
//        {
//        
//            writer.element("li");
//            
//            writer.element("p");
//            writer.element("em");
//            writer.write(context.getName());
//            writer.write(", ");
//            writer.write(context.getClassLoader().toString());
//            writer.end(); // em
//            writer.end(); // p
//            
//            writer.element("p");
//            writer.write(context.getClassNames().stream().collect(Collectors.joining(", ")));
//            writer.end(); // p
//            
//            if (!context.getChildren().isEmpty())
//            {
//                writer.element("ul");
//                for (PageClassloaderContext child : context.getChildren())
//                {
//                    render(child, writer);
//                }
//                writer.end(); // ul
//            }
//            writer.end(); // li
//            
//        }
//        
//    }
//    
//    private void pages(MarkupWriter writer) 
//    {
//        if (!productionMode)
//        {
//            section(writer, "Pages");
//            writer.element("table", "class", "table table-condensed table-hover table-striped exception-report-threads");
//            writer.element("thead");
//            
//            writer.element("td");
//            writer.write("Name");
//            writer.end(); //td Name
//
//            writer.element("td");
//            writer.write("Context");
//            writer.end(); //td Context
//
//            writer.end(); // thead
//            
//            writer.element("tbody");
//            
//            List<Page> pages = new ArrayList<>(pageSource.getAllPages());
//            Collections.sort(pages, Comparator.comparing(Page::getName));
//            
//            for (Page page : pages) {
//                writer.element("tr");
//                writer.element("td");
//                writer.write(page.getName());
//                writer.end(); // td                
//                writer.element("td");
//                writer.write(pageClassLoaderContextManager.getRoot().findByClassName(componentClassResolver.getClassName(page.getName())).toString());
//                writer.end(); // td                
//                writer.end(); // tr
//            }
//            
//            writer.end(); // tbody
//            
//            writer.end(); // table
//        }        
//    }
    
}
