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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.services.Heartbeat;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public class LoopTest extends InternalBaseTestCase
{
    @Test
    public void non_empty_iterator()
    {
        Heartbeat hb = mockHeartbeat();

        // Really hard to test the exact timing of all this; it will have to
        // be "proven" by integration tests.

        hb.begin();
        getMocksControl().times(3);

        hb.end();
        getMocksControl().times(3);

        replay();

        Loop loop = new Loop();

        loop.setHeartbeat(hb);

        loop.setSource(Arrays.asList("alpha", "beta", "gamma"));

        assertTrue(loop.setup());
        assertEquals(loop.getIndex(), 0);

        loop.begin();
        assertEquals(loop.getValue(), "alpha");
        assertEquals(loop.getIndex(), 0);

        assertFalse(loop.after());
        loop.begin();
        assertEquals(loop.getValue(), "beta");
        assertEquals(loop.getIndex(), 1);

        assertFalse(loop.after());
        loop.begin();
        assertEquals(loop.getValue(), "gamma");
        assertEquals(loop.getIndex(), 2);

        assertTrue(loop.after());

        verify();
    }

    @Test
    public void iterator_is_null()
    {
        Loop loop = new Loop();

        loop.setSource(null);

        assertFalse(loop.setup());
    }

    @Test
    public void iterator_is_empty()
    {
        Loop loop = new Loop();

        loop.setSource(Collections.EMPTY_LIST);

        assertFalse(loop.setup());
    }
}
