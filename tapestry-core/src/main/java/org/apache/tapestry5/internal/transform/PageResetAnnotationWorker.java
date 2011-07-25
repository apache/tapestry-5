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

import org.apache.tapestry5.annotations.PageReset;
import org.apache.tapestry5.func.*;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.structure.PageResetListener;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Implementation of the {@link PageReset} annotation. Makes the component implement {@link PageResetListener}.
 *
 * @since 5.2.0
 */
public class PageResetAnnotationWorker implements ComponentClassTransformWorker2
{
    private static final String META_KEY = "tapestry.page-reset-listener";

    private final ConstructorCallback REGISTER_AS_LISTENER = new ConstructorCallback()
    {
        public void onConstruct(Object instance, InstanceContext context)
        {
            InternalComponentResources resources = context.get(InternalComponentResources.class);

            resources.addPageResetListener((PageResetListener) instance);
        }
    };

    private final Predicate<PlasticMethod> METHOD_MATCHER = new Predicate<PlasticMethod>()
    {
        public boolean accept(PlasticMethod method)
        {
            return method.getDescription().methodName.equalsIgnoreCase("pageReset") ||
                    method.hasAnnotation(PageReset.class);
        }
    };

    private final Worker<PlasticMethod> METHOD_VALIDATOR = new Worker<PlasticMethod>()
    {
        public void work(PlasticMethod method)
        {
            boolean valid = method.isVoid() && method.getParameters().isEmpty();

            if (!valid)
            {
                throw new RuntimeException(
                        String.format(
                                "Method %s is invalid: methods with the @PageReset annotation must return void, and have no parameters.",
                                method.getMethodIdentifier()));
            }
        }
    };

    private final Mapper<PlasticMethod, MethodHandle> TO_HANDLE = new Mapper<PlasticMethod, MethodHandle>()
    {
        public MethodHandle map(PlasticMethod method)
        {
            return method.getHandle();
        }
    };

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        Flow<PlasticMethod> methods = findResetMethods(plasticClass);

        if (!methods.isEmpty())
        {
            if (!plasticClass.isInterfaceImplemented(PageResetListener.class))
            {
                plasticClass.introduceInterface(PageResetListener.class);
                plasticClass.onConstruct(REGISTER_AS_LISTENER);
            }

            invokeMethodsOnPageReset(plasticClass, methods);
        }
    }

    private void invokeMethodsOnPageReset(PlasticClass plasticClass, Flow<PlasticMethod> methods)
    {
        final MethodHandle[] handles = methods.map(TO_HANDLE).toArray(MethodHandle.class);

        plasticClass.introduceMethod(TransformConstants.CONTAINING_PAGE_DID_RESET_DESCRIPTION).addAdvice(new MethodAdvice()
        {
            public void advise(MethodInvocation invocation)
            {
                invocation.proceed();

                Object instance = invocation.getInstance();

                for (MethodHandle handle : handles)
                {
                    handle.invoke(instance);
                }
            }
        });
    }

    private Flow<PlasticMethod> findResetMethods(PlasticClass plasticClass)
    {
        return F.flow(plasticClass.getMethods()).filter(METHOD_MATCHER).each(METHOD_VALIDATOR);
    }
}
