// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.Heartbeat;
import org.testng.annotations.Test;

public class HeartbeatImplTest extends InternalBaseTestCase
{
    @Test
    public void single_heartbeat()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();

        replay();

        Heartbeat hb = new HeartbeatImpl();

        hb.begin();

        hb.defer(r1);
        hb.defer(r2);

        verify();

        r1.run();
        r2.run();

        replay();

        hb.end();

        verify();
    }

    @Test
    public void nested_heartbeats()
    {
        Runnable r1 = mockRunnable();
        Runnable r2 = mockRunnable();
        Runnable r3 = mockRunnable();

        replay();

        Heartbeat hb = new HeartbeatImpl();

        hb.begin();

        hb.defer(r1);
        hb.defer(r2);

        hb.begin();

        hb.defer(r3);

        verify();

        r3.run();

        replay();

        hb.end();

        verify();

        r1.run();
        r2.run();

        replay();

        hb.end();

        verify();
    }
}
