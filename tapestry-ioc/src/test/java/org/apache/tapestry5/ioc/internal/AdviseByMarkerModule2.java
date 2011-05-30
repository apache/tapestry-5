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

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.annotations.Advise;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.Order;
import org.testng.Assert;
import org.testng.TestNG;


public class AdviseByMarkerModule2
{
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

    @Advise
    @Match ("RedGreeter")
    public static void byMatchAnnotation(MethodAdviceReceiver receiver)
    {
        doAdvise(receiver, "alpha");
    }

    @Advise(id="withMarker")
    @RedMarker
    @Order("before:*")
    public static void byMarkerAnnotation(MethodAdviceReceiver receiver)
    {
        doAdvise(receiver, "beta");
    }

    @Advise(id="doesNotMatchAnyService")
    public static void doesNotMatchAnyService(MethodAdviceReceiver receiver)
    {
        Assert.fail("Unexpected invocation");
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
