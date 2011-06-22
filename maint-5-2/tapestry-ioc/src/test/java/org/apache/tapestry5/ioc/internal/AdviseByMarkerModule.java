// Copyright 2010 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.GreenMarker;
import org.apache.tapestry5.ioc.Greeter;
import org.apache.tapestry5.ioc.Invocation;
import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.RedMarker;
import org.apache.tapestry5.ioc.annotations.Advise;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Order;


public class AdviseByMarkerModule
{
   
    @Advise(serviceInterface=Greeter.class, id="foo")
    @GreenMarker
    @Order("before:Greeter")
    public static void doAdviseOneMoreTime(MethodAdviceReceiver receiver)
    {
        doAdvise(receiver, "gamma");
    }
   
    @Advise(serviceInterface=Greeter.class, id="bar")
    @GreenMarker
    @Order({"after:foo", "before:Greeter"})
    public static void doAdviseAgain(MethodAdviceReceiver receiver)
    {
        doAdvise(receiver, "beta");
    }
    
    @Advise(serviceInterface=Greeter.class)
    @GreenMarker
    public static void doAdvise(MethodAdviceReceiver receiver)
    {
        doAdvise(receiver, "alpha");
    }
    
    private static void doAdvise(MethodAdviceReceiver receiver, final String id)
    {
        receiver.adviseAllMethods(new MethodAdvice()
        {
           
            public void advise(Invocation invocation)
            {
                invocation.proceed();
               
                Object result = invocation.getResult();
               
                invocation.overrideResult(String.format("%s[%s]", id, result));
               
            }
        });
    }
   
    @Advise(serviceInterface=Greeter.class, id="barney")
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
            public String getGreeting()
            {
                return "Red";
            }
        };
    }

}
