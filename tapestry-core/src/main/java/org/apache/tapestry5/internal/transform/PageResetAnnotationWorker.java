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

import java.util.List;

import org.apache.tapestry5.annotations.PageReset;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.structure.PageResetListener;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.util.func.Predicate;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.MethodAccess;
import org.apache.tapestry5.services.MethodInvocationResult;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.TransformMethod;
import org.apache.tapestry5.services.TransformMethodSignature;

/**
 * Implementation of the {@link PageReset} annotation. Makes the component implement {@link PageResetListener}.
 * 
 * @since 5.2.0
 */
public class PageResetAnnotationWorker implements ComponentClassTransformWorker
{
    private static final String META_KEY = "tapestry.page-reset-listener";

    private static final TransformMethodSignature CONTAINING_PAGE_DID_RESET = new TransformMethodSignature(
            "containingPageDidReset");

    private final ComponentMethodAdvice registerAsListenerAdvice = new ComponentMethodAdvice()
    {
        public void advise(ComponentMethodInvocation invocation)
        {
            invocation.proceed();

            InternalComponentResources icr = (InternalComponentResources) invocation.getComponentResources();

            icr.addPageResetListener((PageResetListener) invocation.getInstance());
        }
    };

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        List<TransformMethod> methods = matchPageResetMethods(transformation);

        if (methods.isEmpty())
            return;

        makeComponentRegisterAsPageResetListenerAtPageLoad(transformation, model);

        adviseContainingPageDidResetMethod(transformation, methods);
    }

    private void adviseContainingPageDidResetMethod(ClassTransformation transformation, List<TransformMethod> methods)
    {
        List<MethodAccess> methodAccess = convertToMethodAccess(methods);

        ComponentMethodAdvice advice = createMethodAccessAdvice(methodAccess);

        transformation.getOrCreateMethod(CONTAINING_PAGE_DID_RESET).addAdvice(advice);
    }

    private ComponentMethodAdvice createMethodAccessAdvice(final List<MethodAccess> methodAccess)
    {
        return new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                invocation.proceed();

                invokeResetMethods(invocation.getInstance());
            }

            private void invokeResetMethods(Object instance)
            {
                for (MethodAccess access : methodAccess)
                {
                    MethodInvocationResult result = access.invoke(instance);

                    result.rethrow();
                }
            }
        };
    }

    private List<MethodAccess> convertToMethodAccess(List<TransformMethod> methods)
    {
        List<MethodAccess> result = CollectionFactory.newList();

        for (TransformMethod method : methods)
        {
            result.add(toMethodAccess(method));
        }

        return result;
    }

    private void makeComponentRegisterAsPageResetListenerAtPageLoad(final ClassTransformation transformation,
            MutableComponentModel model)
    {
        // The meta key tracks whether this has already occurred; it is only necessary for a base class
        // (subclasses, even if they include pageReset methods, do not need to re-register if the base class
        // already has).

        if (model.getMeta(META_KEY) != null)
            return;

        transformation.addImplementedInterface(PageResetListener.class);

        transformation.getOrCreateMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE).addAdvice(
                registerAsListenerAdvice);

        model.setMeta(META_KEY, "true");
    }

    private List<TransformMethod> matchPageResetMethods(final ClassTransformation transformation)
    {
        return transformation.matchMethods(new Predicate<TransformMethod>()
        {
            public boolean accept(TransformMethod method)
            {
                return method.getName().equalsIgnoreCase("pageReset") || method.getAnnotation(PageReset.class) != null;
            }
        });
    }

    private MethodAccess toMethodAccess(TransformMethod method)
    {
        TransformMethodSignature sig = method.getSignature();

        boolean valid = sig.getParameterTypes().length == 0 && sig.getReturnType().equals("void");

        if (!valid)
            throw new RuntimeException(
                    String
                            .format(
                                    "Method %s is invalid: methods with the @PageReset annotation must return void, and have no parameters.",
                                    method.getMethodIdentifier()));

        return method.getAccess();
    }
}
