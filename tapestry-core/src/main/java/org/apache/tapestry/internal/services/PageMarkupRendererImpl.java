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

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.MarkupRenderer;
import org.apache.tapestry.services.PageRenderInitializer;

public class PageMarkupRendererImpl implements PageMarkupRenderer
{
    private final Environment _environment;

    private final PageRenderQueue _pageRenderQueue;

    private final MarkupRenderer _markupRendererPipeline;

    public PageMarkupRendererImpl(PageRenderInitializer pageRenderInitializer, PageRenderQueue pageRenderQueue,
                                  Environment environment)
    {
        // We have to go through some awkward tricks here:
        // - MarkupRenderer and MarkupRendererFilter are PUBLIC
        // - Page, PageMarkupRenderer, PageRenderQueue are PRIVATE
        // - This service is the bridge between public and private


        _pageRenderQueue = pageRenderQueue;
        _environment = environment;

        MarkupRenderer renderer = new MarkupRenderer()
        {
            public void renderMarkup(MarkupWriter writer)
            {
                _pageRenderQueue.render(writer);
            }
        };

        _markupRendererPipeline = pageRenderInitializer.addFilters(renderer);
    }

    public void renderPageMarkup(Page page, MarkupWriter writer)
    {
        _environment.clear();

        // This is why the PRQ is scope perthread; we tell it what to render here ...

        _pageRenderQueue.initializeForCompletePage(page);

        // ... then our fixed pipeline is able to (eventually) call into it.

        _markupRendererPipeline.renderMarkup(writer);

        if (writer.getDocument().getRootElement() == null)
            throw new RuntimeException(ServicesMessages.noMarkupFromPageRender(page));
    }
}
