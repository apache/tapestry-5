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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.PartialMarkupRendererFilter;

/**
 * A wrapper around {@link org.apache.tapestry5.runtime.RenderQueue}, but referencable as a (per-thread) service. This
 * service is scoped so that we can tell it what to render in one method, then have it do the render in another. Part of
 * an elaborate scheme to keep certain interfaces public and other closely related ones private.
 */
public interface PageRenderQueue
{
    /**
     * Initializes the queue for rendering of a complete page.
     */
    void initializeForCompletePage(Page page);

    /**
     * Sets the default page that will render the response.
     */
    void setRenderingPage(Page page);

    /**
     * Returns the page that is rendering markup content.
     */
    Page getRenderingPage();

    /**
     * Adds a rendering command to the queue of rendering commands. This must not be invoked until after
     * the {@linkplain #setRenderingPage(org.apache.tapestry5.internal.structure.Page) rendering page has been identified}.
     *
     * @param renderer responsible for rendering a portion of the final markup
     */
    void addPartialRenderer(RenderCommand renderer);

    /**
     * Returns true if either {@link #addPartialRenderer(org.apache.tapestry5.runtime.RenderCommand)} or
     * {@link #addPartialMarkupRendererFilter(org.apache.tapestry5.services.PartialMarkupRendererFilter)}
     * has been invoked.
     */
    boolean isPartialRenderInitialized();

    /**
     * Render to the markup writer, as setup by the {@link #initializeForCompletePage(org.apache.tapestry5.internal.structure.Page)} or
     * {@link #addPartialRenderer(org.apache.tapestry5.runtime.RenderCommand)} methods.
     *
     * @param writer to write markup to
     */
    void render(MarkupWriter writer);

    /**
     * Performs a partial markup render, as configured via
     * {@link #addPartialRenderer(org.apache.tapestry5.runtime.RenderCommand)}.
     *
     * @param writer to which markup should be written
     * @param reply  JSONObject which will contain the partial response
     */
    void renderPartial(MarkupWriter writer, JSONObject reply);

    /**
     * Adds an optional filter to the rendering. Optional filters are <em>temporary</em>, used just during the current
     * partial render (as opposed to filters contributed to the
     * {@link org.apache.tapestry5.services.PartialMarkupRenderer} service which are permanent, shared and stateless.
     *
     * Filters are added to the <em>end</em> of the pipeline (after all permanent contributions).
     *
     * Filters will be executed in the order in which they are added.
     *
     * @param filter to add to the pipeline
     */
    void addPartialMarkupRendererFilter(PartialMarkupRendererFilter filter);
}
