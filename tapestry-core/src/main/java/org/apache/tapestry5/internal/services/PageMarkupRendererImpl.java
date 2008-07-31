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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.MarkupRenderer;
import org.apache.tapestry5.services.Request;

public class PageMarkupRendererImpl implements PageMarkupRenderer
{
    private final Environment environment;

    private final PageRenderQueue pageRenderQueue;

    private final MarkupRenderer markupRendererPipeline;

    private final Request request;

    public PageMarkupRendererImpl(MarkupRenderer markupRendererPipeline, PageRenderQueue pageRenderQueue,
                                  Environment environment, Request request)
    {
        // We have to go through some awkward tricks here:
        // - MarkupRenderer and MarkupRendererFilter are PUBLIC
        // - Page, PageMarkupRenderer, PageRenderQueue are PRIVATE
        // - This service is the bridge between public and private


        this.pageRenderQueue = pageRenderQueue;
        this.environment = environment;

        this.markupRendererPipeline = markupRendererPipeline;
        this.request = request;
    }

    public void renderPageMarkup(Page page, MarkupWriter writer)
    {
        // Don't clear the environment when rendering a page to a document as we may be doing so when in the middle
        // of another render.

        if (request.getAttribute(InternalConstants.GENERATING_RENDERED_PAGE) == null)
            environment.clear();

        // This is why the PRQ is scope perthread; we tell it what to render here ...

        pageRenderQueue.initializeForCompletePage(page);

        // ... then our statically fixed pipeline is able to (eventually) call into it.

        markupRendererPipeline.renderMarkup(writer);

        if (writer.getDocument().getRootElement() == null)
            throw new RuntimeException(ServicesMessages.noMarkupFromPageRender(page));
    }
}
