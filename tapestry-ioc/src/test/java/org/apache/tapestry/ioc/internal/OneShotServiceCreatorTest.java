// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal;

import java.lang.reflect.Method;

import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.testng.annotations.Test;

public class OneShotServiceCreatorTest extends IOCInternalTestCase
{
    @Test
    public void ensure_only_called_once() throws Exception
    {
        Method method = getClass().getMethod("buildMyService");

        ObjectCreator delegate = newObjectCreator();
        Object service = new Object();

        ServiceDef def = new ServiceDefImpl("foo.Bar", "singleton", method, false, false);

        train_createObject(delegate, service);

        replay();

        ObjectCreator oneShot = new OneShotServiceCreator(def, delegate);

        assertSame(oneShot.createObject(), service);

        try
        {
            oneShot.createObject();
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Construction of service 'foo.Bar' has failed due to recursion: the service depends on itself in some way. Please check "
                            + getClass().getName()
                            + ".buildMyService() for references to another service that is itself dependent on service 'foo.Bar'.");
        }

    }

    /** Fake service builder method. */
    public Runnable buildMyService()
    {
        return null;
    }
}
