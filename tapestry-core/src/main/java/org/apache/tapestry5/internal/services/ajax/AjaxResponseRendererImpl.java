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
import org.apache.tapestry5.services.ajax.JSONCallback;
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

        addRender(zone.getClientId(), zone.getBody());

        return this;
    }

    public AjaxResponseRenderer addCallback(final JavaScriptCallback callback)
    {
        assert callback != null;

        addFilter(new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                callback.run(javaScriptSupport);

                renderer.renderMarkup(writer, reply);
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
                callback.run();

                renderer.renderMarkup(writer, reply);
            }
        });


        return this;
    }

    public AjaxResponseRenderer addFilter(PartialMarkupRendererFilter filter)
    {
        assert filter != null;

        queue.addPartialMarkupRendererFilter(filter);

        return this;
    }

    public AjaxResponseRenderer addCallback(final JSONCallback callback)
    {
        assert callback != null;

        addFilter(new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                callback.run(reply);

                renderer.renderMarkup(writer, reply);
            }
        });

        return this;
    }
}
