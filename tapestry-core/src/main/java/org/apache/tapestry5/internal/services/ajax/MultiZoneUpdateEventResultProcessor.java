// Copyright 2009-2014 The Apache Software Foundation
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

import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.internal.services.AjaxPartialResponseRenderer;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

import java.io.IOException;
import java.util.Map;

/**
 * Handler for {@link org.apache.tapestry5.ajax.MultiZoneUpdate} responses from a component event handler method. Works
 * by adding {@link SingleZonePartialRendererFilter}s for each zone to the
 * {@linkplain org.apache.tapestry5.internal.services.PageRenderQueue#addPartialMarkupRendererFilter(org.apache.tapestry5.services.PartialMarkupRendererFilter)
 * filter stack}. Each zone writes its content as a string in the zones object of the reply, keyed on its id.
 * JavaScript and CSS are collected for all zones rendered in the request (not for each individual zone). The final
 * response will have some combination of "script", "scripts", "stylesheets", "content" (which is expected to be blank)
 * and "zones".
 *
 * @since 5.1.0.1
 * @deprecated Deprecated in 5.3
 */
public class MultiZoneUpdateEventResultProcessor implements ComponentEventResultProcessor<MultiZoneUpdate>
{
    private final TypeCoercer typeCoercer;

    private final AjaxResponseRenderer ajaxResponseRenderer;

    private final AjaxPartialResponseRenderer partialRenderer;

    public MultiZoneUpdateEventResultProcessor(TypeCoercer typeCoercer, AjaxResponseRenderer ajaxResponseRenderer, AjaxPartialResponseRenderer partialRenderer)
    {
        this.typeCoercer = typeCoercer;
        this.ajaxResponseRenderer = ajaxResponseRenderer;
        this.partialRenderer = partialRenderer;
    }

    public void processResultValue(final MultiZoneUpdate value) throws IOException
    {

        Map<String, Object> map = value.getZoneToRenderMap();

        for (String zoneId : map.keySet())
        {
            Object provided = map.get(zoneId);

            // The AjaxResponseRenderer will convert the object to a RenderCommand, but does nothing special if there's a failure
            // (because the stack trace will clearly identify what's going on). We do the conversion here so that we can relate
            // a failure to a zone id. It will just be a pass-thru on the second type coercion.

            RenderCommand zoneRenderCommand = toRenderer(zoneId, provided);

            ajaxResponseRenderer.addRender(zoneId, zoneRenderCommand);
        }

        // This is actually executed deferred:

        partialRenderer.renderPartialPageMarkup();
    }

    private RenderCommand toRenderer(String zoneId, Object provided)
    {
        try
        {
            return typeCoercer.coerce(provided, RenderCommand.class);
        } catch (Exception ex)
        {
            throw new IllegalArgumentException(String.format("Failure converting renderer for zone '%s': %s", zoneId,
                    ExceptionUtils.toMessage(ex)), ex);
        }
    }
}
