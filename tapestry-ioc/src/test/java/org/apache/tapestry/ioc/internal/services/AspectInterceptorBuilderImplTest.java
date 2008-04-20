// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.services;

import org.apache.tapestry.ioc.Invocation;
import org.apache.tapestry.ioc.MethodAdvice;
import org.apache.tapestry.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry.ioc.services.AspectDecorator;
import org.apache.tapestry.ioc.services.AspectInterceptorBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests a few edge and error cases not covered by {@link org.apache.tapestry.ioc.internal.services.LoggingDecoratorImplTest}.
 */
public class AspectInterceptorBuilderImplTest extends IOCInternalTestCase
{
    private AspectDecorator _decorator;

    @BeforeClass
    public void setup()
    {
        _decorator = getService(AspectDecorator.class);
    }

    public interface Subject
    {
        void advised();

        void notAdvised();
    }

    @Test
    public void some_methods_not_intercepted() throws Exception
    {
        Subject delegate = mockSubject();

        MethodAdvice advice = new MethodAdvice()
        {
            public void advise(Invocation invocation)
            {
                assertEquals(invocation.getMethodName(), "advised");

                invocation.proceed();
            }
        };

        delegate.advised();
        delegate.notAdvised();

        replay();

        AspectInterceptorBuilder<Subject> builder = _decorator.createBuilder(Subject.class, delegate, "<Subject>");

        builder.adviseMethod(Subject.class.getMethod("advised"), advice);

        Subject interceptor = builder.build();

        interceptor.advised();
        interceptor.notAdvised();

        verify();
    }

    @Test
    public void method_not_in_service_interface() throws Exception
    {
        Subject delegate = mockSubject();

        MethodAdvice advice = mockAdvice();

        replay();

        AspectInterceptorBuilder<Subject> builder = _decorator.createBuilder(Subject.class, delegate, "<Subject>");

        // This method doesn't belong.

        try
        {
            builder.adviseMethod(Runnable.class.getMethod("run"), advice);

            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Method public abstract void java.lang.Runnable.run() is not defined for interface interface org.apache.tapestry.ioc.internal.services.AspectInterceptorBuilderImplTest$Subject.");
        }


        verify();
    }

    @Test
    public void method_with_duplicate_advice() throws Exception
    {
        Subject delegate = mockSubject();

        MethodAdvice advice = mockAdvice();

        replay();

        AspectInterceptorBuilder<Subject> builder = _decorator.createBuilder(Subject.class, delegate, "<Subject>");


        builder.adviseMethod(Subject.class.getMethod("advised"), advice);

        try
        {
            // Second is failure.

            builder.adviseMethod(Subject.class.getMethod("advised"), advice);

            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Method public abstract void org.apache.tapestry.ioc.internal.services.AspectInterceptorBuilderImplTest$Subject.advised() has already been advised.");
        }

        verify();
    }

    protected final MethodAdvice mockAdvice()
    {
        return newMock(MethodAdvice.class);
    }

    protected final Subject mockSubject()
    {
        return newMock(Subject.class);
    }

}
