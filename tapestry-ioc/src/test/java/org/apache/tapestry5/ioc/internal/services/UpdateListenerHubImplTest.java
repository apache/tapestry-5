// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import java.lang.ref.WeakReference;

import org.apache.tapestry5.ioc.internal.services.UpdateListenerHubImpl;
import org.apache.tapestry5.ioc.test.TestBase;
import org.apache.tapestry5.services.UpdateListener;
import org.apache.tapestry5.services.UpdateListenerHub;
import org.testng.annotations.Test;

public class UpdateListenerHubImplTest extends TestBase
{
    @Test
    public void add_listener_and_invoke() throws Exception
    {
        UpdateListener listener = newMock(UpdateListener.class);

        UpdateListenerHub hub = new UpdateListenerHubImpl();

        listener.checkForUpdates();

        replay();

        hub.addUpdateListener(listener);

        hub.fireCheckForUpdates();

        verify();
    }

    @Test
    public void weak_references_are_not_invoked_once_clears() throws Exception
    {
        UpdateListener listener = new UpdateListener()
        {
            public void checkForUpdates()
            {
                throw new RuntimeException("checkForUpdates() should not be invoked on a dead reference.");
            }
        };

        WeakReference<UpdateListener> ref = new WeakReference<UpdateListener>(listener);

        UpdateListenerHub hub = new UpdateListenerHubImpl();

        hub.addUpdateListener(listener);

        listener = null;

        while (ref.get() != null)
        {
            System.gc();
        }

        hub.fireCheckForUpdates();
    }
}
