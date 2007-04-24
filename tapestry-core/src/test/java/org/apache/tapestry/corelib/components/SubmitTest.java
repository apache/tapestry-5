// Copyright 2007 The Apache Software Foundation
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
import org.apache.tapestry.internal.services.FormParameterLookup;
import org.apache.tapestry.internal.services.HeartbeatImpl;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.Heartbeat;
import org.testng.annotations.Test;

public class SubmitTest extends InternalBaseTestCase
{
    @Test
    public void not_trigger_of_submission()
    {
        FormParameterLookup lookup = newFormParameterLookup();

        String elementName = "myname";

        train_getParameter(lookup, elementName, null);

        replay();

        Submit submit = new Submit();

        submit.processSubmission(lookup, elementName);

        verify();
    }

    @Test
    public void trigger_deferred()
    {
        FormParameterLookup lookup = newFormParameterLookup();
        ComponentResources resources = newComponentResources();
        FormSupportImpl support = new FormSupportImpl();

        String elementName = "myname";

        train_getParameter(lookup, elementName, "login");

        replay();

        Submit submit = new Submit();

        submit.testInject(resources, support, null);

        submit.processSubmission(lookup, elementName);

        verify();

        expect(resources.triggerEvent(Submit.SELECTED_EVENT, null, null)).andReturn(false);

        replay();

        support.executeDeferred();

        verify();
    }

    @Test
    public void trigger_immediate()
    {
        FormParameterLookup lookup = newFormParameterLookup();
        ComponentResources resources = newComponentResources();
        Heartbeat heartbeat = new HeartbeatImpl();

        String elementName = "myname";

        train_getParameter(lookup, elementName, "login");

        replay();

        heartbeat.begin();

        Submit submit = new Submit();

        submit.testInject(resources, null, heartbeat);
        submit.setDefer(false);

        submit.processSubmission(lookup, elementName);

        verify();

        expect(resources.triggerEvent(Submit.SELECTED_EVENT, null, null)).andReturn(false);

        replay();

        heartbeat.end();

        verify();

    }

}
