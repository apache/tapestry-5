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

package org.apache.tapestry.ioc.internal;

import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OneShotServiceCreatorTest extends IOCInternalTestCase
{

    @Test
    public void ensure_only_called_once() throws Exception
    {
        Log log = newLog();
        Method method = getClass().getMethod("buildMyService");

        ObjectCreator delegate = newObjectCreator();
        Object service = new Object();

        ServiceDef def = new ServiceDefImpl("Bar", "singleton", method, false, getClassFactory());

        train_createObject(delegate, service);

        replay();

        ObjectCreator oneShot = new OneShotServiceCreator(def, delegate, log);

        assertSame(oneShot.createObject(), service);

        try
        {
            oneShot.createObject();
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertMessageContains(
                    ex,
                    "Construction of service 'Bar' has failed due to recursion: the service depends on itself in some way.",
                    getClass().getName() + ".buildMyService() ",
                    "for references to another service that is itself dependent on service 'Bar'.");
        }

        verify();
    }

    @Test
    public void reporting_of_construction_failure() throws Exception
    {
        RuntimeException failure = new RuntimeException("Just cranky.");
        Log log = newLog();
        Method method = getClass().getMethod("buildMyService");

        ObjectCreator delegate = newObjectCreator();
        Object service = new Object();

        ServiceDef def = new ServiceDefImpl("foo.Bar", "singleton", method, false, null);

        expect(delegate.createObject()).andThrow(failure);

        log.error("Construction of service foo.Bar failed: Just cranky.", failure);

        replay();

        ObjectCreator oneShot = new OneShotServiceCreator(def, delegate, log);

        try
        {
            oneShot.createObject();
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertSame(ex, failure);
        }

        verify();

        // Now test that the locked flag is not set and that the object may still be created.

        train_createObject(delegate, service);

        replay();

        assertSame(service, oneShot.createObject());

        verify();

    }

    /** Fake service builder method. */
    public Runnable buildMyService()
    {
        return null;
    }

}
