// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.internal.util.MethodInvocationBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.MethodFilter;
import org.apache.tapestry5.services.TransformMethodSignature;

import java.lang.annotation.Annotation;

/**
 * Similar to {@link org.apache.tapestry5.internal.transform.RenderPhaseMethodWorker} but applies to annotations/methods
 * related to the overall page lifecycle.
 */
public class PageLifecycleAnnotationWorker implements ComponentClassTransformWorker
{
    private final Class<? extends Annotation> methodAnnotationClass;

    private final TransformMethodSignature lifecycleMethodSignature;

    private final String methodAlias;

    private final MethodInvocationBuilder invocationBuilder = new MethodInvocationBuilder();

    public PageLifecycleAnnotationWorker(final Class<? extends Annotation> methodAnnotationClass,
                                         final TransformMethodSignature lifecycleMethodSignature,
                                         final String methodAlias)
    {
        this.methodAnnotationClass = methodAnnotationClass;
        this.lifecycleMethodSignature = lifecycleMethodSignature;
        this.methodAlias = methodAlias;
    }

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(TransformMethodSignature signature)
            {
                if (signature.getMethodName().equals(methodAlias))
                    return true;

                return transformation.getMethodAnnotation(signature, methodAnnotationClass) != null;
            }
        };

        // Did this they easy way, because I doubt there will be more than one signature.
        // If I expected lots of signatures, I'd build up a BodyBuilder in the loop and extend the
        // method outside the loop.

        for (TransformMethodSignature signature : transformation.findMethods(filter))
        {
            // TODO: Filter out the non-void methods (with a non-fatal warning/error?) For the
            // moment, we just invoke the method anyway, and ignore the result. Also, MethodInvocationBuilder
            // is very forgiving (and silent) about unexpected parameters (passing null/0/false).

            String body = invocationBuilder.buildMethodInvocation(signature, transformation);

            transformation.extendMethod(lifecycleMethodSignature, body + ";");
        }
    }

}
