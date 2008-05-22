// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.corelib.data.InsertPosition;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.FormSupportImpl;
import org.apache.tapestry5.corelib.internal.HiddenFieldPositioner;
import org.apache.tapestry5.internal.services.ClientBehaviorSupport;
import org.apache.tapestry5.internal.services.ComponentResultProcessorWrapper;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.*;

import java.io.IOException;
import java.util.List;

/**
 * A way to add new content to an existing Form. The FormInjector emulates its tag from the template (or uses a
 * &lt;div&gt;). When triggered, new content is obtained from the application and is injected before or after the
 * element.
 */
@SupportsInformalParameters
public class FormInjector implements ClientElement
{
    public static final String INJECT_EVENT = "inject";

    public static final String FORMID_PARAMETER = "t:formid";

    /**
     * The context for the link (optional parameter). This list of values will be converted into strings and included in
     * the URI. The strings will be coerced back to whatever their values are and made available to event handler
     * methods.
     */
    @Parameter
    private List<?> context;

    @Parameter(defaultPrefix = BindingConstants.LITERAL, value = "above")
    private InsertPosition position;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked to make added content
     * visible. Leaving as null uses the default function, "highlight".
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String show;

    /**
     * The element name to render, which is normally the element name used to represent the FormInjector component in
     * the template, or "div".
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String element;


    @Environmental
    private RenderSupport renderSupport;

    @Environmental
    private FormSupport formSupport;

    @Environmental
    private ClientBehaviorSupport clientBehaviorSupport;

    @Inject
    @Ajax
    private ComponentEventResultProcessor componentEventResultProcessor;


    @Inject
    private PageRenderQueue pageRenderQueue;

    private String clientId;

    @Inject
    private ComponentResources resources;

    @Inject
    private Request request;

    @Inject
    private Environment environment;

    String defaultElement()
    {
        return resources.getElementName("div");
    }

    void beginRender(MarkupWriter writer)
    {
        clientId = renderSupport.allocateClientId(resources);

        writer.element(element,

                       "id", clientId);

        resources.renderInformalParameters(writer);

        // Now work on the JavaScript side of things.

        Link link = resources.createActionLink(INJECT_EVENT, false,
                                               context == null ? new Object[0] : context.toArray());

        link.addParameter(FORMID_PARAMETER, formSupport.getClientId());

        clientBehaviorSupport.addFormInjector(clientId, link, position, show);
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }


    /**
     * Returns the unique client-side id of the rendered element.
     */
    public String getClientId()
    {
        return clientId;
    }

    /**
     * Used during Ajax partial rendering to identify the hidden field that will hold the form data (component actions,
     * used when processing the form submission) for the injection.
     */
    private HiddenFieldPositioner hiddenFieldPositioner;

    @Inject
    private HiddenFieldLocationRules rules;

    /**
     * Invoked via an Ajax request.  Triggers an action event and captures the return value. The return value from the
     * event notification is what will ultimately render (typically, its a Block).  However, we do a <em>lot</em> of
     * tricks to provide the desired FormSupport around the what renders.
     */
    Object onInject(EventContext context) throws IOException
    {
        ComponentResultProcessorWrapper callback = new ComponentResultProcessorWrapper(
                componentEventResultProcessor);

        resources.triggerContextEvent(EventConstants.ACTION, context, callback);

        if (!callback.isAborted()) return null;

        // Here's where it gets very, very tricky.

        final RenderCommand rootRenderCommand = pageRenderQueue.getRootRenderCommand();

        final String formId = request.getParameter(FORMID_PARAMETER);

        final ComponentActionSink actionSink = new ComponentActionSink();

        final RenderCommand cleanup = new RenderCommand()
        {
            public void render(MarkupWriter writer, RenderQueue queue)
            {
                environment.pop(ValidationTracker.class);

                FormSupportImpl formSupport = (FormSupportImpl) environment.pop(FormSupport.class);

                formSupport.executeDeferred();

                hiddenFieldPositioner.getElement().attributes(
                        "type", "hidden",

                        "name", Form.FORM_DATA,

                        "value", actionSink.toBase64());
            }
        };

        final RenderCommand setup = new RenderCommand()
        {
            public void render(final MarkupWriter writer, RenderQueue queue)
            {
                hiddenFieldPositioner = new HiddenFieldPositioner(writer, rules);

                // Kind of ugly, but the only way to ensure we don't have name collisions on the
                // client side is to force a unique id into each name (as well as each id, but that's
                // RenderSupport's job).  It would be nice if we could agree on the uid, but
                // not essential.

                String uid = Long.toHexString(System.currentTimeMillis());

                IdAllocator idAllocator = new IdAllocator(":" + uid);

                FormSupportImpl formSupport = new FormSupportImpl(formId, actionSink, clientBehaviorSupport, true,
                                                                  idAllocator);

                environment.push(FormSupport.class, formSupport);
                environment.push(ValidationTracker.class, new ValidationTrackerImpl());

                // Queue up the root render command to execute first, and the cleanup
                // to execute after it is done.

                queue.push(cleanup);
                queue.push(rootRenderCommand);
            }
        };

        return setup;
    }
}
