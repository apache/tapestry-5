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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.internal.util.MethodInvocationBuilder;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.MethodFilter;
import org.apache.tapestry.services.TransformMethodSignature;

import java.lang.annotation.Annotation;

/**
 * Similar to {@link ComponentLifecycleMethodWorker} but applies to annotations/methods related to
 * the overall page lifecycle.
 */
public class PageLifecycleAnnotationWorker implements ComponentClassTransformWorker
{
    private final Class<? extends Annotation> _methodAnnotationClass;

    private final TransformMethodSignature _lifecycleMethodSignature;

    private final String _methodAlias;

    private final MethodInvocationBuilder _invocationBuilder = new MethodInvocationBuilder();

    public PageLifecycleAnnotationWorker(final Class<? extends Annotation> methodAnnotationClass,
                                         final TransformMethodSignature lifecycleMethodSignature,
                                         final String methodAlias)
    {
        _methodAnnotationClass = methodAnnotationClass;
        _lifecycleMethodSignature = lifecycleMethodSignature;
        _methodAlias = methodAlias;
    }

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(TransformMethodSignature signature)
            {
                if (signature.getMethodName().equals(_methodAlias))
                    return true;

                return transformation.getMethodAnnotation(signature, _methodAnnotationClass) != null;
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

            String body = _invocationBuilder.buildMethodInvocation(signature, transformation);

            transformation.extendMethod(_lifecycleMethodSignature, body + ";");
        }
    }

}
