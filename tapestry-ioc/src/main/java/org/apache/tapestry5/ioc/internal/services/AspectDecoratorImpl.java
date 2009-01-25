// Copyright 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.ClassFactory;

import java.lang.reflect.Method;

public class AspectDecoratorImpl implements AspectDecorator
{
    private final ClassFactory classFactory;

    public AspectDecoratorImpl(@Builtin ClassFactory classFactory)
    {
        this.classFactory = classFactory;
    }

    public <T> T build(Class<T> serviceInterface, T delegate, MethodAdvice advice, String description)
    {
        Defense.notNull(advice, "advice");

        AspectInterceptorBuilder<T> builder = createBuilder(serviceInterface, delegate, description);

        builder.adviseAllMethods(advice);

        return builder.build();
    }

    public <T> AspectInterceptorBuilder<T> createBuilder(final Class<T> serviceInterface, final T delegate,
                                                         final String description)
    {
        Defense.notNull(serviceInterface, "serviceInterface");
        Defense.notNull(delegate, "delegate");
        Defense.notBlank(description, "description");

        // Defer creating the real builder until a method gets advised.  If no method is advised then
        // the delegate can be used unchanged.

        return new AspectInterceptorBuilder<T>()
        {
            private AspectInterceptorBuilder<T> builder;

            public void adviseMethod(Method method, MethodAdvice advice)
            {
                getBuilder().adviseMethod(method, advice);
            }

            public void adviseAllMethods(MethodAdvice advice)
            {
                getBuilder().adviseAllMethods(advice);
            }

            public Class getInterface()
            {
                return serviceInterface;
            }

            public T build()
            {
                return builder == null ? delegate : builder.build();
            }

            private AspectInterceptorBuilder<T> getBuilder()
            {
                if (builder == null)
                    builder = new AspectInterceptorBuilderImpl<T>(classFactory, serviceInterface, delegate,
                                                                  description);

                return builder;
            }
        };
    }
}
