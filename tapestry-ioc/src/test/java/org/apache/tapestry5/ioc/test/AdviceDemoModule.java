// Copyright 2009, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.test;

import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.Order;
import org.apache.tapestry5.ioc.services.LoggingAdvisor;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class AdviceDemoModule
{
    public static Greeter buildGreeter()
    {
        return new Greeter()
        {
            @Override
            public String getGreeting()
            {
                return "Advice is Easy!";
            }
        };
    }

    @Match("*")
    public static void adviseLogging(MethodAdviceReceiver receiver, LoggingAdvisor loggingAdvisor, Logger logger)
    {
        loggingAdvisor.addLoggingAdvice(logger, receiver);
    }

    @Order("after:Logging")
    public static void adviseGreeter(MethodAdviceReceiver receiver)
    {
        MethodAdvice advice = new MethodAdvice()
        {
            @Override
            public void advise(MethodInvocation invocation)
            {
                invocation.proceed();

                String result = (String) invocation.getReturnValue();

                if (result != null)
                    invocation.setReturnValue(result.toUpperCase());
            }
        };

        for (Method m : receiver.getInterface().getMethods())
        {
            if (m.getReturnType().equals(String.class))
            {
                receiver.adviseMethod(m, advice);
            }
        }
    }
}
