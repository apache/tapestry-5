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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.Stack;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.PartialMarkupRenderer;
import org.apache.tapestry5.services.PartialMarkupRendererFilter;
import org.slf4j.Logger;

/**
 * This services keeps track of the page being rendered and the root command for the partial render, it is therefore
 * request/thread scoped. There's a filter pipeline around the rendering, and that gets to be stateless because this
 * service, at the end of the pipeline, is stateful.
 */
@Scope(ScopeConstants.PERTHREAD)
public class PageRenderQueueImpl implements PageRenderQueue
{
    private final LoggerSource loggerSource;

    private Page page;

    private boolean partialRenderInitialized;

    private final Stack<PartialMarkupRendererFilter> filters = CollectionFactory.newStack();

    private RenderQueueImpl queue;

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
        setRenderingPage(page);

        queue.push(page.getRootElement());
    }

    public void setRenderingPage(Page page)
    {
        assert page != null;

        this.page = page;

        String name = "tapestry.render." + page.getLogger().getName();

        Logger logger = loggerSource.getLogger(name);

        queue = new RenderQueueImpl(logger);
    }

    public boolean isPartialRenderInitialized()
    {
        return partialRenderInitialized;
    }

    public void addPartialRenderer(RenderCommand renderer)
    {
        assert renderer != null;

        checkQueue();

        partialRenderInitialized = true;

        queue.push(renderer);
    }

    private void checkQueue()
    {
        if (queue == null) {
            throw new IllegalStateException("The page used as the basis for partial rendering has not been set.");
        }
    }

    public Page getRenderingPage()
    {
        return page;
    }

    public void render(MarkupWriter writer)
    {
        // Run the queue until empty.

        queue.run(writer);
    }

    public void addPartialMarkupRendererFilter(PartialMarkupRendererFilter filter)
    {
        assert filter != null;

        partialRenderInitialized = true;

        filters.push(filter);
    }

    public void renderPartial(MarkupWriter writer, JSONObject reply)
    {
        checkQueue();

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

        // The initialize methods will already have been invoked.

        delegate.renderMarkup(writer, reply);
    }
}
