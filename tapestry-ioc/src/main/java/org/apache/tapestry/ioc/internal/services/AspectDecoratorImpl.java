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

import org.apache.tapestry.ioc.MethodAdvice;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.services.AspectDecorator;
import org.apache.tapestry.ioc.services.AspectInterceptorBuilder;
import org.apache.tapestry.ioc.services.Builtin;
import org.apache.tapestry.ioc.services.ClassFactory;

import java.lang.reflect.Method;

public class AspectDecoratorImpl implements AspectDecorator
{
    private final ClassFactory _classFactory;

    public AspectDecoratorImpl(@Builtin ClassFactory classFactory)
    {
        _classFactory = classFactory;
    }

    public <T> T build(Class<T> serviceInterface, T delegate, MethodAdvice advice, String description)
    {
        Defense.notNull(advice, "advice");

        AspectInterceptorBuilder<T> builder = createBuilder(serviceInterface, delegate, description);

        // Use the same advice for all methods.

        for (Method m : serviceInterface.getMethods())
            builder.adviseMethod(m, advice);

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
            private AspectInterceptorBuilder<T> _builder;

            public void adviseMethod(Method method, MethodAdvice advice)
            {
                if (_builder == null)
                    _builder = new AspectInterceptorBuilderImpl<T>(_classFactory, serviceInterface, delegate,
                                                                   description);

                _builder.adviseMethod(method, advice);
            }

            public T build()
            {
                return _builder == null ? delegate : _builder.build();
            }
        };
    }
}
