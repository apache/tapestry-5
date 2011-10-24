// Copyright 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.corelib.data.InsertPosition;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ClientBehaviorSupport;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JSONCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.io.IOException;

/**
 * A way to add new content to an existing Form. The FormInjector emulates its tag from the template (or uses a
 * &lt;div&gt;). When triggered, new content is obtained from the application and is injected before or after the
 * element.
 * <p/>
 * On the client side, a new function, trigger(), is added to the element. Invoking this client-side function will
 * trigger the FormInjector; a request is sent to the server, new content is generated, and the new content is placed
 * before or after (per configuration) the existing FormInjector element.
 *
 * @tapestrydoc
 */
@SupportsInformalParameters
@Events(EventConstants.ACTION)
public class FormInjector implements ClientElement
{
    public static final String INJECT_EVENT = "inject";

    /**
     * The context for the link (optional parameter). This list of values will be converted into strings and included in
     * the URI. The strings will be coerced back to whatever their values are and made available to event handler
     * methods.
     */
    @Parameter
    private Object[] context;

    @Parameter(defaultPrefix = BindingConstants.LITERAL,
            value = BindingConstants.SYMBOL + ":" + ComponentParameterConstants.FORMINJECTOR_INSERT_POSITION)
    private InsertPosition position;

    /**
     * Name of a function on the client-side Tapestry.ElementEffect object that is invoked to make added content
     * visible. The default value is "highlight".
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL,
            value = BindingConstants.SYMBOL + ":" + ComponentParameterConstants.FORMINJECTOR_SHOW_FUNCTION)
    private String show;

    /**
     * The element name to render, which is normally the element name used to represent the FormInjector component in
     * the template, or "div".
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String element;

    @Environmental
    private JavaScriptSupport javascriptSupport;

    @Environmental
    private FormSupport formSupport;

    @Environmental
    private ClientBehaviorSupport clientBehaviorSupport;

    @SuppressWarnings("unchecked")
    @Environmental
    private TrackableComponentEventCallback eventCallback;

    private String clientId;

    @Inject
    private ComponentResources resources;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    private Element clientElement;

    String defaultElement()
    {
        return resources.getElementName("div");
    }

    void beginRender(MarkupWriter writer)
    {
        clientId = javascriptSupport.allocateClientId(resources);

        clientElement = writer.element(element, "id", clientId);

        resources.renderInformalParameters(writer);

        // Now work on the JavaScript side of things.

        Link link = resources.createEventLink(INJECT_EVENT, context);

        link.addParameter(RequestConstants.FORM_CLIENTID_PARAMETER, formSupport.getClientId());
        link.addParameter(RequestConstants.FORM_COMPONENTID_PARAMETER, formSupport.getFormComponentId());

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
     * Invoked via an Ajax request. Triggers an action event and captures the return value. The return value from the
     * event notification is what will ultimately render (typically, its a Block).
     */
    void onInject(EventContext context) throws IOException
    {
        ajaxResponseRenderer.addCallback(new JSONCallback()
        {
            public void run(JSONObject reply)
            {
                clientId = javascriptSupport.allocateClientId(resources);

                reply.put("elementId", clientId);
            }
        });

        resources.triggerContextEvent(EventConstants.ACTION, context, eventCallback);
    }
}
