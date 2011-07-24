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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.DiscardAfter;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

public class DiscardAfterWorker implements ComponentClassTransformWorker2
{
    private static final MethodAdvice advice = new MethodAdvice()
    {
        public void advise(MethodInvocation invocation)
        {
            invocation.proceed();

            if (invocation.didThrowCheckedException())
                return;

            ComponentResources resources = invocation.getInstanceContext().get(ComponentResources.class);

            resources.discardPersistentFieldChanges();
        }
    };

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticMethod method : plasticClass.getMethodsWithAnnotation(DiscardAfter.class))
        {
            method.addAdvice(advice);
        }
    }
}