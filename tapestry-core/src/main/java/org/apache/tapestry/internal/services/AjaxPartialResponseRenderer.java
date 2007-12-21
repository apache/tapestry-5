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

import org.apache.tapestry.runtime.RenderCommand;

import java.io.IOException;

/**
 * Used to render portions of a page as part of an
 * {@linkplain AjaxComponentActionRequestHandler Ajax request}.    This encapsulates
 * rendering of the partial response and then the construction of a {@linkplain org.apache.tapestry.json.JSONObject JSON reply}.
 * Works with the pipeline defined by the {@link org.apache.tapestry.services.PartialMarkupRenderer} service.
 *
 * @see org.apache.tapestry.internal.services.PageRenderQueue
 */
public interface AjaxPartialResponseRenderer
{
    /**
     * Used to render a partial response as part of an Ajax action request.
     *
     * @param renderCommand command that will be executed to render the content
     */
    void renderPartialPageMarkup(RenderCommand renderCommand) throws IOException;
}
