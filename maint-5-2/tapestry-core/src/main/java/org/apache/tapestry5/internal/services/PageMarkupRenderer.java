// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.structure.Page;

/**
 * Service used to render page markup using a MarkupWriter.  This is  used when rendering a complete page as part of a
 * {@linkplain org.apache.tapestry5.internal.services.PageRenderRequestHandlerImpl page render request},
 */
public interface PageMarkupRenderer
{
    /**
     * Initializes the rendering using the {@link org.apache.tapestry5.services.MarkupRenderer} pipeline.
     *
     * @param page   page to render
     * @param writer receives the markup
     */
    void renderPageMarkup(Page page, MarkupWriter writer);
}
