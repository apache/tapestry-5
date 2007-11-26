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

import org.apache.tapestry.ioc.services.PipelineBuilder;
import org.apache.tapestry.services.MarkupRenderer;
import org.apache.tapestry.services.MarkupRendererFilter;
import org.apache.tapestry.services.PageRenderInitializer;
import org.slf4j.Logger;

import java.util.List;

public class PageRenderInitializerImpl implements PageRenderInitializer
{
    private final Logger _logger;

    private final PipelineBuilder _builder;

    private final List<MarkupRendererFilter> _configuration;

    public PageRenderInitializerImpl(Logger logger, PipelineBuilder builder, List<MarkupRendererFilter> configuration)
    {
        _builder = builder;
        _configuration = configuration;
        _logger = logger;
    }

    // In the scheme of things, this method is only called once. We've jumped through
    // some very special hoops to allow extensions to the rendering pipeline without
    // revealing any private classes, and this service is an important piece of that.

    public MarkupRenderer addFilters(MarkupRenderer renderer)
    {
        return _builder.build(_logger, MarkupRenderer.class, MarkupRendererFilter.class, _configuration, renderer);
    }
}
