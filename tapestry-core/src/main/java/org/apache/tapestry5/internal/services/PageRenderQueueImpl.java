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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.util.Stack;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.PartialMarkupRenderer;
import org.apache.tapestry5.services.PartialMarkupRendererFilter;
import org.slf4j.Logger;

/**
 * This services keeps track of the page being rendered and the root command for the partial render, it is therefore
 * request/thread scoped.  There's a filter pipeline around the rendering, and that gets to be stateless because this
 * service, at the end of the pipeline, is stateful.
 */
@Scope(ScopeConstants.PERTHREAD)
public class PageRenderQueueImpl implements PageRenderQueue
{
    private final LoggerSource loggerSource;

    private Page page;

    private RenderCommand rootCommand;

    private final Stack<PartialMarkupRendererFilter> filters = CollectionFactory.newStack();

    private static class Bridge implements PartialMarkupRenderer
    {
        private final PartialMarkupRendererFilter filter;

        private final PartialMarkupRenderer delegate;

        private Bridge(PartialMarkupRendererFilter filter, PartialMarkupRenderer delegate)
        {
            this.filter = filter;
            this.delegate = delegate;
        }

        public void renderMarkup(MarkupWriter writer, JSONObject reply)
        {
            filter.renderMarkup(writer, reply, delegate);
        }
    }

    public PageRenderQueueImpl(LoggerSource loggerSource)
    {
        this.loggerSource = loggerSource;
    }

    public void initializeForCompletePage(Page page)
    {
        this.page = page;
        rootCommand = page.getRootElement();
    }


    public void setRenderingPage(Page page)
    {
        Defense.notNull(page, "page");

        this.page = page;
    }

    public boolean isPartialRenderInitialized()
    {
        return rootCommand != null;
    }

    public void initializeForPartialPageRender(RenderCommand rootCommand)
    {
        Defense.notNull(rootCommand, "rootCommand");

        if (page == null) throw new IllegalStateException("Page must be specified before root render command.");

        this.rootCommand = rootCommand;
    }

    public RenderCommand getRootRenderCommand()
    {
        return rootCommand;
    }

    public Page getRenderingPage()
    {
        return page;
    }

    public void render(MarkupWriter writer)
    {
        String name = "tapestry.render." + page.getLogger().getName();

        Logger logger = loggerSource.getLogger(name);

        RenderQueueImpl queue = new RenderQueueImpl(logger);

        queue.push(rootCommand);

        // Run the queue until empty.

        queue.run(writer);
    }

    public void addPartialMarkupRendererFilter(PartialMarkupRendererFilter filter)
    {
        Defense.notNull(filter, "filter");

        filters.push(filter);
    }

    public void renderPartial(MarkupWriter writer, JSONObject reply)
    {
        PartialMarkupRenderer terminator = new PartialMarkupRenderer()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply)
            {
                render(writer);
            }
        };

        PartialMarkupRenderer delegate = terminator;

        while (!filters.isEmpty())
        {
            PartialMarkupRendererFilter filter = filters.pop();

            PartialMarkupRenderer bridge = new Bridge(filter, delegate);

            delegate = bridge;
        }

        // The partial will quite often contain multiple elements (or just a block of plain text),
        // so those must be enclosed in a root element.

        Element root = writer.element("ajax-partial");

        // The initialize methods will already have been invoked.

        delegate.renderMarkup(writer, reply);

        writer.end();

        String content = root.getChildMarkup().trim();

        reply.put("content", content);
    }
}
