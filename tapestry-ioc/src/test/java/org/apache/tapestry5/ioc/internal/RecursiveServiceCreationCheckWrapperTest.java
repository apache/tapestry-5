// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.slf4j.Logger;
import org.testng.annotations.Test;

public class RecursiveServiceCreationCheckWrapperTest extends IOCInternalTestCase
{

    private static final String SOURCE_DESCRIPTION = "{SOURCE DESCRIPTION}";

    @Test
    public void ensure_only_called_once() throws Exception
    {
        Logger logger = mockLogger();
        ObjectCreatorSource source = mockObjectCreatorSource();
        ObjectCreator delegate = mockObjectCreator();
        Object service = new Object();

        ServiceDef def = new ServiceDefImpl(Runnable.class, "Bar", null, "singleton", false, false, source);

        train_createObject(delegate, service);

        train_getDescription(source, SOURCE_DESCRIPTION);

        replay();

        ObjectCreator wrapper = new RecursiveServiceCreationCheckWrapper(def, delegate, logger);

        assertSame(wrapper.createObject(), service);

        try
        {
            wrapper.createObject();
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertMessageContains(
                    ex,
                    "Construction of service 'Bar' has failed due to recursion: the service depends on itself in some way.",
                    SOURCE_DESCRIPTION,
                    "for references to another service that is itself dependent on service 'Bar'.");
        }

        verify();
    }

    @Test
    public void reporting_of_construction_failure() throws Exception
    {
        RuntimeException failure = new RuntimeException("Just cranky.");
        Logger logger = mockLogger();
        ObjectCreatorSource source = mockObjectCreatorSource();
        ObjectCreator delegate = mockObjectCreator();
        Object service = new Object();

        ServiceDef def = new ServiceDefImpl(Runnable.class, "Bar", null, "singleton", false, false, source);

        expect(delegate.createObject()).andThrow(failure);

        logger.error("Construction of service Bar failed: Just cranky.", failure);

        replay();

        ObjectCreator wrapper = new RecursiveServiceCreationCheckWrapper(def, delegate, logger);

        try
        {
            wrapper.createObject();
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

        assertSame(service, wrapper.createObject());

        verify();
    }
}
