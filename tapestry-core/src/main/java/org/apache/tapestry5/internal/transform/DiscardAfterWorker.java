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

package org.apache.tapestry5.internal.transform;

import java.util.List;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.DiscardAfter;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.TransformMethod;

public class DiscardAfterWorker implements ComponentClassTransformWorker
{

    private static final ComponentMethodAdvice advice = new ComponentMethodAdvice()
    {

        public void advise(ComponentMethodInvocation invocation)
        {
            invocation.proceed();

            if (invocation.isFail())
                return;

            ComponentResources resources = invocation.getComponentResources();

            resources.discardPersistentFieldChanges();
        }

    };

    public void transform(final ClassTransformation transformation, final MutableComponentModel model)
    {
        final List<TransformMethod> methods = transformation.matchMethodsWithAnnotation(DiscardAfter.class);

        if (methods.isEmpty())
            return;

        for (final TransformMethod method : methods)
        {
            method.addAdvice(advice);
        }

    }
}
