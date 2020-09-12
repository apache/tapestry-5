// Copyright 2013 The Apache Software Foundation
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
package org.apache.tapestry5.ioc.test.internal;

import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Decorate;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;

/**
 * @author Thiago H. de Paula Figueiredo (http://machina.com.br/thiago)
 */
public class DecoratorModule
{

    public static void bind(ServiceBinder binder)
    {
        binder.bind(NonAnnotatedServiceInterface.class, NonAnnotatedServiceInterfaceImpl.class);
        binder.bind(AnnotatedServiceInterface.class, AnnotatedServiceInterfaceImpl.class);
    }

    @Decorate(serviceInterface = AnnotatedServiceInterface.class)
    public static AnnotatedServiceInterface decorateAnnotated(AnnotatedServiceInterface delegate,
            AspectDecorator aspectDecorator)
    {
        final AspectInterceptorBuilder<AnnotatedServiceInterface> builder = aspectDecorator
                .createBuilder(AnnotatedServiceInterface.class, delegate, "!");
        builder.adviseAllMethods(new TestAdvice());
        return builder.build();
    }

    @Decorate(serviceInterface = NonAnnotatedServiceInterface.class)
    public static NonAnnotatedServiceInterface decorateNonAnnotated(
            NonAnnotatedServiceInterface delegate, AspectDecorator aspectDecorator)
    {
        final AspectInterceptorBuilder<NonAnnotatedServiceInterface> builder = aspectDecorator
                .createBuilder(NonAnnotatedServiceInterface.class, delegate, "!");
        builder.adviseAllMethods(new TestAdvice());
        return builder.build();
    }

}
