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

package org.apache.tapestry5.internal.services.ajax;

import org.apache.tapestry5.ClientBodyElement;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.internal.services.PageSource;
import org.apache.tapestry5.internal.services.RequestPageCache;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.PartialMarkupRenderer;
import org.apache.tapestry5.services.PartialMarkupRendererFilter;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JSONCallback;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;


public class AjaxResponseRendererImpl implements AjaxResponseRenderer
{
    private final PageRenderQueue queue;

    private final AjaxFormUpdateController ajaxFormUpdateController;

    private final TypeCoercer typeCoercer;

    private final JavaScriptSupport javaScriptSupport;

    private final RequestPageCache requestPageCache;

    private final Request request;

    public AjaxResponseRendererImpl(PageRenderQueue queue, AjaxFormUpdateController ajaxFormUpdateController, TypeCoercer typeCoercer, JavaScriptSupport javaScriptSupport, RequestPageCache requestPageCache, Request request)
    {
        this.queue = queue;
        this.ajaxFormUpdateController = ajaxFormUpdateController;
        this.typeCoercer = typeCoercer;
        this.javaScriptSupport = javaScriptSupport;
        this.requestPageCache = requestPageCache;
        this.request = request;
    }

    public AjaxResponseRenderer addRender(String clientId, Object renderer)
    {
        assert InternalUtils.isNonBlank(clientId);
        assert renderer != null;

        RenderCommand command = typeCoercer.coerce(renderer, RenderCommand.class);

        addFilter(new SingleZonePartialRendererFilter(clientId, command, queue, ajaxFormUpdateController));

        return this;
    }

    public AjaxResponseRenderer addRender(ClientBodyElement zone)
    {
        assert zone != null;

        final String clientId = zone.getClientId();

        if (clientId == null)
        {
            throw new IllegalArgumentException(
                    "Attempt to render a ClientBodyElement, probably a Zone, with a null clientId. "
                            + "You can solve this by using the id parameter.");
        }

        addRender(clientId, zone.getBody());

        return this;
    }

    public AjaxResponseRenderer addCallback(final JavaScriptCallback callback)
    {
        assert callback != null;

        addFilter(new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                renderer.renderMarkup(writer, reply);

                callback.run(javaScriptSupport);
            }
        });

        return this;
    }

    public AjaxResponseRenderer addCallback(final Runnable callback)
    {
        assert callback != null;

        addFilter(new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                renderer.renderMarkup(writer, reply);

                callback.run();
            }
        });


        return this;
    }

    private boolean isRedirect(JSONObject reply)
    {

        return reply.has(InternalConstants.PARTIAL_KEY) &&
                reply.in(InternalConstants.PARTIAL_KEY).has("redirectURL");
    }

    public AjaxResponseRenderer addFilter(final PartialMarkupRendererFilter filter)
    {
        assert filter != null;

        queue.addPartialMarkupRendererFilter(new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer delete)
            {
                if (isRedirect(reply))
                {
                    // Bypass the callback.
                    delete.renderMarkup(writer, reply);
                    return;
                }

                filter.renderMarkup(writer, reply, delete);
            }
        });

        return this;
    }

    public AjaxResponseRenderer addCallback(final JSONCallback callback)
    {
        assert callback != null;

        addFilter(new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                renderer.renderMarkup(writer, reply);

                callback.run(reply);
            }
        });

        return this;
    }

    @Override
    public void setupPartial(String pageName)
    {
        Page page = requestPageCache.get(pageName);

        queue.setRenderingPage(page);

        request.setAttribute(InternalConstants.PAGE_NAME_ATTRIBUTE_NAME, page.getName());
    }
}
