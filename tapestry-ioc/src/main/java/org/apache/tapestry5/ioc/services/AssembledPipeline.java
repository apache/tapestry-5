// Copyright 2026 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.ioc.internal.services.PipelineDescriber;

/**
 * A record of a pipeline assembled by {@link PipelineBuilder}: the service and filter interfaces it bridges,
 * the filters in the order they are invoked, and the terminator at the end of the chain.
 *
 * The filters and the terminator are recorded as descriptions, taken when the pipeline was assembled,
 * rather than as objects, so that a pipeline no longer in use is not kept alive by this record.
 *
 * @since 5.10.0
 */
public final class AssembledPipeline
{
    private final Class<?> serviceInterface;

    private final Class<?> filterInterface;

    private final List<String> filters;

    private final String terminator;

    public AssembledPipeline(Class<?> serviceInterface, Class<?> filterInterface, List<String> filters,
                             String terminator)
    {
        this.serviceInterface = serviceInterface;
        this.filterInterface = filterInterface;
        this.filters = Collections.unmodifiableList(new ArrayList<>(filters));
        this.terminator = terminator;
    }

    /**
     * The interface the pipeline implements.
     */
    public Class<?> getServiceInterface()
    {
        return serviceInterface;
    }

    /**
     * The interface implemented by each filter of the pipeline.
     */
    public Class<?> getFilterInterface()
    {
        return filterInterface;
    }

    /**
     * The filters, in the order they are invoked. Empty when the pipeline is just its terminator.
     */
    public List<String> getFilters()
    {
        return filters;
    }

    /**
     * What the last filter delegates to.
     */
    public String getTerminator()
    {
        return terminator;
    }

    @Override
    public String toString()
    {
        List<String> steps = new ArrayList<>(filters);

        steps.add(terminator);

        return PipelineDescriber.describe(PipelineDescriber.header(serviceInterface, filterInterface), steps);
    }
}
