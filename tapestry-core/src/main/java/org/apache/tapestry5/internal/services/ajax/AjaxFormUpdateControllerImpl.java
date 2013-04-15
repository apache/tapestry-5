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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationTracker;
import org.apache.tapestry5.ValidationTrackerImpl;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.internal.ComponentActionSink;
import org.apache.tapestry5.corelib.internal.HiddenFieldPositioner;
import org.apache.tapestry5.corelib.internal.InternalFormSupport;
import org.apache.tapestry5.internal.util.CaptureResultCallback;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.ComponentSource;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.HiddenFieldLocationRules;
import org.slf4j.Logger;

@Scope(ScopeConstants.PERTHREAD)
public class AjaxFormUpdateControllerImpl implements AjaxFormUpdateController
{
    private final ComponentSource componentSource;

    private final HiddenFieldLocationRules rules;

    private final Environment environment;

    private final Heartbeat heartbeat;

    private final ClientDataEncoder clientDataEncoder;

    private final Logger logger;

    private String formComponentId;

    private String formClientId;

    private HiddenFieldPositioner hiddenFieldPositioner;

    private ComponentActionSink actionSink;

    private InternalFormSupport formSupport;

    public AjaxFormUpdateControllerImpl(ComponentSource componentSource, HiddenFieldLocationRules rules,
            Environment environment, Heartbeat heartbeat, ClientDataEncoder clientDataEncoder, Logger logger)
    {
        this.componentSource = componentSource;
        this.rules = rules;
        this.environment = environment;
        this.heartbeat = heartbeat;
        this.clientDataEncoder = clientDataEncoder;
        this.logger = logger;
    }

    public void initializeForForm(String formComponentId, String formClientId)
    {
        this.formComponentId = formComponentId;
        this.formClientId = formClientId;
    }

    public void setupBeforePartialZoneRender(MarkupWriter writer)
    {
        if (formComponentId == null)
            return;

        hiddenFieldPositioner = new HiddenFieldPositioner(writer, rules);

        actionSink = new ComponentActionSink(logger, clientDataEncoder);

        formSupport = createInternalFormSupport(formClientId, formComponentId, actionSink);

        environment.push(FormSupport.class, formSupport);
        environment.push(ValidationTracker.class, new ValidationTrackerImpl());

        heartbeat.begin();
    }

    public void cleanupAfterPartialZoneRender()
    {
        if (formComponentId == null)
            return;

        heartbeat.end();

        formSupport.executeDeferred();

        environment.pop(ValidationTracker.class);
        environment.pop(FormSupport.class);

        // If the Zone didn't actually contain any form control elements, then
        // nothing will have been written to the action sink. In that case,
        // get rid of the hidden field, if one was even added.

        if (actionSink.isEmpty())
        {
            hiddenFieldPositioner.discard();

            return;
        }

        // We've collected some hidden data that needs to be placed inside the Zone.
        // This will raise an exception if the content of the zone didn't provide such a position.

        hiddenFieldPositioner.getElement().attributes("type", "hidden",

        "name", Form.FORM_DATA,

        "value", actionSink.getClientData());
    }

    private InternalFormSupport createInternalFormSupport(String formClientId, String formComponentId,
            ComponentActionSink actionSink)
    {
        // Kind of ugly, but the only way to ensure we don't have name collisions on the
        // client side is to force a unique id into each name (as well as each id, but that's
        // JavaScriptSupport's job). It would be nice if we could agree on the uid, but
        // not essential.

        String uid = Long.toHexString(System.nanoTime());

        IdAllocator idAllocator = new IdAllocator("_" + uid);

        Component formComponent = componentSource.getComponent(formComponentId);

        CaptureResultCallback<InternalFormSupport> callback = CaptureResultCallback.create();

        // This is a bit of a back-door to access a non-public method!

        formComponent.getComponentResources().triggerEvent("internalCreateRenderTimeFormSupport", new Object[]
        { formClientId, actionSink, idAllocator }, callback);

        return callback.getResult();
    }
}
