// Copyright 2009, 2010 The Apache Software Foundation
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

import java.io.IOException;
import java.util.Map;

import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.internal.services.ajax.AjaxFormUpdateController;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.ComponentEventResultProcessor;

/**
 * Handler for {@link org.apache.tapestry5.ajax.MultiZoneUpdate} responses from a component event handler method. Works
 * by adding {@link org.apache.tapestry5.services.ajax.SingleZonePartialRendererFilter}s for each zone to the
 * {@linkplain org.apache.tapestry5.internal.services.PageRenderQueue#addPartialMarkupRendererFilter(org.apache.tapestry5.services.PartialMarkupRendererFilter)
 * filter stack}. Each zone writes its content as a string in the zones object of the reply, keyed on its id.
 * JavaScript and CSS are collected for all zones rendered in the request (not for each individual zone). The final
 * response will have some combination of "script", "scripts", "stylesheets", "content" (which is expected to be blank)
 * and "zones".
 * 
 * @since 5.1.0.1
 */
public class MultiZoneUpdateEventResultProcessor implements ComponentEventResultProcessor<MultiZoneUpdate>
{
    private final PageRenderQueue queue;

    private final TypeCoercer typeCoercer;

    private final AjaxFormUpdateController ajaxFormUpdateController;

    public MultiZoneUpdateEventResultProcessor(PageRenderQueue queue, TypeCoercer typeCoercer,
            AjaxFormUpdateController ajaxFormUpdateController)
    {
        this.queue = queue;
        this.typeCoercer = typeCoercer;
        this.ajaxFormUpdateController = ajaxFormUpdateController;
    }

    public void processResultValue(final MultiZoneUpdate value) throws IOException
    {
        queue.forcePartialRenderInitialized();
        queue.addPartialMarkupRendererFilter(new SetupZonesFilter());

        Map<String, Object> map = value.getZoneToRenderMap();

        for (String zoneId : map.keySet())
        {
            Object provided = map.get(zoneId);

            RenderCommand zoneRenderCommand = toRenderer(zoneId, provided);

            queue.addPartialMarkupRendererFilter(new SingleZonePartialRendererFilter(zoneId, zoneRenderCommand, queue,
                    ajaxFormUpdateController));
        }
    }

    private RenderCommand toRenderer(String zoneId, Object provided)
    {
        try
        {
            return typeCoercer.coerce(provided, RenderCommand.class);
        }
        catch (Exception ex)
        {
            throw new IllegalArgumentException(String.format("Failure converting renderer for zone '%s': %s", zoneId,
                    InternalUtils.toMessage(ex)), ex);
        }
    }
}
