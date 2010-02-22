// Copyright 2008, 2009, 2010 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.SubmitMode;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavascriptSupport;

/**
 * Generates a client-side hyperlink that submits the enclosing form. If the link is clicked in the browser, the
 * component will trigger an event ({@linkplain EventConstants#SELECTED selected} by default) , just like {@link Submit}
 * .
 */
@SupportsInformalParameters
@IncludeJavaScriptLibrary("linksubmit.js")
@Events(EventConstants.SELECTED + " by default, may be overridden")
public class LinkSubmit implements ClientElement
{
    /**
     * If true, then no link (or accompanying JavaScript) is written (though the body still is).
     */
    @Parameter
    private boolean disabled;

    /**
     * The name of the event that will be triggered if this component is the cause of the form submission. The default
     * is "selected".
     */
    @Parameter(allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private String event = EventConstants.SELECTED;

    /**
     * Defines the mode, or client-side behavior, for the submit. The default is {@link SubmitMode#NORMAL}; clicking the
     * button submits the form with validation. {@link SubmitMode#CANCEL} indicates the client-side validation
     * should be omitted (though server-side validation still occurs).
     * 
     * @since 5.2.0
     */
    @Parameter(allowNull = false, defaultPrefix = BindingConstants.LITERAL)
    private SubmitMode mode = SubmitMode.NORMAL;

    /**
     * If true (the default), then any notification sent by the component will be deferred until the end of the form
     * submission (this is usually desirable).
     */
    @Parameter
    private boolean defer = true;

    @Inject
    private ComponentResources resources;

    @Inject
    private JavascriptSupport javascriptSupport;

    @Environmental
    private FormSupport formSupport;

    @Environmental
    private Heartbeat heartbeat;

    @Inject
    private Request request;

    @SuppressWarnings("unchecked")
    @Environmental
    private TrackableComponentEventCallback eventCallback;

    private String clientId;

    private static class ProcessSubmission implements ComponentAction<LinkSubmit>
    {
        private final String clientId;

        public ProcessSubmission(String clientId)
        {
            this.clientId = clientId;
        }

        public void execute(LinkSubmit component)
        {
            component.processSubmission(clientId);
        }
    }

    private void processSubmission(String clientId)
    {
        this.clientId = clientId;

        String hiddenFieldName = this.clientId + "-hidden";

        if (request.getParameter(hiddenFieldName) != null)
        {
            Runnable notification = new Runnable()
            {
                public void run()
                {
                    resources.triggerEvent(event, null, eventCallback);
                }
            };

            if (defer)
                formSupport.defer(notification);
            else
                heartbeat.defer(notification);
        }
    }

    void beginRender(MarkupWriter writer)
    {
        if (!disabled)
        {
            clientId = javascriptSupport.allocateClientId(resources);

            formSupport.store(this, new ProcessSubmission(clientId));

            writer.element("a",

            "id", clientId,

            "href", "#");

            if (!request.isXHR())
                writer.attributes(MarkupConstants.ONCLICK, MarkupConstants.WAIT_FOR_PAGE);

            resources.renderInformalParameters(writer);
        }
    }

    void afterRender(MarkupWriter writer)
    {
        if (!disabled)
        {
            writer.end();

            JSONObject spec = new JSONObject("form", formSupport.getClientId(), "clientId", clientId);

            spec.put("validate", mode == SubmitMode.NORMAL);

            javascriptSupport.addInitializerCall("linkSubmit", spec);
        }
    }

    public String getClientId()
    {
        return clientId;
    }
}
