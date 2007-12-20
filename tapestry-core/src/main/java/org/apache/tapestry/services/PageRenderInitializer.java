// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.services;

/**
 * Service to which an ordered collection of {@link org.apache.tapestry.services.MarkupRendererFilter}s is
 * provided, which can then merge the filters with an actual {@link org.apache.tapestry.services.MarkupRenderer}.
 */
public interface PageRenderInitializer
{
    /**
     * Creates a new MarkupRenderer by wrapping the provided renderer with
     * the {@link org.apache.tapestry.services.MarkupRendererFilter}s configured for the service.
     *
     * @param renderer the renderer at the end of the filter chain
     * @return a new renderer that encapsulates the filter chain
     */
    MarkupRenderer addFilters(MarkupRenderer renderer);
}
