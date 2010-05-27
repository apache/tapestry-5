// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.AfterRenderBody;
import org.apache.tapestry5.annotations.AfterRenderTemplate;
import org.apache.tapestry5.annotations.BeforeRenderBody;
import org.apache.tapestry5.annotations.BeforeRenderTemplate;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.CleanupRender;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.func.Predicate;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Event;
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
 * Converts one of the methods of {@link org.apache.tapestry5.runtime.Component} into a chain of
 * command that, itself,
 * invokes certain methods (render phase methods) marked with an annotation, or named in a specific
 * way.
 */
@SuppressWarnings("unchecked")
public class RenderPhaseMethodWorker implements ComponentClassTransformWorker
{
    private final class RenderPhaseMethodAdvice implements ComponentMethodAdvice
    {
        private final boolean reverse;

        private final List<Invoker> invokers;

        private RenderPhaseMethodAdvice(boolean reverse, List<Invoker> invokers)
        {
            this.reverse = reverse;
            this.invokers = invokers;
        }

        public void advise(ComponentMethodInvocation invocation)
        {
            if (!reverse)
                invocation.proceed();

            // All render phase methods take the same two parameters (writer and event)

            Event event = (Event) invocation.getParameter(1);

            if (event.isAborted())
                return;

            Object instance = invocation.getInstance();
            MarkupWriter writer = (MarkupWriter) invocation.getParameter(0);

            for (Invoker invoker : invokers)
            {
                invoker.invoke(instance, writer, event);

                if (event.isAborted())
                    return;
            }

            // Parent class implementation goes last.

            if (reverse)
                invocation.proceed();
        }
    }

    private class Invoker
    {
        private final String methodIdentifier;

        private final MethodAccess access;

        Invoker(String methodIdentifier, MethodAccess access)
        {
            this.methodIdentifier = methodIdentifier;
            this.access = access;
        }

        void invoke(Object instance, MarkupWriter writer, Event event)
        {
            event.setMethodDescription(methodIdentifier);

            // As currently implemented, MethodAccess objects ignore excess parameters.

            MethodInvocationResult result = access.invoke(instance, writer);

            result.rethrow();

            event.storeResult(result.getReturnValue());
        }

    }

    private final Map<Class<? extends Annotation>, TransformMethodSignature> annotationToSignature = CollectionFactory
            .newMap();

    private final Map<String, Class<? extends Annotation>> nameToAnnotation = CollectionFactory.newCaseInsensitiveMap();

    private final Set<Class<? extends Annotation>> reverseAnnotations = CollectionFactory.newSet(AfterRenderBody.class,
            AfterRenderTemplate.class, AfterRender.class, CleanupRender.class);

    private final Set<TransformMethodSignature> lifecycleMethods = CollectionFactory.newSet();

    {
        annotationToSignature.put(SetupRender.class, TransformConstants.SETUP_RENDER_SIGNATURE);
        annotationToSignature.put(BeginRender.class, TransformConstants.BEGIN_RENDER_SIGNATURE);
        annotationToSignature.put(BeforeRenderTemplate.class, TransformConstants.BEFORE_RENDER_TEMPLATE_SIGNATURE);
        annotationToSignature.put(BeforeRenderBody.class, TransformConstants.BEFORE_RENDER_BODY_SIGNATURE);
        annotationToSignature.put(AfterRenderBody.class, TransformConstants.AFTER_RENDER_BODY_SIGNATURE);
        annotationToSignature.put(AfterRenderTemplate.class, TransformConstants.AFTER_RENDER_TEMPLATE_SIGNATURE);
        annotationToSignature.put(AfterRender.class, TransformConstants.AFTER_RENDER_SIGNATURE);
        annotationToSignature.put(CleanupRender.class, TransformConstants.CLEANUP_RENDER_SIGNATURE);

        for (Entry<Class<? extends Annotation>, TransformMethodSignature> me : annotationToSignature.entrySet())
        {
            nameToAnnotation.put(me.getValue().getMethodName(), me.getKey());
            lifecycleMethods.add(me.getValue());
        }
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        Map<Class, List<TransformMethod>> methods = mapRenderPhaseAnnotationToMethods(transformation);

        for (Class renderPhaseAnnotation : methods.keySet())
        {
            mapMethodsToRenderPhase(transformation, model, renderPhaseAnnotation, methods.get(renderPhaseAnnotation));
        }
    }

