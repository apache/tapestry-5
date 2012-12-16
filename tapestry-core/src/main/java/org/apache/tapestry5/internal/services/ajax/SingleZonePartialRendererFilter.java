// Copyright 2009-2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.ajax;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.PartialMarkupRenderer;
import org.apache.tapestry5.services.PartialMarkupRendererFilter;

/**
 * Responsible for capturing the content for a single zone and storing it into the JSON reply object. As a {@link PartialMarkupRendererFilter} , this
 * has access to the {@link JSONObject} for the reply, and can {@linkplain PageRenderQueue#addPartialRenderer(org.apache.tapestry5.runtime.RenderCommand) add renderers that generate and package the markup content}.
 *
 * @see org.apache.tapestry5.ajax.MultiZoneUpdate
 * @see org.apache.tapestry5.services.ajax.AjaxResponseRenderer#addRender(String, Object)
 * @since 5.1.0.1
 */
public class SingleZonePartialRendererFilter implements PartialMarkupRendererFilter
{
    private final String zoneId;

    private final RenderCommand zoneRenderCommand;

    private final PageRenderQueue queue;

    private final AjaxFormUpdateController ajaxFormUpdateController;

    public SingleZonePartialRendererFilter(String zoneId, RenderCommand zoneRenderCommand, PageRenderQueue queue,
                                           AjaxFormUpdateController ajaxFormUpdateController)
    {
        this.zoneId = zoneId;
        this.zoneRenderCommand = zoneRenderCommand;
        this.queue = queue;
        this.ajaxFormUpdateController = ajaxFormUpdateController;
    }

    public void renderMarkup(MarkupWriter writer, final JSONObject reply, PartialMarkupRenderer renderer)
    {
        RenderCommand forZone = new RenderCommand()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                // Create an element to contain the content for the zone. We give it a mnemonic
                // element name and attribute just to help with debugging (the element itself is discarded).

                final Element zoneContainer = writer.element("zone-update", "zoneId", zoneId);

                ajaxFormUpdateController.setupBeforePartialZoneRender(writer);

                queue.push(new RenderCommand()
                {
                    public void render(MarkupWriter writer, RenderQueue queue)
                    {
                        writer.end(); // the zoneContainer element

                        // Need to do this Ajax Form-related cleanup here, before we extract the zone content.

                        ajaxFormUpdateController.cleanupAfterPartialZoneRender();

                        String zoneUpdateContent = zoneContainer.getChildMarkup();

                        zoneContainer.remove();

                        // This has changed a bit in 5.4;
                        // In 5.3, it was just "zones", and was key/value pairs for id and content.
                        // In 5.4, it is "content", and is an array of id/content arrays
                        reply.in(InternalConstants.PARTIAL_KEY).append("content",
                                new JSONArray(zoneId, zoneUpdateContent));
                    }
                });

                // Make sure the zone's actual rendering command is processed first, then the inline
                // RenderCommand just above.

                queue.push(zoneRenderCommand);
            }
        };

        queue.addPartialRenderer(forZone);

        renderer.renderMarkup(writer, reply);
    }
}
