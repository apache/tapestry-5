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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.AfterRenderBody;
import org.apache.tapestry5.annotations.AfterRenderTemplate;
import org.apache.tapestry5.annotations.BeforeRenderBody;
import org.apache.tapestry5.annotations.BeforeRenderTemplate;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.CleanupRender;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.internal.util.MethodInvocationBuilder;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.MethodFilter;
import org.apache.tapestry5.services.TransformConstants;
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
    private static final String CHECK_ABORT_FLAG = "if ($2.isAborted()) return;";

    private final MethodInvocationBuilder invocationBuilder = new MethodInvocationBuilder();

    private final Map<Class, TransformMethodSignature> annotationToSignature = CollectionFactory
            .newMap();

    private final Map<String, Class> nameToAnnotation = CollectionFactory.newCaseInsensitiveMap();

    private final Set<Class> reverseAnnotations = CollectionFactory.newSet(AfterRenderBody.class,
            AfterRenderTemplate.class, AfterRender.class, CleanupRender.class);

    private final Set<TransformMethodSignature> lifecycleMethods = CollectionFactory.newSet();

    {
        annotationToSignature.put(SetupRender.class, TransformConstants.SETUP_RENDER_SIGNATURE);
        annotationToSignature.put(BeginRender.class, TransformConstants.BEGIN_RENDER_SIGNATURE);
        annotationToSignature.put(BeforeRenderTemplate.class,
                TransformConstants.BEFORE_RENDER_TEMPLATE_SIGNATURE);
        annotationToSignature.put(BeforeRenderBody.class,
                TransformConstants.BEFORE_RENDER_BODY_SIGNATURE);
        annotationToSignature.put(AfterRenderBody.class,
                TransformConstants.AFTER_RENDER_BODY_SIGNATURE);
        annotationToSignature.put(AfterRenderTemplate.class,
                TransformConstants.AFTER_RENDER_TEMPLATE_SIGNATURE);
        annotationToSignature.put(AfterRender.class, TransformConstants.AFTER_RENDER_SIGNATURE);
        annotationToSignature.put(CleanupRender.class, TransformConstants.CLEANUP_RENDER_SIGNATURE);

        for (Map.Entry<Class, TransformMethodSignature> me : annotationToSignature.entrySet())
        {
            nameToAnnotation.put(me.getValue().getMethodName(), me.getKey());
            lifecycleMethods.add(me.getValue());
        }

        // If we ever add more parameters to the methods, then we can add more to the invocation
        // builder. *Never* expose the Event parameter ($2), it is for internal use only.

        invocationBuilder.addParameter(MarkupWriter.class.getName(), "$1");
    }

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        Map<Class, List<TransformMethodSignature>> methods = CollectionFactory.newMap();

        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(TransformMethodSignature signature)
            {
                return !transformation.isMethodOverride(signature)
                        && !lifecycleMethods.contains(signature);
            }
        };

        for (TransformMethodSignature sig : transformation.findMethods(filter))
        {
            Class categorized = null;

            for (Class annotationClass : annotationToSignature.keySet())
            {
                if (transformation.getMethodAnnotation(sig, annotationClass) != null)
                {
                    categorized = annotationClass;
                    break;
                }
            }

            // If no annotation, see if the method name maps to an annotation class
            // and use that. Thus explicit annotations always override method name matching
            // as per TAP5-266

            if (categorized == null)
                categorized = nameToAnnotation.get(sig.getMethodName());

            if (categorized != null)
            {
                InternalUtils.addToMapList(methods, categorized, sig);
            }
        }

        if (methods.isEmpty())
            return;

        for (Map.Entry<Class, List<TransformMethodSignature>> me : methods.entrySet())
        {
            Class annotationClass = me.getKey();

            model.addRenderPhase(annotationClass);

            linkMethodsToRenderPhase(transformation, model, annotationToSignature
                    .get(annotationClass), reverseAnnotations.contains(annotationClass), me
                    .getValue());
        }
    }

    public void linkMethodsToRenderPhase(ClassTransformation transformation,
            MutableComponentModel model, TransformMethodSignature lifecycleMethodSignature,
            boolean reverse, List<TransformMethodSignature> methods)
    {
        String lifecycleMethodName = lifecycleMethodSignature.getMethodName();

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        // If in a subclass, and in normal order mode, invoke the super class version first.

        if (!(reverse || model.isRootClass()))
        {
            builder.addln("super.%s($$);", lifecycleMethodName);
            builder.addln(CHECK_ABORT_FLAG);
        }

        Iterator<TransformMethodSignature> i = reverse ? InternalUtils.reverseIterator(methods)
                : methods.iterator();

        builder.addln("try");
        builder.begin();

        while (i.hasNext())
            addMethodCallToBody(builder, i.next(), transformation);

        // In reverse order in a a subclass, invoke the super method last.

        if (reverse && !model.isRootClass())
            builder.addln("super.%s($$);", lifecycleMethodName);

        builder.end(); // try

        // Let runtime exceptions work up (they'll be caught at a higher level.
        // Wrap checked exceptions for later reporting.

        builder.addln("catch (RuntimeException ex) { throw ex; }");
        builder.addln("catch (Exception ex) { throw new RuntimeException(ex); }");

        builder.end();

        // Let's see if this works; for base classes, we are adding an empty method then adding a
        // non-empty method "on top of it".

        transformation.addMethod(lifecycleMethodSignature, builder.toString());
    }

    private void addMethodCallToBody(BodyBuilder builder, TransformMethodSignature sig,
            ClassTransformation transformation)
    {
        boolean isVoid = sig.getReturnType().equals("void");

        builder.addln("$2.setMethodDescription(\"%s\");", transformation.getMethodIdentifier(sig));

        if (!isVoid)
        {
            // If we're not going to invoke storeResult(), then there's no reason to invoke
            // setMethodDescription().

            builder.add("if ($2.storeResult(($w) ");
        }

        // This is the best part; the method can even be private and this still works. It's a lot
        // like how javac enables access to private members for inner classes (by introducing
        // synthetic, static methods).

        builder.add(invocationBuilder.buildMethodInvocation(sig, transformation));

        // Now, if non void ...

        if (!isVoid)
        {
            // Complete the call to storeResult(). If storeResult() returns true, then
            // the event is aborted and no further processing is required.

            builder.addln(")) return;");
        }
        else
            builder.addln(";");
    }
}
