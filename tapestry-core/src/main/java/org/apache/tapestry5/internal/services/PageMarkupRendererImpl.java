// Copyright 2006, 2007, 2008, 2010, 2011 The Apache Software Foundation
//
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
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.MarkupRenderer;

public class PageMarkupRendererImpl implements PageMarkupRenderer
{
    private final PageRenderQueue pageRenderQueue;

    private final MarkupRenderer markupRendererPipeline;

    public PageMarkupRendererImpl(MarkupRenderer markupRendererPipeline, PageRenderQueue pageRenderQueue)
    {
        // We have to go through some awkward tricks here:
        // - MarkupRenderer and MarkupRendererFilter are PUBLIC
        // - Page, PageMarkupRenderer, PageRenderQueue are PRIVATE
        // - This service is the bridge between public and private

        this.pageRenderQueue = pageRenderQueue;

        this.markupRendererPipeline = markupRendererPipeline;
    }

    public void renderPageMarkup(Page page, MarkupWriter writer)
    {
        // This is why the PRQ is scope perthread; we tell it what to render here ...

        pageRenderQueue.initializeForCompletePage(page);

        // ... then our statically fixed pipeline is able to (eventually) call into it.

        markupRendererPipeline.renderMarkup(writer);

        if (writer.getDocument().getRootElement() == null)
        {
            throw new RuntimeException(String.format("Page %s did not generate any markup when rendered. This could be because its template file could not be located, or because a render phase method in the page prevented rendering.", page.getName()));
        }
    }
}
