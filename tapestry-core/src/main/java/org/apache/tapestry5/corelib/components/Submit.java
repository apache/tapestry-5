// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.Request;

/**
 * Corresponds to &lt;input type="submit"&gt;, a client-side element that can force the enclosing form to submit. The
 * submit responsible for the form submission will post a notification that allows the application to know that it was
 * the responsible entity. The notification is named "selected" and has no context.
 */
public class Submit extends AbstractField
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

    @Environmental
    private FormSupport formSupport;

    @Environmental
    private Heartbeat heartbeat;

    @Inject
    private ComponentResources resources;

    @Inject
    private Request request;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled renderDisabled;

    public Submit()
    {
    }

    Submit(Request request)
    {
        this.request = request;
    }

    void beginRender(MarkupWriter writer)
    {
        writer.element("input", "type", "submit", "name", getControlName(), "id", getClientId());

        resources.renderInformalParameters(writer);
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    @Override
    protected void processSubmission(String elementName)
    {
        String value = request.getParameter(elementName);

        if (value == null) return;

        Runnable sendNotification = new Runnable()
        {
            public void run()
            {
                resources.triggerEvent(event, null, null);
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

    void setup(ComponentResources resources, FormSupport support, Heartbeat heartbeat)
    {
        this.resources = resources;
        formSupport = support;
        this.heartbeat = heartbeat;
    }
}
