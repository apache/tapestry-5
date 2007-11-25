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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.runtime.RenderCommand;

/**
 * Service used to render page markup using a MarkupWriter.  This is usually used when rendering a complete
 * page as part of a {@linkplain org.apache.tapestry.internal.services.PageRenderRequestHandlerImpl page render request},
 * but may also be used to render portions of a page as part of an {@linkplain org.apache.tapestry.internal.services.AjaxResponseGenerator Ajax request}.
 */
public interface PageMarkupRenderer
{
    /**
     * Initializes the rendering using the
     * {@link org.apache.tapestry.services.PageRenderInitializer} change of command.
     *
     * @param page   page to render
     * @param writer receives the markup
     */
    void renderPageMarkup(Page page, MarkupWriter writer);

    /**
     * Used to render a partial response as part of an Ajax action request.
     *
     * @param page              page used to perform the render
     * @param rootRenderCommand initial object to render
     * @param writer            writer used to perform the render
     */
    void renderPartialPageMarkup(Page page, RenderCommand rootRenderCommand, MarkupWriter writer);

}
