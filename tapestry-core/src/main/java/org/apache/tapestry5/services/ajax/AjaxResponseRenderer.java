// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.services.ajax;

import org.apache.tapestry5.ClientBodyElement;

/**
 * Manages the rendering of a partial page render as part of an Ajax response. This replaces
 * the {@link org.apache.tapestry5.ajax.MultiZoneUpdate} introduced in Tapestry 5.1. Much of the API is used to
 * queue behaviors that are deferred until the component event processing is completed, at which
 * point a partial page render takes place, using queued data to construct the overall response.
 * <p/>
 * The implementation of this class provides {@link org.apache.tapestry5.services.PartialMarkupRendererFilter} to
 * the {@link org.apache.tapestry5.internal.services.PageRenderQueue}.
 *
 * @since 5.3
 */
public interface AjaxResponseRenderer
{
    /**
     * Queues the renderer to render markup for the client-side element with the provided id.
     *
     * @param clientId client id of zone to update with the content from the renderer
     * @param renderer a {@link org.apache.tapestry5.Block}, {@link org.apache.tapestry5.runtime.Component} or other object that can be
     *                 {@linkplain org.apache.tapestry5.ioc.services.TypeCoercer coerced} to  {@link org.apache.tapestry5.runtime.RenderCommand}.
     */
    void render(String clientId, Object renderer);

    /**
     * Queues an update to the zone, using the zone's body as the new content.
     *
     * @param zone the element that contains both a client id and a body (this is primarily used to represent a {@link org.apache.tapestry5.corelib.components.Zone} component).
     */
    void render(ClientBodyElement zone);

    /**
     * Queues a callback to execute during the partial markup render.
     *
     * @param callback object to be invoked
     */
    void callback(JavaScriptCallback callback);
}