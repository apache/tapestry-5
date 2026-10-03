// Copyright 2026 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.pages;

import java.util.List;

import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.UnknownActivationContextCheck;
import org.apache.tapestry5.annotations.WhitelistAccessOnly;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.AssembledPipeline;
import org.apache.tapestry5.ioc.services.PipelineBuilder;

/**
 * Page used to see the pipelines assembled by the {@link PipelineBuilder}: which filters are in each pipeline, and
 * in which order they are invoked.
 *
 * A pipeline is assembled when the service it implements is first realized, so this page shows more as the
 * application runs.
 *
 * @since 5.10
 */
@UnknownActivationContextCheck(false)
@WhitelistAccessOnly
public class Pipelines
{
    @Inject
    private PipelineBuilder pipelineBuilder;

    @Property
    @Inject
    @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
    private boolean productionMode;

    @Property
    private AssembledPipeline pipeline;

    @Property
    private int pipelineIndex;

    @Property
    private String filter;

    @Cached
    public List<AssembledPipeline> getPipelines()
    {
        return pipelineBuilder.getAssembledPipelines();
    }

    /**
     * Identifies the panel of the current pipeline, for the links at the top of the page. The index is used rather
     * than the service interface, since the same interface may be the basis of more than one pipeline.
     */
    public String getPipelineClientId()
    {
        return "pipeline-" + pipelineIndex;
    }
}
