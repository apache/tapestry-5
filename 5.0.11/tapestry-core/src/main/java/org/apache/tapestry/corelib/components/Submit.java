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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Mixin;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.corelib.mixins.RenderDisabled;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.services.Heartbeat;
import org.apache.tapestry.services.Request;

/**
 * Corresponds to &lt;input type="submit"&gt;, a client-side element that can force the enclosing form to submit. The
 * submit responsible for the form submission will post a notification that allows the application to know that it was
 * the responsible entity. The notification is named "selected" and has no context.
 */
public final class Submit extends AbstractField
{
    static final String SELECTED_EVENT = "selected";

    /**
     * If true (the default), then any notification sent by the component will be deferred until the end of the form
     * submission (this is usually desirable).
     */
    @Parameter
    private boolean _defer = true;

    @Environmental
    private FormSupport _formSupport;

    @Environmental
    private Heartbeat _heartbeat;

    @Inject
    private ComponentResources _resources;

    @Inject
    private Request _request;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled _renderDisabled;

    public Submit()
    {
    }

    Submit(Request request)
    {
        _request = request;
    }

    void beginRender(MarkupWriter writer)
    {
        writer.element("input", "type", "submit", "name", getControlName(), "id", getClientId());

        _resources.renderInformalParameters(writer);
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    @Override
    protected void processSubmission(String elementName)
    {
        String value = _request.getParameter(elementName);

        if (value == null) return;

        Runnable sendNotification = new Runnable()
        {
            public void run()
            {
                _resources.triggerEvent(SELECTED_EVENT, null, null);
            }
        };

        // When not deferred, don't wait, fire the event now (actually, at the end of the current
        // heartbeat). This is most likely because the Submit is inside a Loop and some contextual
        // information will change if we defer.

        if (_defer) _formSupport.defer(sendNotification);
        else _heartbeat.defer(sendNotification);

    }

    // For testing:

    void setDefer(boolean defer)
    {
        _defer = defer;
    }

    void setup(ComponentResources resources, FormSupport support, Heartbeat heartbeat)
    {
        _resources = resources;
        _formSupport = support;
        _heartbeat = heartbeat;
    }
}
