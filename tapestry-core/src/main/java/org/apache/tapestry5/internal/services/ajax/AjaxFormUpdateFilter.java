// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.ajax;

import java.io.IOException;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.ValidationTrackerImpl;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.HiddenFieldPositioner;
import org.apache.tapestry5.corelib.internal.InternalFormSupport;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.internal.services.RequestConstants;
import org.apache.tapestry5.internal.util.CaptureResultCallback;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;

/**
 * Filter for the {@link Ajax} {@link ComponentEventRequestHandler} pipeline that may
 * contribute a {@link PartialMarkupRendererFilter} into the {@link PageRenderQueue}.
 * The contributed filter detects the case of a a client-side Tapestry.ZoneManager (i.e., a {@link Zone} component) and,
 * if it indicates the containing {@link Form} component, sets up a {@link PartialMarkupRendererFilter} to re-establish
 * the Form so that it will all mesh together on the client side.
 * 
 * @since 5.2.0
 */
public class AjaxFormUpdateFilter implements ComponentEventRequestFilter
{
    private final Request request;

    private final ComponentSource componentSource;

    private final HiddenFieldLocationRules rules;

    private final Environment environment;

    private final Heartbeat heartbeat;

    private final ClientDataEncoder clientDataEncoder;

    private final PageRenderQueue queue;

    private final Logger logger;

    public AjaxFormUpdateFilter(Request request, ComponentSource componentSource, HiddenFieldLocationRules rules,
            Environment environment, Heartbeat heartbeat, ClientDataEncoder clientDataEncoder, PageRenderQueue queue,
            Logger logger)
    {
        this.request = request;
        this.componentSource = componentSource;
        this.rules = rules;
        this.environment = environment;
        this.heartbeat = heartbeat;
        this.clientDataEncoder = clientDataEncoder;
        this.queue = queue;
        this.logger = logger;
    }

    public void handle(ComponentEventRequestParameters parameters, ComponentEventRequestHandler handler)
            throws IOException
    {
        String formClientId = request.getParameter(RequestConstants.FORM_CLIENTID_PARAMETER);
        String formComponentId = request.getParameter(RequestConstants.FORM_COMPONENTID_PARAMETER);

        if (InternalUtils.isNonBlank(formClientId) && InternalUtils.isNonBlank(formComponentId))
            addFilterToPartialRenderQueue(formClientId, formComponentId);

        handler.handle(parameters);
    }

    private void addFilterToPartialRenderQueue(String formClientId, String formComponentId)
    {
        queue.addPartialMarkupRendererFilter(createFilter(formClientId, formComponentId));
    }

    private PartialMarkupRendererFilter createFilter(final String formClientId, final String formComponentId)
    {
        return new PartialMarkupRendererFilter()
        {
            public void renderMarkup(MarkupWriter writer, JSONObject reply, PartialMarkupRenderer renderer)
            {
                HiddenFieldPositioner hiddenFieldPositioner = new HiddenFieldPositioner(writer, rules);

                ComponentActionSink actionSink = new ComponentActionSink(logger, clientDataEncoder);

                InternalFormSupport formSupport = createInternalFormSupport(formClientId, formComponentId, actionSink);

                setupBeforeRender(formSupport);

                renderer.renderMarkup(writer, reply);

                cleanupAfterRender(formSupport);

                injectHiddenFieldIntoDOM(hiddenFieldPositioner, actionSink);
            }

            private void setupBeforeRender(InternalFormSupport formSupport)
            {
                environment.push(FormSupport.class, formSupport);
                environment.push(ValidationTracker.class, new ValidationTrackerImpl());

                heartbeat.begin();
            }

            private void cleanupAfterRender(InternalFormSupport formSupport)
            {
                formSupport.executeDeferred();

                heartbeat.end();

                environment.pop(ValidationTracker.class);
                environment.pop(FormSupport.class);
            }

            private void injectHiddenFieldIntoDOM(HiddenFieldPositioner hiddenFieldPositioner,
                    ComponentActionSink actionSink)
            {
                hiddenFieldPositioner.getElement().attributes("type", "hidden",

                "name", Form.FORM_DATA,

                "value", actionSink.getClientData());
            }

            private InternalFormSupport createInternalFormSupport(String formClientId, String formComponentId,
                    ComponentActionSink actionSink)
            {
                // Kind of ugly, but the only way to ensure we don't have name collisions on the
                // client side is to force a unique id into each name (as well as each id, but that's
                // JavascriptSupport's job). It would be nice if we could agree on the uid, but
                // not essential.

                String uid = Long.toHexString(System.currentTimeMillis());

                IdAllocator idAllocator = new IdAllocator("_" + uid);

                Component formComponent = componentSource.getComponent(formComponentId);

                CaptureResultCallback<InternalFormSupport> callback = CaptureResultCallback.create();

                // This is a bit of a back-door to access a non-public method!

                formComponent.getComponentResources().triggerEvent("internalCreateRenderTimeFormSupport", new Object[]
                { formClientId, actionSink, idAllocator }, callback);

                return callback.getResult();
            }

        };
    }
}
