// Copyright 2007, 2008, 2010, 2011 The Apache Software Foundation
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

import static org.easymock.EasyMock.isA;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.corelib.internal.FormSupportImpl;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.services.HeartbeatImpl;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.test.ioc.TestBase;
import org.testng.annotations.Test;

public class SubmitTest extends InternalBaseTestCase
{
    @Test
    public void not_trigger_of_submission()
    {
        Request request = mockRequest();

        String elementName = "myname";

        train_getParameter(request, Form.SUBMITTING_ELEMENT_ID, null);
        train_getParameter(request, elementName, null);

        replay();

        Submit submit = new Submit(request);

        submit.processSubmission("xyz", elementName);

        verify();
    }

    @Test
    public void trigger_deferred()
    {
        Request request = mockRequest();
        ComponentResources resources = mockComponentResources();
        FormSupportImpl support = new FormSupportImpl(null, null);

        String elementName = "myname";

        // Also: test for the alternate, JavaScript oriented way, of determining the
        // element/component that triggered the submission.
        train_getParameter(request, Form.SUBMITTING_ELEMENT_ID, "[ 'xyz', 'pdq' ]");

        replay();

        Submit submit = new Submit(request);

        TestBase.set(submit, "resources", resources, "formSupport", support);

        submit.processSubmission("xyz", elementName);

        verify();

        expect(resources.triggerEvent(EventConstants.SELECTED, null, null)).andReturn(false);

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

        train_getParameter(request, Form.SUBMITTING_ELEMENT_ID, null);
        train_getParameter(request, elementName, "login");

        replay();

        heartbeat.begin();

        Submit submit = new Submit(request);

        TestBase.set(submit, "resources", resources, "formSupport", support, "heartbeat", heartbeat, "defer", false);

        submit.processSubmission("xyz", elementName);

        verify();

        expect(resources.triggerEvent(EventConstants.SELECTED, null, null)).andReturn(false);

        replay();

        heartbeat.end();

        verify();
    }

    @Test
    public void test_imagesubmit_event_fired()
    {
        Request request = mockRequest();
        final ComponentResources resources = mockComponentResources();
        FormSupport formSupport = mockFormSupport();
        Asset image = mockAsset();

        String elementName = "myname";

        train_getParameter(request, Form.SUBMITTING_ELEMENT_ID, null);
        train_getParameter(request, elementName + ".x", "15");

        formSupport.defer(isA(Runnable.class));

        replay();

        Submit submit = new Submit(request);

        TestBase.set(submit, "resources", resources, "formSupport", formSupport, "image", image);

        submit.processSubmission("xyz", elementName);

        verify();
    }

    @Test
    public void test_submit_event_fired()
    {
        Request request = mockRequest();
        final ComponentResources resources = mockComponentResources();
        FormSupport formSupport = mockFormSupport();

        String elementName = "myname";

        train_getParameter(request, Form.SUBMITTING_ELEMENT_ID, null);
        train_getParameter(request, elementName, "login");

        formSupport.defer(isA(Runnable.class));

        replay();

        Submit submit = new Submit(request);

        TestBase.set(submit, "resources", resources, "formSupport", formSupport);

        submit.processSubmission("xyz", elementName);

        verify();
    }
}
