// Copyright 2007 The Apache Software Foundation
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
import org.apache.tapestry.runtime.RenderCommand;
import org.apache.tapestry.services.Environment;

public class PartialMarkupRendererImpl implements PartialMarkupRenderer
{
    private final Environment _environment;

    private final PageRenderQueue _pageRenderQueue;

    public PartialMarkupRendererImpl(Environment environment, PageRenderQueue pageRenderQueue)
    {
        _environment = environment;
        _pageRenderQueue = pageRenderQueue;
    }

    public void renderPartialPageMarkup(Page page, RenderCommand rootRenderCommand, MarkupWriter writer)
    {
        _environment.clear();

        _pageRenderQueue.initializeForPartialPageRender(page, rootRenderCommand);

        // No pipeline for partial rendering, yet.

        _pageRenderQueue.render(writer);
    }
}
