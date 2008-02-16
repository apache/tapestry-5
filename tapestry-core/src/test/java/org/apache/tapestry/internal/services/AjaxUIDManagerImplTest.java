// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Link;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.services.PerthreadManager;
import org.apache.tapestry.services.Request;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AjaxUIDManagerImplTest extends InternalBaseTestCase
{
    private PerthreadManager _perthreadManager;

    @BeforeClass
    public void setup()
    {
        _perthreadManager = getService(PerthreadManager.class);
    }

    @BeforeMethod
    public void setup_method()
    {
        _perthreadManager.cleanup();
    }

    @Test
    public void get_uid_when_no_parameter_in_request()
    {
        Request request = mockRequest();

        train_getParameter(request, AjaxUIDManagerImpl.AJAX_UID_PARAMETER_NAME, null);

        replay();

        AjaxUIDManager manager = new AjaxUIDManagerImpl(request, _perthreadManager);

        // Use a loop to check caching

        for (int i = 0; i < 2; i++)
        {
            assertEquals(manager.getAjaxUID(), "1");
        }

        verify();
    }

    @Test
    public void current_uid_increments_value_from_request()
    {
        Request request = mockRequest();

        train_getParameter(request, AjaxUIDManagerImpl.AJAX_UID_PARAMETER_NAME, "123");

        replay();

        AjaxUIDManager manager = new AjaxUIDManagerImpl(request, _perthreadManager);

        // Use a loop to check caching

        for (int i = 0; i < 2; i++)
        {
            assertEquals(manager.getAjaxUID(), "124");
        }

        verify();
    }

    @Test
    public void action_link_in_traditional_request()
    {
        Request request = mockRequest();
        Link link = mockLink();

        train_isXHR(request, false);

        replay();

        AjaxUIDManagerImpl manager = new AjaxUIDManagerImpl(request, _perthreadManager);


        manager.createdActionLink(link);

        verify();
    }

    @Test
    public void action_link_in_ajax_request()
    {
        Request request = mockRequest();
        Link link = mockLink();

        train_isXHR(request, true);
        train_getParameter(request, AjaxUIDManagerImpl.AJAX_UID_PARAMETER_NAME, "777");

        link.addParameter(AjaxUIDManagerImpl.AJAX_UID_PARAMETER_NAME, "778");

        replay();

        AjaxUIDManagerImpl manager = new AjaxUIDManagerImpl(request, _perthreadManager);

        manager.createdActionLink(link);

        verify();
    }
}
