// Copyright 2008, 2009, 2010, 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.AnnotationAccess;
import org.apache.tapestry5.ioc.annotations.PreventServiceDecoration;
import org.apache.tapestry5.ioc.internal.AnnotationAccessImpl;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;
import org.apache.tapestry5.ioc.services.Builtin;

import java.lang.reflect.Method;

@PreventServiceDecoration
public class AspectDecoratorImpl implements AspectDecorator
{
    private final PlasticProxyFactory proxyFactory;

    public AspectDecoratorImpl(@Builtin
                               PlasticProxyFactory proxyFactory)
    {
        this.proxyFactory = proxyFactory;
    }

    @Override
    public <T> AspectInterceptorBuilder<T> createBuilder(Class<T> serviceInterface, final T delegate, String description)
    {
        return createBuilder(serviceInterface, delegate, new AnnotationAccessImpl(delegate.getClass()), description);
    }

    @Override
    public <T> AspectInterceptorBuilder<T> createBuilder(final Class<T> serviceInterface, final T delegate,
                                                         AnnotationAccess annotationAccess, final String description)
    {
        assert serviceInterface != null;
        assert delegate != null;
        assert InternalUtils.isNonBlank(description);

        // The inner class here prevents the needless creation of the AspectInterceptorBuilderImpl,
        // and the various Plastic related overhead, until there's some actual advice.

        return new AbtractAspectInterceptorBuilder<T>(annotationAccess)
        {
            private AspectInterceptorBuilder<T> builder;

            @Override
            public void adviseMethod(Method method, org.apache.tapestry5.plastic.MethodAdvice advice)
            {
                getBuilder().adviseMethod(method, advice);
            }

            @Override
            public void adviseAllMethods(org.apache.tapestry5.plastic.MethodAdvice advice)
            {
                getBuilder().adviseAllMethods(advice);
            }

            @Override
            public Class getInterface()
            {
                return serviceInterface;
            }

            @Override
            public T build()
            {
                return builder == null ? delegate : builder.build();
            }

            private AspectInterceptorBuilder<T> getBuilder()
            {
                if (builder == null)
                    builder = new AspectInterceptorBuilderImpl<T>(annotationAccess, proxyFactory, serviceInterface,
                            delegate, description);

                return builder;
            }
        };
    }
}
