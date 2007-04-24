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

import static java.util.Arrays.asList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;

import java.util.List;

import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceDecorator;
import org.testng.annotations.Test;

public class InterceptorStackBuilderTest extends IOCInternalTestCase
{
    private static final String SERVICE_ID = "foo.bar.Baz";

    @Test
    public void no_decorators()
    {
        ObjectCreator core = newObjectCreator();
        Module module = newModule();
        Object coreObject = new Object();
        List<ServiceDecorator> decorators = newList();

        train_createObject(core, coreObject);

        train_findDecoratorsForService(module, SERVICE_ID, decorators);

        replay();

        ObjectCreator isb = new InterceptorStackBuilder(module, SERVICE_ID, core);

        Object intercepted = isb.createObject();

        assertSame(intercepted, coreObject);

        verify();
    }

    @Test
    public void decorator_returns_null_interceptor()
    {
        ObjectCreator core = newObjectCreator();
        Module module = newModule();
        Object coreObject = new Object();
        ServiceDecorator decorator = newServiceDecorator();

        List<ServiceDecorator> decorators = asList(decorator);

        train_createObject(core, coreObject);

        train_findDecoratorsForService(module, SERVICE_ID, decorators);

        train_createInterceptor(decorator, coreObject, null);

        replay();

        ObjectCreator isb = new InterceptorStackBuilder(module, SERVICE_ID, core);

        Object intercepted = isb.createObject();

        assertSame(intercepted, coreObject);

        verify();
    }

    @Test
    public void decorator_orderering()
    {
        ObjectCreator core = newObjectCreator();
        Module module = newModule();
        Object coreObject = new Object();
        Object interceptor1 = new Object();
        Object interceptor2 = new Object();
        ServiceDecorator decorator1 = newServiceDecorator();
        ServiceDecorator decorator2 = newServiceDecorator();

        List<ServiceDecorator> decorators = asList(decorator1, decorator2);

        train_createObject(core, coreObject);

        train_findDecoratorsForService(module, SERVICE_ID, decorators);

        // Notice: reverse order!

        train_createInterceptor(decorator2, coreObject, interceptor2);
        train_createInterceptor(decorator1, interceptor2, interceptor1);

        replay();

        ObjectCreator isb = new InterceptorStackBuilder(module, SERVICE_ID, core);

        Object intercepted = isb.createObject();

        assertSame(intercepted, interceptor1);

        verify();
    }
}
