// Copyright 2008, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.TransformMethod;

/**
 * Extends a <em>root</em> component class' postRenderCleanup() method to invoke
 * {@link org.apache.tapestry5.internal.InternalComponentResources#postRenderCleanup()}.
 */
public class InvokePostRenderCleanupOnResourcesWorker implements ComponentClassTransformWorker
{
    private final ComponentMethodAdvice advice = new ComponentMethodAdvice()
    {
        public void advise(ComponentMethodInvocation invocation)
        {
            invocation.proceed();

            InternalComponentResources icr = (InternalComponentResources) invocation.getComponentResources();

            icr.postRenderCleanup();
        }
    };

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        // Since the stuff to be cleaned up is inside the component resources, we don't need to
        // do anything extra for a subclass; the root class will invoke the cleanup method.
        
        if (!transformation.isRootTransformation())
            return;

        TransformMethod method = transformation.getMethod(TransformConstants.POST_RENDER_CLEANUP_SIGNATURE);

        method.addAdvice(advice);
    }
}
