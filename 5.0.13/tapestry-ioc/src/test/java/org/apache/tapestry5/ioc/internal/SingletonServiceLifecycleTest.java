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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceLifecycle;
import org.apache.tapestry5.ioc.ServiceResources;
import org.testng.annotations.Test;

public class SingletonServiceLifecycleTest extends IOCInternalTestCase
{
    @Test
    public void test()
    {
        ServiceResources resources = mockServiceResources();
        ObjectCreator creator = mockObjectCreator();
        Object expected = new Object();

        train_createObject(creator, expected);

        replay();

        ServiceLifecycle lifecycle = new SingletonServiceLifecycle();

        Object actual = lifecycle.createService(resources, creator);

        assertSame(actual, expected);

        verify();
    }
}
