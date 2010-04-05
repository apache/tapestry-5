// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.TransformMethod;

public class HeartbeatDeferredWorker implements ComponentClassTransformWorker
{
    private final Heartbeat heartbeat;

    private final ComponentMethodAdvice deferredAdvice = new ComponentMethodAdvice()
    {
        public void advise(final ComponentMethodInvocation invocation)
        {
            heartbeat.defer(new Runnable()
            {

                public void run()
                {
                    invocation.proceed();
                }
            });
        }
    };

    public HeartbeatDeferredWorker(Heartbeat heartbeat)
    {
        this.heartbeat = heartbeat;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformMethod method : transformation.matchMethodsWithAnnotation(HeartbeatDeferred.class))
        {
            deferMethodInvocations(method);
        }
    }

    void deferMethodInvocations(TransformMethod method)
    {
        validateVoid(method);

        validateNoCheckedExceptions(method);

        method.addAdvice(deferredAdvice);

    }

    private void validateNoCheckedExceptions(TransformMethod method)
    {
        if (method.getSignature().getExceptionTypes().length > 0)
            throw new RuntimeException(
                    String
                            .format(
                                    "Method %s is not compatible with the @HeartbeatDeferred annotation, as it throws checked exceptions.",
                                    method.getMethodIdentifier()));
    }

    private void validateVoid(TransformMethod method)
    {
        if (!method.getSignature().getReturnType().equals("void"))
            throw new RuntimeException(String.format(
                    "Method %s is not compatible with the @HeartbeatDeferred annotation, as it is not a void method.",
                    method.getMethodIdentifier()));
    }
}
