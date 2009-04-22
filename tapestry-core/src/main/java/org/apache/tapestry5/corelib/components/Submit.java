// Copyright 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.Request;

/**
 * Corresponds to &lt;input type="submit"&gt; or &lt;input type="image"&gt;, a client-side element that can force the
 * enclosing form to submit. The submit responsible for the form submission will post a notification that allows the
 * application to know that it was the responsible entity. The notification is named "selected" and has no context.
 */
@SupportsInformalParameters
@Events(EventConstants.SELECTED + " by default, may be overridden")
public class Submit implements ClientElement
{
    /**
     * If true (the default), then any notification sent by the component will be deferred until the end of the form
     * submission (this is usually desirable).
     */
    @Parameter
    private boolean defer = true;

    /**
     * The name of the event that will be triggered if this component is the cause of the form submission. The default
     * is "selected".
     */
    @Parameter(allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private String event = EventConstants.SELECTED;

    /**
     * If true, then the field will render out with a disabled attribute (to turn off client-side behavior). Further, a
     * disabled field ignores any value in the request when the form is submitted.
     */
    @Parameter("false")
    private boolean disabled;

    /**
     * The list of values that will be made available to event handler method of this component when the form is
     * submitted.
     *
     * @since 5.1.0.0
     */
    @Parameter
    private Object[] context;

    /**
     * If provided, the component renders an input tag with type "image". Otherwise "submit".
     *
     * @since 5.1.0.0
     */
    @Parameter(defaultPrefix = BindingConstants.ASSET)
    private Asset image;


    @Environmental
    private FormSupport formSupport;

    @Environmental
    private Heartbeat heartbeat;

    @Inject
    private ComponentResources resources;

    @Inject
    private Request request;

    @Inject
    private RenderSupport renderSupport;

    private Element element;

    private String clientId;

    private static class ProcessSubmission implements ComponentAction<Submit>
    {
        private final String elementName;

        public ProcessSubmission(String elementName)
        {
            this.elementName = elementName;
        }

        public void execute(Submit component)
        {
            component.processSubmission(elementName);
        }
    }

    public Submit()
    {
    }

    Submit(Request request)
    {
        this.request = request;
    }

    void beginRender(MarkupWriter writer)
    {
        clientId = null;

        String name = formSupport.allocateControlName(resources.getId());

        // Save the element, to see if an id is later requested.

        String type = image == null ? "submit" : "image";

        element = writer.element("input", "type", type, "name", name);

        if (disabled) writer.attributes("disabled", "disabled");

        if (image != null) writer.attributes("src", image.toClientURL());

        formSupport.store(this, new ProcessSubmission(name));

        resources.renderInformalParameters(writer);
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    void processSubmission(String elementName)
    {
        if (disabled) return;

        String value = request.getParameter(elementName);

        if (value == null) return;

        Runnable sendNotification = new Runnable()
        {
            public void run()
            {
                resources.triggerEvent(event, context, null);
            }
        };

        // When not deferred, don't wait, fire the event now (actually, at the end of the current
        // heartbeat). This is most likely because the Submit is inside a Loop and some contextual
        // information will change if we defer.

        if (defer) formSupport.defer(sendNotification);
        else heartbeat.defer(sendNotification);
    }

    // For testing:

    void setDefer(boolean defer)
    {
        this.defer = defer;
    }

    void setup(ComponentResources resources, FormSupport formSupport, Heartbeat heartbeat, RenderSupport renderSupport)
    {
        this.resources = resources;
        this.formSupport = formSupport;
        this.heartbeat = heartbeat;
        this.renderSupport = renderSupport;
    }

    /**
     * Returns the component's client id. This must be called after the component has rendered. The id is allocated
     * lazily (first time this method is invoked).
     *
     * @return client id for the component
     */
    public String getClientId()
    {
        if (clientId == null)
        {
            clientId = renderSupport.allocateClientId(resources);

            element.forceAttributes("id", clientId);
        }

        return clientId;
    }
}
