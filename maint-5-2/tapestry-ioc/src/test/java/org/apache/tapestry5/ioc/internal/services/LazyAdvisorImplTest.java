// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.Greeter;
import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;
import org.apache.tapestry5.ioc.services.LazyAdvisor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This tests {@link org.apache.tapestry5.ioc.internal.services.LazyAdvisorImpl}, but also tests a lot of {@link
 * org.apache.tapestry5.ioc.internal.services.ThunkCreatorImpl} (which was refactored out of LazyAdvisorImpl).
 */
public class LazyAdvisorImplTest extends IOCInternalTestCase
{
    private AspectDecorator aspectDecorator;

    private LazyAdvisor lazyAdvisor;

    @BeforeClass
    public void setup()
    {
        aspectDecorator = getService(AspectDecorator.class);
        lazyAdvisor = getService(LazyAdvisor.class);
    }

    @Test
    public void void_methods_are_not_lazy()
    {
        LazyService service = mockLazyService();

        service.notLazyBecauseVoid();

        replay();

        LazyService advised = advise(service);

        advised.notLazyBecauseVoid();

        verify();
    }

    private LazyService mockLazyService()
    {
        return newMock(LazyService.class);
    }

    @Test
    public void non_interface_return_value()
    {
        LazyService service = mockLazyService();

        expect(service.notLazyBecauseOfReturnValue()).andReturn("working hard!");

        replay();

        LazyService advised = advise(service);

        assertEquals(advised.notLazyBecauseOfReturnValue(), "working hard!");

        verify();
    }

    @Test
    public void lazy_method()
    {
        LazyService service = mockLazyService();
        Greeter greeter = new Greeter()
        {
            public String getGreeting()
            {
                return "Hello!";
            }
        };


        replay();

        LazyService advised = advise(service);

        Greeter thunk = advised.createGreeter();

        assertEquals(thunk.toString(),
                     "<org.apache.tapestry5.ioc.Greeter Thunk for org.apache.tapestry5.ioc.internal.services.LazyService.createGreeter()>");

        verify();

        expect(service.createGreeter()).andReturn(greeter);

        // Prove that the lazy method is only invoked once.

        replay();

        for (int i = 0; i < 2; i++)
            assertEquals(thunk.getGreeting(), "Hello!");

        verify();
    }

    @Test
    public void checked_exception_prevents_lazy() throws Exception
    {
        LazyService service = mockLazyService();
        Greeter greeter = newMock(Greeter.class);

        expect(service.notLazyCreateGreeter()).andReturn(greeter);

        replay();

        LazyService advised = advise(service);

        Greeter actual = advised.notLazyCreateGreeter();

        assertSame(actual, greeter);

        verify();
    }

    @Test
    public void notlazy_annotation()
    {
        LazyService service = mockLazyService();
        Greeter greeter = newMock(Greeter.class);

        expect(service.notLazyFromAnnotationGreeter()).andReturn(greeter);

        replay();

        LazyService advised = advise(service);

        Greeter actual = advised.notLazyFromAnnotationGreeter();

        assertSame(actual, greeter);

        verify();

    }

    @Test
    public void thunk_class_is_cached()
    {
        LazyService service = mockLazyService();

        replay();

        LazyService advised = advise(service);

        Greeter g1 = advised.createGreeter();
        Greeter g2 = advised.safeCreateCreator();

        assertSame(g2.getClass(), g1.getClass());

        verify();
    }

    private LazyService advise(LazyService base)
    {
        AspectInterceptorBuilder<LazyService> builder = aspectDecorator.createBuilder(LazyService.class, base,
                                                                                      "<LazyService Proxy>");

        lazyAdvisor.addLazyMethodInvocationAdvice(builder);

        return builder.build();
    }
}
