// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.util.MethodInvocationBuilder;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.MethodFilter;
import org.apache.tapestry5.services.TransformMethodSignature;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

/**
 * Converts one of the methods of {@link org.apache.tapestry5.runtime.Component} into a chain of command that, itself,
 * invokes certain methods (render phase methods) marked with an annotation, or named in a specific way.
 */
public class RenderPhaseMethodWorker implements ComponentClassTransformWorker
{
    private static final String CHECK_ABORT_FLAG = "if ($2.isAborted()) return;";

    private final Class<? extends Annotation> methodAnnotation;

    private final TransformMethodSignature lifecycleMethodSignature;

    private final String lifecycleMethodName;

    private final boolean reverse;

    private final MethodInvocationBuilder invocationBuilder = new MethodInvocationBuilder();

    /**
     * Normal method invocation: parent class, then methods in ascending alphabetical order. Reverse order: method in
     * descending alphabetical order, then parent class.
     *
     * @param lifecycleMethodSignature the signature of the method to be implemented in the component class
     * @param methodAnnotation         the class of the corresponding annotation
     * @param reverse                  if true, the normal method invocation order is reversed
     */
    public RenderPhaseMethodWorker(TransformMethodSignature lifecycleMethodSignature,
                                   Class<? extends Annotation> methodAnnotation, boolean reverse)
    {
        this.lifecycleMethodSignature = lifecycleMethodSignature;
        this.methodAnnotation = methodAnnotation;
        this.reverse = reverse;
        lifecycleMethodName = lifecycleMethodSignature.getMethodName();

        // If we ever add more parameters to the methods, then we can add more to the invocation
        // builder.
        // *Never* expose the Event parameter ($2), it is for internal use only.

        invocationBuilder.addParameter(MarkupWriter.class.getName(), "$1");
    }

    @Override
    public String toString()
    {
        return String.format("RenderPhaseMethodWorker[%s]", methodAnnotation.getName());
    }

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(TransformMethodSignature signature)
            {
                // These methods get added to base classes and otherwise fall into this filter. If
                // we don't include this filter, then we get endless loops.

                if (signature.equals(lifecycleMethodSignature)) return false;

                // A degenerate case would be a method, say beginRender(), with an conflicting
                // annotation, say @AfterRender. In that case, this code is broken, as the method
                // will be invoked for both phases!

                return (correctName(signature) || correctAnnotation(signature)) &&
                        !transformation.isMethodOverride(signature);
            }

            private boolean correctAnnotation(TransformMethodSignature signature)
            {
                return transformation.getMethodAnnotation(signature, methodAnnotation) != null;
            }

            private boolean correctName(TransformMethodSignature signature)
            {
                return signature.getMethodName().equals(lifecycleMethodName);
            }
        };

        List<TransformMethodSignature> methods = transformation.findMethods(filter);

        // Except in the root class, don't bother to add a new method unless there's something to
        // call (beside super).

        if (methods.isEmpty()) return;

        model.addRenderPhase(methodAnnotation);

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        // If in a subclass, and in normal order mode, invoke the super class version first.

        if (!(reverse || model.isRootClass()))
        {
            builder.addln("super.%s($$);", lifecycleMethodName);
            builder.addln(CHECK_ABORT_FLAG);
        }

        Iterator<TransformMethodSignature> i = reverse ? InternalUtils.reverseIterator(methods) : methods
                .iterator();

        builder.addln("try");
        builder.begin();

        while (i.hasNext())
            addMethodCallToBody(builder, i.next(), transformation);

        // In reverse order in a a subclass, invoke the super method last.

        if (reverse && !model.isRootClass()) builder.addln("super.%s($$);", lifecycleMethodName);


        builder.end(); // try

        // Let runtime exceptions work up (they'll be caught at a higher level.
        // Wrap checked exceptions for later reporting.

        builder.addln("catch (RuntimeException ex) { throw ex; }");
        builder.addln("catch (Exception ex) { throw new RuntimeException(ex); }");

        builder.end();

        // Let's see if this works; for base classes, we are adding an empty method the adding a
        // non-empty
        // method "on top of it".

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
