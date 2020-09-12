// Copyright 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.annotations.Advise;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Order;
import org.apache.tapestry5.ioc.test.GreenMarker;
import org.apache.tapestry5.ioc.test.Greeter;
import org.apache.tapestry5.ioc.test.RedMarker;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;


public class AdviseByMarkerModule
{

    @Advise(serviceInterface = Greeter.class, id = "foo")
    @GreenMarker
    @Order("before:Greeter")
    public static void doAdviseOneMoreTime(MethodAdviceReceiver receiver)
    {
        doAdvise(receiver, "gamma");
    }

    @Advise(serviceInterface = Greeter.class, id = "bar")
    @GreenMarker
    @Order({"after:foo", "before:Greeter"})
    public static void doAdviseAgain(MethodAdviceReceiver receiver)
    {
        doAdvise(receiver, "beta");
    }

    @Advise(serviceInterface = Greeter.class)
    @GreenMarker
    public static void doAdvise(MethodAdviceReceiver receiver)
    {
        doAdvise(receiver, "alpha");
    }

    private static void doAdvise(MethodAdviceReceiver receiver, final String id)
    {
        receiver.adviseAllMethods(new MethodAdvice()
        {

            @Override
            public void advise(MethodInvocation invocation)
            {
                invocation.proceed();

                Object result = invocation.getReturnValue();

                invocation.setReturnValue(String.format("%s[%s]", id, result));

            }
        });
    }

    @Advise(serviceInterface = Greeter.class, id = "barney")
    @Local
    public static void localAdvise(MethodAdviceReceiver receiver)
    {
        doAdvise(receiver, "delta");
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
