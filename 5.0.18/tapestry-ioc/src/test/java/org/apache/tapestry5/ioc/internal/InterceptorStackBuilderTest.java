// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ServiceDecorator;
import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;
import java.util.List;

public class InterceptorStackBuilderTest extends IOCInternalTestCase
{
    private static final String SERVICE_ID = "foo.bar.Baz";

    private final OperationTracker tracker = new QuietOperationTracker();

    @Test
    public void no_decorators()
    {
        ObjectCreator core = mockObjectCreator();
        Module module = mockModule();
        Object coreObject = new Object();
        List<ServiceDecorator> decorators = newList();

        train_createObject(core, coreObject);

        train_findDecoratorsForService(module, SERVICE_ID, decorators);

        replay();

        ObjectCreator isb = new InterceptorStackBuilder(module, SERVICE_ID, core, tracker);

        Object intercepted = isb.createObject();

        assertSame(intercepted, coreObject);

        verify();
    }

    @Test
    public void decorator_returns_null_interceptor()
    {
        ObjectCreator core = mockObjectCreator();
        Module module = mockModule();
        Object coreObject = new Object();
        ServiceDecorator decorator = mockServiceDecorator();

        List<ServiceDecorator> decorators = asList(decorator);

        train_createObject(core, coreObject);

        train_findDecoratorsForService(module, SERVICE_ID, decorators);

        train_createInterceptor(decorator, coreObject, null);

        replay();

        ObjectCreator isb = new InterceptorStackBuilder(module, SERVICE_ID, core, tracker);

        Object intercepted = isb.createObject();

        assertSame(intercepted, coreObject);

        verify();
    }

    @Test
    public void decorator_orderering()
    {
        ObjectCreator core = mockObjectCreator();
        Module module = mockModule();
        Object coreObject = new Object();
        Object interceptor1 = new Object();
        Object interceptor2 = new Object();
        ServiceDecorator decorator1 = mockServiceDecorator();
        ServiceDecorator decorator2 = mockServiceDecorator();

        List<ServiceDecorator> decorators = asList(decorator1, decorator2);

        train_createObject(core, coreObject);

        train_findDecoratorsForService(module, SERVICE_ID, decorators);

        // Notice: reverse order!

        train_createInterceptor(decorator2, coreObject, interceptor2);
        train_createInterceptor(decorator1, interceptor2, interceptor1);

        replay();

        ObjectCreator isb = new InterceptorStackBuilder(module, SERVICE_ID, core, tracker);

        Object intercepted = isb.createObject();

        assertSame(intercepted, interceptor1);

        verify();
    }
}
