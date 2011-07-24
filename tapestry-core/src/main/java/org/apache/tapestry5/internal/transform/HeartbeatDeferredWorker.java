// Copyright 2010, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.services.Heartbeat;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

public class HeartbeatDeferredWorker implements ComponentClassTransformWorker2
{
    private final Heartbeat heartbeat;

    private final MethodAdvice deferredAdvice = new MethodAdvice()
    {
        public void advise(final MethodInvocation invocation)
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

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticMethod method : plasticClass.getMethodsWithAnnotation(HeartbeatDeferred.class))
        {
            deferMethodInvocations(method);
        }
    }

    void deferMethodInvocations(PlasticMethod method)
    {
        validateVoid(method);

        validateNoCheckedExceptions(method);

        method.addAdvice(deferredAdvice);

    }

    private void validateNoCheckedExceptions(PlasticMethod method)
    {
        if (method.getDescription().checkedExceptionTypes.length > 0)
            throw new RuntimeException(
                    String.format(
                            "Method %s is not compatible with the @HeartbeatDeferred annotation, as it throws checked exceptions.",
                            method.getMethodIdentifier()));
    }

    private void validateVoid(PlasticMethod method)
    {
        if (!method.isVoid())
            throw new RuntimeException(String.format(
                    "Method %s is not compatible with the @HeartbeatDeferred annotation, as it is not a void method.",
                    method.getMethodIdentifier()));
    }
}
