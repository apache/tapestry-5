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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.internal.IOCInternalTestCase;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * Tests a few edge and error cases not covered by {@link org.apache.tapestry5.ioc.internal.services.LoggingDecoratorImplTest}.
 */
public class AspectInterceptorBuilderImplTest extends IOCInternalTestCase
{
    private AspectDecorator decorator;

    @BeforeClass
    public void setup()
    {
        decorator = getService(AspectDecorator.class);
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

        AspectInterceptorBuilder<Subject> builder = decorator.createBuilder(Subject.class, delegate, "<Subject>");

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

        AspectInterceptorBuilder<Subject> builder = decorator.createBuilder(Subject.class, delegate, "<Subject>");

        // This method doesn't belong.

        try
        {
            builder.adviseMethod(Runnable.class.getMethod("run"), advice);

            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Method public abstract void java.lang.Runnable.run() is not defined for interface interface org.apache.tapestry5.ioc.internal.services.AspectInterceptorBuilderImplTest$Subject.");
        }


        verify();
    }

    @Test
    public void method_with_duplicate_advice() throws Exception
    {
        TextTransformer delegate = new TextTransformer()
        {
            public String transform(String input)
            {
                return input;
            }
        };

        MethodAdvice stripFirstLetter = new MethodAdvice()
        {
            public void advise(Invocation invocation)
            {
                String param = (String) invocation.getParameter(0);

                invocation.override(0, param.substring(1));

                invocation.proceed();
            }
        };

        MethodAdvice reverse = new MethodAdvice()
        {
            public void advise(Invocation invocation)
            {
                String param = (String) invocation.getParameter(0);

                char[] input = param.toCharArray();
                int count = input.length;

                char[] output = new char[count];

                for (int i = 0; i < count; i++)
                    output[count - i - 1] = input[i];

                invocation.override(0, new String(output));

                invocation.proceed();
            }
        };

        AspectInterceptorBuilder<TextTransformer> builder = decorator.createBuilder(TextTransformer.class, delegate,
                                                                                    "<TextTransformer>");

        Method method = TextTransformer.class.getMethod("transform", String.class);
        builder.adviseMethod(method, stripFirstLetter);
        builder.adviseMethod(method, reverse);

        TextTransformer advised = builder.build();

        // strip first letter THEN reverse
        assertEquals(advised.transform("Tapestry"), "yrtsepa");
    }

    @Test
    public void arrays_as_parameters_and_result()
    {
        ArraysSubject delegate = new ArraysSubjectImpl();

        MethodAdvice advice = new MethodAdvice()
        {
            public void advise(Invocation invocation)
            {
                String[] param = (String[]) invocation.getParameter(0);

                for (int i = 0; i < param.length; i++)
                {
                    param[i] = param[i].toUpperCase();
                }

                invocation.proceed();

                String[] result = (String[]) invocation.getResult();

                for (int i = 0; i < result.length; i++)
                {
                    result[i] = i + ":" + result[i];
                }
            }
        };

        ArraysSubject advised = decorator.build(ArraysSubject.class, delegate, advice, "whatever");

        String[] inputs = {"Fred", "Barney"};

        String[] result = advised.operation(inputs);

        assertEquals(result[0], "0:FRED");
        assertEquals(result[1], "1:BARNEY");
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
