// Copyright 2007, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import java.lang.annotation.Annotation;

/**
 * Similar to {@link org.apache.tapestry5.internal.transform.RenderPhaseMethodWorker} but applies to annotations/methods
 * related to the overall page lifecycle. Page lifecycle methods are always void and take no parameters.
 */
public class PageLifecycleAnnotationWorker implements ComponentClassTransformWorker2
{
    private final Class<? extends Annotation> methodAnnotationClass;

    private final MethodDescription lifecycleMethodDescription;

    private final String methodAlias;

    private final Predicate<PlasticMethod> MATCHER = new Predicate<PlasticMethod>()
    {
        public boolean accept(PlasticMethod method)
        {
            return method.getDescription().methodName.equalsIgnoreCase(methodAlias)
                    || method.hasAnnotation(methodAnnotationClass);
        }
    };

    private final Worker<PlasticMethod> VALIDATE = new Worker<PlasticMethod>()
    {
        public void work(PlasticMethod method)
        {
            if (!method.isVoid())
                throw new RuntimeException(String.format("Method %s is a lifecycle method and should return void.", method
                        .getMethodIdentifier()));

            if (!method.getParameters().isEmpty())
                throw new RuntimeException(String.format("Method %s is a lifecycle method and should take no parameters.",
                        method.getMethodIdentifier()));

        }
    };

    public PageLifecycleAnnotationWorker(Class<? extends Annotation> methodAnnotationClass,
                                         MethodDescription lifecycleMethodDescription, String methodAlias)
    {
        this.methodAnnotationClass = methodAnnotationClass;
        this.lifecycleMethodDescription = lifecycleMethodDescription;
        this.methodAlias = methodAlias;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        Flow<PlasticMethod> methods = matchLifecycleMethods(plasticClass);

        if (methods.isEmpty())
        {
            return;
        }

        plasticClass.introduceInterface(PageLifecycleListener.class);

        for (PlasticMethod method : methods)
        {
            invokeMethodWithinLifecycle(plasticClass, method);
        }
    }


    private void invokeMethodWithinLifecycle(PlasticClass plasticClass, PlasticMethod method)
    {
        MethodHandle handle = method.getHandle();

        plasticClass.introduceMethod(lifecycleMethodDescription).addAdvice(createAdvice(handle));
    }

    private MethodAdvice createAdvice(final MethodHandle handle)
    {
        return new MethodAdvice()
        {
            public void advise(MethodInvocation invocation)
            {
                invocation.proceed();

                handle.invoke(invocation.getInstance()).rethrow();
            }
        };
    }

    private Flow<PlasticMethod> matchLifecycleMethods(PlasticClass plasticClass)
    {
        return F.flow(plasticClass.getMethods()).filter(MATCHER).each(VALIDATE);
    }
}
