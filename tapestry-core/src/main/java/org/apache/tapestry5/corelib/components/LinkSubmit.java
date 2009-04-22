//  Copyright 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.Request;

/**
 * Generates a client-side hyperlink that submits the enclosing form. If the link is clicked in the browser, the
 * component will trigger an event ({@linkplain EventConstants#SELECTED selected} by default) , just like {@link
 * Submit}.
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
     * If true (the default), then any notification sent by the component will be deferred until the end of the form
     * submission (this is usually desirable).
     */
    @Parameter
    private boolean defer = true;

    @Inject
    private ComponentResources resources;

    @Inject
    private RenderSupport renderSupport;

    @Environmental
    private FormSupport formSupport;

    @Environmental
    private Heartbeat heartbeat;

    @Inject
    private Request request;

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

        String hiddenFieldName = this.clientId + ":hidden";

        if (request.getParameter(hiddenFieldName) != null)
        {
            Runnable notification = new Runnable()
            {
                public void run()
                {
                    resources.triggerEvent(event, null, null);
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
            clientId = renderSupport.allocateClientId(resources);

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

            renderSupport.addInit("linkSubmit", formSupport.getClientId(), clientId);
        }
    }

    public String getClientId()
    {
        return clientId;
    }
}

