// Copyright 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.corelib.data.InsertPosition;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.HiddenFieldPositioner;
import org.apache.tapestry5.corelib.internal.InternalFormSupport;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.services.ComponentResultProcessorWrapper;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * A way to add new content to an existing Form. The FormInjector emulates its tag from the template (or uses a
 * &lt;div&gt;). When triggered, new content is obtained from the application and is injected before or after the
 * element.
 * <p/>
 * On the client side, a new function, trigger(), is added to the element. Invoking this client-side function will
 * trigger the FormInjector; a request is sent to the server, new content is generated, and the new content is placed
 * before or after (per configuration) the existing FormInjector element.
 */
@SupportsInformalParameters
@Events(EventConstants.ACTION)
public class FormInjector implements ClientElement
{
    public static final String INJECT_EVENT = "inject";

    public static final String FORM_CLIENTID_PARAMETER = "t:formid";

    public static final String FORM_COMPONENTID_PARAMETER = "t:formcomponentid";

    /**
     * The context for the link (optional parameter). This list of values will be converted into strings and included in
     * the URI. The strings will be coerced back to whatever their values are and made available to event handler
     * methods.
     */
    @Parameter
    private Object[] context;

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

    @Environmental
    private Heartbeat heartbeat;

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

    @Inject
    private Logger logger;

    @Inject
    private ComponentSource componentSource;

    private Element clientElement;

    String defaultElement()
    {
        return resources.getElementName("div");
    }

    void beginRender(MarkupWriter writer)
    {
        clientId = renderSupport.allocateClientId(resources);

        clientElement = writer.element(element, "id", clientId);

        resources.renderInformalParameters(writer);

        // Now work on the JavaScript side of things.

        Link link = resources.createEventLink(INJECT_EVENT, context);

        link.addParameter(FORM_CLIENTID_PARAMETER, formSupport.getClientId());
        link.addParameter(FORM_COMPONENTID_PARAMETER, formSupport.getFormComponentId());

        clientBehaviorSupport.addFormInjector(clientId, link, position, show);
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();

        // Add the class name to the rendered client element. This allows nested elements to locate
        // the containing FormInjector element.

        clientElement.addClassName("t-forminjector");
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

    @Inject
    private ClientDataEncoder clientDataEncoder;

    /**
     * Invoked via an Ajax request.  Triggers an action event and captures the return value. The return value from the
     * event notification is what will ultimately render (typically, its a Block).  However, we do a <em>lot</em> of
     * tricks to provide the desired FormSupport around the what renders.
     */
    void onInject(EventContext context) throws IOException
    {
        ComponentResultProcessorWrapper callback = new ComponentResultProcessorWrapper(
                componentEventResultProcessor);

        resources.triggerContextEvent(EventConstants.ACTION, context, callback);

        if (!callback.isAborted()) return;

        // Here's where it gets very, very tricky.

        final String formClientId = readParameterValue(FORM_CLIENTID_PARAMETER);

        String formComponentId = request.getParameter(FORM_COMPONENTID_PARAMETER);

        final Form form = (Form) componentSource.getComponent(formComponentId);

        final ComponentActionSink actionSink = new ComponentActionSink(logger, clientDataEncoder);

        PartialMarkupRendererFilter filter = new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                hiddenFieldPositioner = new HiddenFieldPositioner(writer, rules);

                // Kind of ugly, but the only way to ensure we don't have name collisions on the
                // client side is to force a unique id into each name (as well as each id, but that's
                // RenderSupport's job).  It would be nice if we could agree on the uid, but
                // not essential.

                String uid = Long.toHexString(System.currentTimeMillis());

                IdAllocator idAllocator = new IdAllocator("-" + uid);

                clientId = renderSupport.allocateClientId(resources);

                reply.put("elementId", clientId);

                InternalFormSupport formSupport =
                        form.createRenderTimeFormSupport(formClientId, actionSink, idAllocator);

                environment.push(FormSupport.class, formSupport);
                environment.push(ValidationTracker.class, new ValidationTrackerImpl());

                heartbeat.begin();

                renderer.renderMarkup(writer, reply);

                formSupport.executeDeferred();

                heartbeat.end();

                environment.pop(ValidationTracker.class);
                environment.pop(FormSupport.class);

                hiddenFieldPositioner.getElement().attributes(
                        "type", "hidden",

                        "name", Form.FORM_DATA,

                        "value", actionSink.getClientData());
            }
        };

        pageRenderQueue.addPartialMarkupRendererFilter(filter);
    }

    private String readParameterValue(String parameterName)
    {
        String value = request.getParameter(parameterName);

        if (InternalUtils.isBlank(value))
            throw new RuntimeException(String.format(
                    "Query parameter '%s' was blank, but should have been specified in the request.",
                    parameterName));

        return value;
    }
}
