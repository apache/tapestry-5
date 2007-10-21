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

package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry.ioc.internal.ServiceActivityTracker;
import org.apache.tapestry.ioc.services.Status;
import org.testng.annotations.Test;

public class JustInTimeObjectCreatorTest extends IOCInternalTestCase
{
    private static final String SERVICE_ID = "FooBar";

    @Test
    public void create_after_shutdown()
    {
        ObjectCreator creator = mockObjectCreator();

        replay();

        JustInTimeObjectCreator j = new JustInTimeObjectCreator(null, creator, SERVICE_ID);

        j.registryDidShutdown();

        try
        {
            j.createObject();
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Proxy for service FooBar is no longer active because the IOC Registry has been shut down.");
        }
    }

    @Test
    public void eager_load()
    {
        ObjectCreator creator = mockObjectCreator();
        Object service = new Object();
        ServiceActivityTracker tracker = mockServiceActivityTracker();

        replay();

        JustInTimeObjectCreator j = new JustInTimeObjectCreator(tracker, creator, SERVICE_ID);

        verify();

        // First access: use the creator to get the actual object.

        train_createObject(creator, service);

        tracker.setStatus(SERVICE_ID, Status.REAL);

        replay();

        j.eagerLoadService();

        verify();

        // This part tests the caching part.

        replay();

        assertSame(j.createObject(), service);

        verify();
    }
}
