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

import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

public class ServiceDecoratorImplTest extends IOCInternalTestCase
{
    private static final String SERVICE_ID = "ioc.Fie";

    private ModuleBuilderSource newSource(final Object builder)
    {
        return new ModuleBuilderSource()
        {
            public Object getModuleBuilder()
            {
                return builder;
            }
        };
    }

    /**
     * Also, test logging of decorator method invocation.
     *
     * @throws Exception
     */
    @Test
    public void decorator_returns_interceptor() throws Exception
    {
        ServiceDecoratorFixture fixture = new ServiceDecoratorFixture();
        Method m = findMethod(fixture, "decoratorReturnsInterceptor");

        ServiceResources resources = mockServiceResources();
        Logger logger = mockLogger();
        fixture.expectedDelegate = mockFieService();
        fixture.interceptorToReturn = mockFieService();
        ModuleBuilderSource source = newSource(fixture);

        trainForConstructor(resources, logger);

        train_isDebugEnabled(logger, true);

        logger.debug(IOCMessages.invokingMethod(InternalUtils.asString(m, getClassFactory())));

        replay();

        // Check that the delegate gets passed in; check that the return value of the
        // decorator method is the return value of the ServiceDecorator.

        ServiceDecoratorImpl decorator = new ServiceDecoratorImpl(m, source, resources,
                                                                  getClassFactory());

        Object interceptor = decorator.createInterceptor(fixture.expectedDelegate);

        assertSame(interceptor, fixture.interceptorToReturn);

        verify();
    }

    @Test
    public void decorator_returns_null_interceptor() throws Exception
    {
        ServiceDecoratorFixture fixture = new ServiceDecoratorFixture();
        ModuleBuilderSource source = newSource(fixture);
        ServiceResources resources = mockServiceResources();
        Logger logger = mockLogger();
        Object delegate = mockFieService();

        trainForConstructor(resources, logger);

        train_isDebugEnabled(logger, false);

        replay();

        Method m = findMethod(fixture, "decorateReturnNull");

        ServiceDecoratorImpl decorator = new ServiceDecoratorImpl(m, source, resources,
                                                                  getClassFactory());

        Object interceptor = decorator.createInterceptor(delegate);

        assertNull(interceptor);

        verify();
    }

    @Test
    public void decorator_returns_incorrect_type() throws Exception
    {
        ServiceDecoratorFixture fixture = new ServiceDecoratorFixture();
        ModuleBuilderSource source = newSource(fixture);
        ServiceResources resources = mockServiceResources();
        Logger logger = mockLogger();
        fixture.expectedDelegate = mockFieService();
        fixture.interceptorToReturn = newMock(FoeService.class);

        Method m = findMethod(fixture, "decoratorUntyped");

        trainForConstructor(resources, logger);

        train_isDebugEnabled(logger, false);

        logger.warn(IOCMessages.decoratorReturnedWrongType(
                m,
                SERVICE_ID,
                fixture.interceptorToReturn,
                FieService.class));

        replay();

        ServiceDecoratorImpl decorator = new ServiceDecoratorImpl(m, source, resources,
                                                                  getClassFactory());

        Object interceptor = decorator.createInterceptor(fixture.expectedDelegate);

        assertNull(interceptor);

        verify();
    }

    @Test
    public void decorator_method_throws_exception() throws Exception
    {
        ServiceDecoratorFixture fixture = new ServiceDecoratorFixture();
        ModuleBuilderSource source = newSource(fixture);
        ServiceResources resources = mockServiceResources();
        Logger logger = mockLogger();
        Object delegate = mockFieService();
        fixture.exception = new RuntimeException("Ouch!");

        trainForConstructor(resources, logger);

        train_isDebugEnabled(logger, false);

        replay();

        Method m = findMethod(fixture, "decoratorThrowsException");

        ServiceDecoratorImpl decorator = new ServiceDecoratorImpl(m, source, resources,
                                                                  getClassFactory());

        try
        {
            decorator.createInterceptor(delegate);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), IOCMessages.decoratorMethodError(m, SERVICE_ID, ex
                    .getCause()));

            Throwable cause = ex.getCause();

            assertSame(cause, fixture.exception);
        }

        verify();
    }

    private FieService mockFieService()
    {
        return newMock(FieService.class);
    }

    private void trainForConstructor(ServiceResources resources, Logger logger)
    {
        train_getServiceId(resources, SERVICE_ID);

        train_getServiceInterface(resources, FieService.class);

        train_getLogger(resources, logger);
    }

}
