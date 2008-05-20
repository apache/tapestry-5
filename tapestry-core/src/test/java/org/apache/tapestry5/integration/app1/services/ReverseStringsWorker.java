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

package org.apache.tapestry5.integration.app1.services;

import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.*;

public class ReverseStringsWorker implements ComponentClassTransformWorker
{
    private final ComponentMethodAdvice advice = new ComponentMethodAdvice()
    {
        public void advise(ComponentMethodInvocation invocation)
        {
            for (int i = 0; i < invocation.getParameterCount(); i++)
            {
                if (invocation.getParameterType(i).equals(String.class))
                {
                    String value = (String) invocation.getParameter(i);

                    invocation.override(i, reverse(value));
                }
            }

            invocation.proceed();

            if (invocation.getResultType().equals(String.class))
            {
                if (invocation.isFail())
                {
                    Exception thrown = invocation.getThrown(Exception.class);

                    invocation.overrideResult(
                            String.format("Invocation of method %s() failed with %s.",
                                          invocation.getMethodName(),
                                          thrown.getClass().getName()));

                    return;
                }

                String value = (String) invocation.getResult();

                invocation.overrideResult(reverse(value));
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

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformMethodSignature sig : transformation.findMethodsWithAnnotation(ReverseStrings.class))
        {
            transformation.advise(sig, advice);
        }
    }
}
