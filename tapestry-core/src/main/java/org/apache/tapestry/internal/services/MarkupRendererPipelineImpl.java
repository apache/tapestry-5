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
import org.apache.tapestry.ioc.annotations.Marker;
import org.apache.tapestry.ioc.annotations.Primary;
import org.apache.tapestry.ioc.services.PipelineBuilder;
import org.apache.tapestry.services.MarkupRenderer;
import org.apache.tapestry.services.MarkupRendererFilter;
import org.slf4j.Logger;

import java.util.List;

@Marker(Primary.class)
public class MarkupRendererPipelineImpl implements MarkupRenderer
{
    private final MarkupRenderer _pipeline;

    public MarkupRendererPipelineImpl(final PageRenderQueue pageRenderQueue, Logger logger, PipelineBuilder builder,
                                      List<MarkupRendererFilter> configuration)
    {
        MarkupRenderer terminator = new MarkupRenderer()
        {
            public void renderMarkup(MarkupWriter writer)
            {
                pageRenderQueue.render(writer);
            }
        };

        _pipeline = builder.build(logger, MarkupRenderer.class, MarkupRendererFilter.class, configuration, terminator);
    }

    public void renderMarkup(MarkupWriter writer)
    {
        _pipeline.renderMarkup(writer);
    }
}
