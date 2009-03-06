// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.services.ajax;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.PartialMarkupRenderer;
import org.apache.tapestry5.services.PartialMarkupRendererFilter;

/**
 * Responsible for capturing the content for a single zone and storing it into the JSON reply object.
 *
 * @see org.apache.tapestry5.ajax.MultiZoneUpdate
 * @since 5.1.0.1
 */
public class SingleZonePartialRendererFilter implements PartialMarkupRendererFilter
{
    private final String zoneId;

    private final RenderCommand zoneRenderCommand;

    private final PageRenderQueue queue;

    public SingleZonePartialRendererFilter(String zoneId, RenderCommand zoneRenderCommand, PageRenderQueue queue)
    {
        this.zoneId = zoneId;
        this.zoneRenderCommand = zoneRenderCommand;
        this.queue = queue;
    }

    public void renderMarkup(MarkupWriter writer, final JSONObject reply, PartialMarkupRenderer renderer)
    {
        RenderCommand forZone = new RenderCommand()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                // Create an element to contain the content for the zone. We give it a menumonic
                // element name and attribute just to help with debugging (the element itself is discarded).

                final Element zoneContainer = writer.element("zone-update", "zoneId", zoneId);

                queue.push(new RenderCommand()
                {
                    public void render(MarkupWriter writer, RenderQueue queue)
                    {
                        writer.end(); // the zoneContainer element

                        String zoneUpdateContent = zoneContainer.getChildMarkup();

                        zoneContainer.remove();

                        reply.getJSONObject("zones").put(zoneId, zoneUpdateContent);
                    }
                });

                // Make sure the zone's actual rendering command is processed first, then the inline
                // RenderCommand just above.

                queue.push(zoneRenderCommand);
            }
        };

        RenderCommand existing = queue.getRootRenderCommand();

        queue.initializeForPartialPageRender(new CombinedRenderCommand(existing, forZone));

        renderer.renderMarkup(writer, reply);
    }
}
