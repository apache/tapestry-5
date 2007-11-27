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


/**
 * A wrapper around {@link org.apache.tapestry.runtime.RenderQueue}, but referencable as
 * a (per-thread) service.     This service is scoped so that we can tell it what to render
 * in one method, then have it do the render in another. Part of an elaborate
 * scheme to keep certain interfaces public and other closely related ones private.
 */
public interface PageRenderQueue
{
    /**
     * Initializes the queue for rendering of a complete page.
     */
    void initializeForCompletePage(Page page);

    /**
     * Initializes the queue for rendering of a portion of a page.
     */
    void initializeForPartialPageRender(RenderCommand rootCommand);

    /**
     * Render to the write, as setup by the initialize method.
     *
     * @param writer to write markup to
     */
    void render(MarkupWriter writer);
}