    private void mapMethodsToRenderPhase(ClassTransformation transformation, MutableComponentModel model,
            Class annotationType, List<TransformMethod> methods)
    {
        ComponentMethodAdvice renderPhaseAdvice = createAdviceForMethods(annotationType, methods);

        TransformMethodSignature renderPhaseSignature = annotationToSignature.get(annotationType);

        transformation.getOrCreateMethod(renderPhaseSignature).addAdvice(renderPhaseAdvice);

        model.addRenderPhase(annotationType);
    }

    private ComponentMethodAdvice createAdviceForMethods(Class annotationType, List<TransformMethod> methods)
    {
        boolean reverse = reverseAnnotations.contains(annotationType);

        List<Invoker> invokers = toInvokers(annotationType, methods, reverse);

        return new RenderPhaseMethodAdvice(reverse, invokers);
    }

    private List<Invoker> toInvokers(Class annotationType, List<TransformMethod> methods, boolean reverse)
    {
        List<Invoker> result = CollectionFactory.newList();

        for (TransformMethod method : methods)
        {
            MethodAccess methodAccess = toMethodAccess(method);

            Invoker invoker = new Invoker(method.getMethodIdentifier(), methodAccess);

            result.add(invoker);
        }

        if (reverse)
            Collections.reverse(result);

        return result;
    }

    private MethodAccess toMethodAccess(TransformMethod method)
    {
        validateAsRenderPhaseMethod(method);

        return method.getAccess();
    }

    private void validateAsRenderPhaseMethod(TransformMethod method)
    {
        String[] parameterTypes = method.getSignature().getParameterTypes();

        switch (parameterTypes.length)
        {
            case 0:
                break;

            case 1:
                if (parameterTypes[0].equals(MarkupWriter.class.getName()))
                    break;
            default:
                throw new RuntimeException(
                        String
                                .format(
                                        "Method %s is not a valid render phase method: it should take no parameters, or take a single parameter of type MarkupWriter.",
                                        method.getMethodIdentifier()));
        }
    }

    private Map<Class, List<TransformMethod>> mapRenderPhaseAnnotationToMethods(final ClassTransformation transformation)
    {
        Map<Class, List<TransformMethod>> map = CollectionFactory.newMap();

        List<TransformMethod> matches = matchAllMethodsNotOverriddenFromBaseClass(transformation);

        for (TransformMethod method : matches)
        {
            addMethodToRenderPhaseCategoryMap(map, method);
        }

        return map;
    }

    private void addMethodToRenderPhaseCategoryMap(Map<Class, List<TransformMethod>> map, TransformMethod method)
    {
        Class categorized = categorizeMethod(method);

        if (categorized != null)
            InternalUtils.addToMapList(map, categorized, method);
    }

    private Class categorizeMethod(TransformMethod method)
    {
        for (Class annotationClass : annotationToSignature.keySet())
        {
            if (method.getAnnotation(annotationClass) != null)
                return annotationClass;
        }

        return nameToAnnotation.get(method.getName());
    }

    private List<TransformMethod> matchAllMethodsNotOverriddenFromBaseClass(final ClassTransformation transformation)
    {
        return transformation.matchMethods(new Predicate<TransformMethod>()
        {
            public boolean accept(TransformMethod method)
            {
                return !method.isOverride() && !lifecycleMethods.contains(method.getSignature());
            }
        });

    }
}
