// Copyright 2007, 2010 The Apache Software Foundation
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

import java.lang.annotation.Annotation;
import java.util.List;

import org.apache.tapestry5.ioc.util.func.Predicate;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.MethodAccess;
import org.apache.tapestry5.services.MethodInvocationResult;
import org.apache.tapestry5.services.TransformMethod;
import org.apache.tapestry5.services.TransformMethodSignature;

/**
 * Similar to {@link org.apache.tapestry5.internal.transform.RenderPhaseMethodWorker} but applies to annotations/methods
 * related to the overall page lifecycle. Page lifecycle methods are always void and take no parameters.
 */
public class PageLifecycleAnnotationWorker implements ComponentClassTransformWorker
{
    private final Class<? extends Annotation> methodAnnotationClass;

    private final TransformMethodSignature lifecycleMethodSignature;

    private final String methodAlias;

    public PageLifecycleAnnotationWorker(Class<? extends Annotation> methodAnnotationClass,
            TransformMethodSignature lifecycleMethodSignature, String methodAlias)
    {
        this.methodAnnotationClass = methodAnnotationClass;
        this.lifecycleMethodSignature = lifecycleMethodSignature;
        this.methodAlias = methodAlias;
    }

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformMethod method : matchLifecycleMethods(transformation))
        {
            invokeMethodWithinLifecycle(transformation, method);
        }
    }

    private void invokeMethodWithinLifecycle(final ClassTransformation transformation, TransformMethod method)
    {
        validateMethodSignature(method);

        final MethodAccess access = method.getAccess();

        ComponentMethodAdvice advice = createAdviceToInvokeMethod(access);

        transformation.getOrCreateMethod(lifecycleMethodSignature).addAdvice(advice);
    }

    private ComponentMethodAdvice createAdviceToInvokeMethod(final MethodAccess access)
    {
        return new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                invocation.proceed();

                MethodInvocationResult result = access.invoke(invocation.getInstance());

                result.rethrow();
            }
        };
    }

    private void validateMethodSignature(TransformMethod method)
    {
        TransformMethodSignature signature = method.getSignature();

        if (!signature.getReturnType().equals("void"))
            throw new RuntimeException(String.format("Method %s is a lifecycle method and should return void.", method
                    .getMethodIdentifier()));

        if (signature.getParameterTypes().length > 0)
            throw new RuntimeException(String.format("Method %s is a lifecycle method and should take no parameters.",
                    method.getMethodIdentifier()));
    }

    private List<TransformMethod> matchLifecycleMethods(final ClassTransformation transformation)
    {
        return transformation.matchMethods(new Predicate<TransformMethod>()
        {

            public boolean accept(TransformMethod method)
            {
                return method.getName().equalsIgnoreCase(methodAlias)
                        || method.getAnnotation(methodAnnotationClass) != null;
            }
        });
    }
}
