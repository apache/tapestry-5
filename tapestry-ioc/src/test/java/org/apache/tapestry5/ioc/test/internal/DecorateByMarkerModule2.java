// Copyright 2011 The Apache Software Foundation
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
package org.apache.tapestry5.ioc.test.internal;

import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.annotations.Decorate;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.Order;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;
import org.apache.tapestry5.ioc.test.Greeter;
import org.apache.tapestry5.ioc.test.RedMarker;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.testng.Assert;


public class DecorateByMarkerModule2
{

    @Decorate
    @Match("RedGreeter")
    public static <T> T byMatchAnnotation(ServiceResources resources, T delegate, AspectDecorator aspectDecorator)
    {
        return doDecorate("alpha", resources, delegate, aspectDecorator);
    }

    @Decorate(id = "withMarker")
    @RedMarker
    @Order("before:*")
    public static <T> T byMarkerAnnotation(ServiceResources resources, T delegate, AspectDecorator aspectDecorator)
    {
        return doDecorate("beta", resources, delegate, aspectDecorator);
    }

    @Decorate(id = "doesNotMatchAnyService")
    public static <T> T doesNotMatchAnyService(ServiceResources resources, T delegate, AspectDecorator aspectDecorator)
    {
        Assert.fail("Unexpected invocation");

        return delegate;
    }

    private static <T> T doDecorate(final String decoratorId, ServiceResources resources, T delegate, AspectDecorator aspectDecorator)
    {
        Class<T> serviceInterface = resources.getServiceInterface();

        AspectInterceptorBuilder<T> builder = aspectDecorator.createBuilder(serviceInterface, delegate, String.format(
                "<Interceptor for %s(%s)>", resources.getServiceId(), serviceInterface.getName()));

        builder.adviseAllMethods(new MethodAdvice()
        {

            @Override
            public void advise(MethodInvocation invocation)
            {
                invocation.proceed();

                Object result = invocation.getReturnValue();

                invocation.setReturnValue(String.format("Decorated by %s[%s]", decoratorId, result));

            }
        });

        return builder.build();
    }

    @Marker(RedMarker.class)
    public Greeter buildRedGreeter()
    {
        return new Greeter()
        {
            @Override
            public String getGreeting()
            {
                return "Red";
            }
        };
    }

}
