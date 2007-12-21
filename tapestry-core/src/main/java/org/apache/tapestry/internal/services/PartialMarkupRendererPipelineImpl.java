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
import org.apache.tapestry.json.JSONObject;
import org.apache.tapestry.services.PartialMarkupRenderer;
import org.apache.tapestry.services.PartialMarkupRendererFilter;
import org.slf4j.Logger;

import java.util.List;

/**
 * Represents a pipeline of {@link org.apache.tapestry.services.PartialMarkupRendererFilter}s that
 * terminates with {@link org.apache.tapestry.internal.services.PageRenderQueue#renderPartial(org.apache.tapestry.MarkupWriter, org.apache.tapestry.json.JSONObject)}.
 */
@Marker(Primary.class)
public class PartialMarkupRendererPipelineImpl implements PartialMarkupRenderer
{
    private final PartialMarkupRenderer _pipeline;

    public PartialMarkupRendererPipelineImpl(Logger logger, List<PartialMarkupRendererFilter> configuration,
                                             PipelineBuilder builder, final PageRenderQueue renderQueue)
    {

        PartialMarkupRenderer terminator = new PartialMarkupRenderer()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply)
            {
                renderQueue.renderPartial(writer, reply);
            }
        };

        _pipeline = builder.build(logger, PartialMarkupRenderer.class, PartialMarkupRendererFilter.class, configuration,
                                  terminator);
    }


    public void renderMarkup(MarkupWriter writer, JSONObject reply)
    {
        _pipeline.renderMarkup(writer, reply);
    }
}
