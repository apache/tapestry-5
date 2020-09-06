// Copyright 2010,, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Order;
import org.apache.tapestry5.ioc.services.AspectDecorator;
import org.apache.tapestry5.ioc.services.AspectInterceptorBuilder;
import org.apache.tapestry5.ioc.test.GreenMarker;
import org.apache.tapestry5.ioc.test.Greeter;
import org.apache.tapestry5.ioc.test.RedMarker;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;


public class DecorateByMarkerModule
{

    @Decorate(serviceInterface = Greeter.class)
    @GreenMarker
    public static <T> T greeter(ServiceResources resources, T delegate, AspectDecorator aspectDecorator)
    {
        return doDecorate("foo", resources, delegate, aspectDecorator);
    }

    @Decorate(serviceInterface = Greeter.class, id = "bar")
    @GreenMarker
    @Order("after:Greeter")
    public static <T> T greeter2(ServiceResources resources, T delegate, AspectDecorator aspectDecorator)
    {
        return doDecorate("bar", resources, delegate, aspectDecorator);
    }

    @Decorate(serviceInterface = Greeter.class, id = "baz")
    @GreenMarker
    @Order({"after:Greeter", "before:bar"})
    public static <T> T greeter3(ServiceResources resources, T delegate, AspectDecorator aspectDecorator)
    {
        return doDecorate("baz", resources, delegate, aspectDecorator);
    }

    @Decorate(serviceInterface = Greeter.class, id = "barney")
    @Local
    public static <T> T localAdvise(ServiceResources resources, T delegate, AspectDecorator aspectDecorator)
    {
        return doDecorate("barney", resources, delegate, aspectDecorator);
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
