// Copyright 2008, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.services;

import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

public class ReverseStringsWorker implements ComponentClassTransformWorker2
{
    private final MethodAdvice advice = new MethodAdvice()
    {
        public void advise(MethodInvocation invocation)
        {
            Class<?>[] parameterTypes = invocation.getMethod().getParameterTypes();

            for (int i = 0; i < parameterTypes.length; i++)
            {
                if (parameterTypes[i].equals(String.class))
                {
                    String value = (String) invocation.getParameter(i);

                    invocation.setParameter(i, reverse(value));
                }
            }

            invocation.proceed();

            if (invocation.getMethod().getReturnType().equals(String.class))
            {
                if (invocation.didThrowCheckedException())
                {
                    Exception thrown = invocation.getCheckedException(Exception.class);

                    invocation.setReturnValue(String.format("Invocation of method %s() failed with %s.",
                            invocation.getMethod().getName(), thrown.getClass().getName()));

                    return;
                }

                String value = (String) invocation.getReturnValue();

                invocation.setReturnValue(reverse(value));
            }
        }

        private String reverse(String input)
        {
            char[] inputArray = input.toCharArray();
            char[] outputArray = new char[inputArray.length];

            for (int i = 0; i < inputArray.length; i++)
            {
                outputArray[inputArray.length - i - 1] = inputArray[i];
            }

            return new String(outputArray);
        }
    };

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticMethod method : plasticClass.getMethodsWithAnnotation(ReverseStrings.class))
        {
            method.addAdvice(advice);
        }
    }
}
