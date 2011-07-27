package org.apache.tapestry5.internal.services.ajax;

import org.apache.tapestry5.ClientBodyElement;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.services.PartialMarkupRenderer;
import org.apache.tapestry5.services.PartialMarkupRendererFilter;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;


public class AjaxResponseRendererImpl implements AjaxResponseRenderer
{
    private final PageRenderQueue queue;

    private final AjaxFormUpdateController ajaxFormUpdateController;

    private final TypeCoercer typeCoercer;

    private final JavaScriptSupport javaScriptSupport;

    public AjaxResponseRendererImpl(PageRenderQueue queue, AjaxFormUpdateController ajaxFormUpdateController, TypeCoercer typeCoercer, JavaScriptSupport javaScriptSupport)
    {
        this.queue = queue;
        this.ajaxFormUpdateController = ajaxFormUpdateController;
        this.typeCoercer = typeCoercer;
        this.javaScriptSupport = javaScriptSupport;
    }

    public void render(String clientId, Object renderer)
    {
        assert InternalUtils.isNonBlank(clientId);
        assert renderer != null;

        RenderCommand command = typeCoercer.coerce(renderer, RenderCommand.class);

        queue.forcePartialRenderInitialized();
        queue.addPartialMarkupRendererFilter(new SingleZonePartialRendererFilter(clientId, command, queue, ajaxFormUpdateController));
    }

    public void render(ClientBodyElement zone)
    {
        assert zone != null;

        render(zone.getClientId(), zone.getBody());
    }

    public void callback(final JavaScriptCallback callback)
    {
        queue.forcePartialRenderInitialized();
        queue.addPartialMarkupRendererFilter(new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                callback.run(javaScriptSupport);
            }
        });
    }
}
