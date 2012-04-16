// Copyright 2007, 2008, 2009, 2010, 2011, 2012 The Apache Software Foundation
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
import org.apache.tapestry5.corelib.SubmitMode;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Corresponds to &lt;input type="submit"&gt; or &lt;input type="image"&gt;, a client-side element that can force the
 * enclosing form to submit. The submit responsible for the form submission will post a notification that allows the
 * application to know that it was the responsible entity. The notification is named "selected" and has no context.
 *
 * @tapestrydoc
 */
@SupportsInformalParameters
@Events(EventConstants.SELECTED + " by default, may be overridden")
public class Submit implements ClientElement
{
    /**
     * If true (the default), then any notification sent by the component will be deferred until the end of the form
     * submission (this is usually desirable). In general, this can be left as the default except when the Submit
     * component is rendering inside a {@link Loop}, in which case defer should be bound to false (otherwise, the
     * event context will always be the final value of the Loop).
     */
    @Parameter
    private boolean defer = true;

    /**
     * The name of the event that will be triggered if this component is the cause of the form submission. The default
     * is {@link EventConstants#SELECTED}.
     */
    @Parameter(allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private String event = EventConstants.SELECTED;

    /**
     * If true, then the field will render out with a disabled attribute
     * (to turn off client-side behavior). When the form is submitted, the
     * bound value is evaluated again and, if true, the field's value is
     * ignored (not even validated) and the component's events are not fired.
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

    /**
     * Defines the mode, or client-side behavior, for the submit. The default is {@link SubmitMode#NORMAL}; clicking the
     * button submits the form with validation. {@link SubmitMode#CANCEL} indicates the form should be submitted as a cancel,
     * with no client-side validation. {@link SubmitMode#UNCONDITIONAL} bypasses client-side validation, but does not indicate
     * that the form was cancelled.
     *
     * @see EventConstants#CANCELED
     * @since 5.2.0
     */
    @Parameter(allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private SubmitMode mode = SubmitMode.NORMAL;

    @Environmental
    private FormSupport formSupport;

    @Environmental
    private Heartbeat heartbeat;

    @Inject
    private ComponentResources resources;

    @Inject
    private Request request;

    @Inject
    private JavaScriptSupport javascriptSupport;

    @SuppressWarnings("unchecked")
    @Environmental
    private TrackableComponentEventCallback eventCallback;

    private String clientId;

    private static class ProcessSubmission implements ComponentAction<Submit>
    {
        private final String clientId, elementName;

        public ProcessSubmission(String clientId, String elementName)
        {
            this.clientId = clientId;
            this.elementName = elementName;
        }

        public void execute(Submit component)
        {
            component.processSubmission(clientId, elementName);
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
        clientId = javascriptSupport.allocateClientId(resources);

        boolean isCancel = mode == SubmitMode.CANCEL;

        String name =
                isCancel ? InternalConstants.CANCEL_NAME :
                        formSupport.allocateControlName(resources.getId());

        // Save the element, to see if an id is later requested.

        String type = image == null ? "submit" : "image";

        writer.element("input",

                "type", type,

                "name", name,

                "id", clientId);

        if (disabled)
        {
            writer.attributes("disabled", "disabled");
        }

        if (image != null)
        {
            writer.attributes("src", image.toClientURL());
        }

        formSupport.store(this, new ProcessSubmission(clientId, name));

        resources.renderInformalParameters(writer);

        if (mode != SubmitMode.NORMAL)
        {
            javascriptSupport.addInitializerCall("enableBypassValidation", getClientId());
        }
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    void processSubmission(String clientId, String elementName)
    {
        if (disabled || !selected(clientId, elementName))
            return;

        Runnable sendNotification = new Runnable()
        {
            public void run()
            {
                // TAP5-1024: allow for navigation result from the event callback
                resources.triggerEvent(event, context, eventCallback);
            }
        };

        // When not deferred, don't wait, fire the event now (actually, at the end of the current
        // heartbeat). This is most likely because the Submit is inside a Loop and some contextual
        // information will change if we defer.

        if (defer)
            formSupport.defer(sendNotification);
        else
            heartbeat.defer(sendNotification);
    }

    private boolean selected(String clientId, String elementName)
    {
        // Case #1: via JavaScript, the client id is passed up.

        String raw = request.getParameter(Form.SUBMITTING_ELEMENT_ID);

        if (InternalUtils.isNonBlank(raw) &&
                new JSONArray(raw).getString(0).equals(clientId))
        {
            return true;
        }

        // Case #2: No JavaScript, look for normal semantic (non-null value for the element's name).
        // If configured as an image submit, look for a value for the x position. Ah, the ugliness
        // of HTML.

        String name = image == null ? elementName : elementName + ".x";

        String value = request.getParameter(name);

        return value != null;
    }

    /**
     * Returns the component's client id. This must be called after the component has rendered.
     *
     * @return client id for the component
     */
    public String getClientId()
    {
        return clientId;
    }
}
