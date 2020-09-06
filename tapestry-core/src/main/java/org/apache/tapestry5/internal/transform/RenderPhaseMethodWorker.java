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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.runtime.Event;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Converts one of the methods of {@link org.apache.tapestry5.runtime.Component} into a chain of
 * command that, itself,
 * invokes certain methods (render phase methods) marked with an annotation, or named in a specific
 * way.
 */
@SuppressWarnings("all")
public class RenderPhaseMethodWorker implements ComponentClassTransformWorker2
{
    private final Map<Class<? extends Annotation>, MethodDescription> annotationToDescription = CollectionFactory.newMap();

    private final Map<String, Class<? extends Annotation>> nameToAnnotation = CollectionFactory.newCaseInsensitiveMap();

    private final Set<Class<? extends Annotation>> reverseAnnotations = CollectionFactory.newSet(AfterRenderBody.class,
            AfterRenderTemplate.class, AfterRender.class, CleanupRender.class);

    private final Set<MethodDescription> lifecycleMethods = CollectionFactory.newSet();

    {

        annotationToDescription.put(SetupRender.class, TransformConstants.SETUP_RENDER_DESCRIPTION);
        annotationToDescription.put(BeginRender.class, TransformConstants.BEGIN_RENDER_DESCRIPTION);
        annotationToDescription.put(BeforeRenderTemplate.class, TransformConstants.BEFORE_RENDER_TEMPLATE_DESCRIPTION);
        annotationToDescription.put(BeforeRenderBody.class, TransformConstants.BEFORE_RENDER_BODY_DESCRIPTION);
        annotationToDescription.put(AfterRenderBody.class, TransformConstants.AFTER_RENDER_BODY_DESCRIPTION);
        annotationToDescription.put(AfterRenderTemplate.class, TransformConstants.AFTER_RENDER_TEMPLATE_DESCRIPTION);
        annotationToDescription.put(AfterRender.class, TransformConstants.AFTER_RENDER_DESCRIPTION);
        annotationToDescription.put(CleanupRender.class, TransformConstants.CLEANUP_RENDER_DESCRIPTION);

        for (Entry<Class<? extends Annotation>, MethodDescription> me : annotationToDescription.entrySet())
        {
            nameToAnnotation.put(me.getValue().methodName, me.getKey());
            lifecycleMethods.add(me.getValue());
        }

    }


    private InstructionBuilderCallback JUST_RETURN = new InstructionBuilderCallback()
    {
        public void doBuild(InstructionBuilder builder)
        {
            builder.returnDefaultValue();
        }
    };

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        Map<Class, List<PlasticMethod>> methods = mapRenderPhaseAnnotationToMethods(plasticClass);

        for (Class renderPhaseAnnotation : methods.keySet())
        {
            mapMethodsToRenderPhase(plasticClass, support.isRootTransformation(), renderPhaseAnnotation, methods.get(renderPhaseAnnotation));

            model.addRenderPhase(renderPhaseAnnotation);
        }
    }

    private void mapMethodsToRenderPhase(final PlasticClass plasticClass, final boolean isRoot, Class annotationType, List<PlasticMethod> methods)
    {

        // The method, defined by Component, that will in turn invoke the other methods.

        final MethodDescription interfaceMethodDescription = annotationToDescription.get(annotationType);
        PlasticMethod interfaceMethod = plasticClass.introduceMethod(interfaceMethodDescription);

        final boolean reverse = reverseAnnotations.contains(annotationType);

        final Flow<PlasticMethod> orderedMethods =
                reverse ? F.flow(methods).reverse()
                        : F.flow(methods);

        // You'd think we'd need to catch non-RuntimeExceptions thrown by invoked methods ... turns out
        // that the distinction between checked and non-checked exception is a concern only of the Java compiler,
        // not the runtime or JVM. This did require a small change to ComponentPageElementImpl, to catch Exception (previously
        // it caught RuntimeException).
        interfaceMethod.changeImplementation(new InstructionBuilderCallback()
        {
            private void addSuperCall(InstructionBuilder builder)
            {
                builder.loadThis().loadArguments().invokeSpecial(plasticClass.getSuperClassName(), interfaceMethodDescription);
            }

            private void invokeMethod(InstructionBuilder builder, PlasticMethod method)
            {
                // First, tell the Event object what method is being invoked.

                builder.loadArgument(1);
                builder.loadConstant( method.getMethodIdentifier());
                builder.invoke(Event.class, void.class, "setMethodDescription", String.class);

                builder.loadThis();

                // Methods either take no parameters, or take a MarkupWriter parameter.

                if (method.getParameters().size() > 0)
                {
                    builder.loadArgument(0);
                }

                builder.invokeVirtual(method);

                // Non-void methods will pass a value to the event.

                if (!method.isVoid())
                {
                    builder.boxPrimitive(method.getDescription().returnType);
                    builder.loadArgument(1).swap();

                    builder.invoke(Event.class, boolean.class, "storeResult", Object.class);

                    builder.when(Condition.NON_ZERO, JUST_RETURN);
                }
            }

            public void doBuild(InstructionBuilder builder)
            {
                if (!reverse && !isRoot)
                {
                    addSuperCall(builder);

                    builder.loadArgument(1).invoke(Event.class, boolean.class, "isAborted");

                    builder.when(Condition.NON_ZERO, JUST_RETURN);
                }

                for (PlasticMethod invokedMethod : orderedMethods)
                {
                    invokeMethod(builder, invokedMethod);
                }

                if (reverse && !isRoot)
                {
                    addSuperCall(builder);
                }

                builder.returnDefaultValue();
            }
        });
    }


    private Map<Class, List<PlasticMethod>> mapRenderPhaseAnnotationToMethods(PlasticClass plasticClass)
    {
        Map<Class, List<PlasticMethod>> map = CollectionFactory.newMap();

        Flow<PlasticMethod> matches = matchAllMethodsNotOverriddenFromBaseClass(plasticClass);

        for (PlasticMethod method : matches)
        {
            addMethodToRenderPhaseCategoryMap(map, method);
        }

        return map;
    }


    private void addMethodToRenderPhaseCategoryMap(Map<Class, List<PlasticMethod>> map, PlasticMethod method)
    {
        Class categorized = categorizeMethod(method);

        if (categorized != null)
        {
            validateAsRenderPhaseMethod(method);

            InternalUtils.addToMapList(map, categorized, method);
        }
    }


    private Class categorizeMethod(PlasticMethod method)
    {
        for (Class annotationClass : annotationToDescription.keySet())
        {
            if (method.hasAnnotation(annotationClass))
                return annotationClass;
        }

        return nameToAnnotation.get(method.getDescription().methodName);
    }

    private void validateAsRenderPhaseMethod(PlasticMethod method)
    {
        final String[] argumentTypes = method.getDescription().argumentTypes;

        switch (argumentTypes.length)
        {
            case 0:
                break;

            case 1:
                if (argumentTypes[0].equals(MarkupWriter.class.getName()))
                    break;
            default:
                throw new RuntimeException(
                        String.format(
                                "Method %s is not a valid render phase method: it should take no parameters, or take a single parameter of type MarkupWriter.",
                                method.toString()));
        }
    }


    private Flow<PlasticMethod> matchAllMethodsNotOverriddenFromBaseClass(final PlasticClass plasticClass)
    {
        return F.flow(plasticClass.getMethods()).filter(new Predicate<PlasticMethod>()
        {
            public boolean accept(PlasticMethod method)
            {
                return !method.isOverride() && !lifecycleMethods.contains(method.getDescription());
            }
        });
    }
}
