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
import org.apache.tapestry.internal.services.HeartbeatImpl;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.services.Heartbeat;
import org.apache.tapestry.services.Request;
import org.testng.annotations.Test;

public class SubmitTest extends InternalBaseTestCase
{
    @Test
    public void not_trigger_of_submission()
    {
        FormSupport support = mockFormSupport();
        Request request = mockRequest();

        String elementName = "myname";

        train_getParameter(request, elementName, null);

        replay();

        Submit submit = new Submit(request);

        submit.processSubmission(support, elementName);

        verify();
    }

    @Test
    public void trigger_deferred()
    {
        Request request = mockRequest();
        ComponentResources resources = mockComponentResources();
        FormSupportImpl support = new FormSupportImpl();

        String elementName = "myname";

        train_getParameter(request, elementName, "login");

        replay();

        Submit submit = new Submit(request);

        submit.setup(resources, support, null);

        submit.processSubmission(support, elementName);

        verify();

        expect(resources.triggerEvent(Submit.SELECTED_EVENT, null, null)).andReturn(false);

        replay();

        support.executeDeferred();

        verify();
    }

    @Test
    public void trigger_immediate()
    {
        FormSupport support = mockFormSupport();
        ComponentResources resources = mockComponentResources();
        Heartbeat heartbeat = new HeartbeatImpl();
        Request request = mockRequest();

        String elementName = "myname";

        train_getParameter(request, elementName, "login");

        replay();

        heartbeat.begin();

        Submit submit = new Submit(request);

        submit.setup(resources, support, heartbeat);
        submit.setDefer(false);

        submit.processSubmission(support, elementName);

        verify();

        expect(resources.triggerEvent(Submit.SELECTED_EVENT, null, null)).andReturn(false);

        replay();

        heartbeat.end();

        verify();

    }

    protected final FormSupport mockFormSupport()
    {
        return newMock(FormSupport.class);
    }

}
