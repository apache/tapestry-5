// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.*;

import java.util.Arrays;
import java.util.List;

/**
 * Provides implementations of the {@link org.apache.tapestry5.runtime.Component#dispatchComponentEvent(org.apache.tapestry5.runtime.ComponentEvent)}
 * method, based on {@link org.apache.tapestry5.annotations.OnEvent} annotations.
 */
public class OnEventWorker implements ComponentClassTransformWorker
{
    static final String OBJECT_ARRAY_TYPE = "java.lang.Object[]";

    static final String EVENT_CONTEXT_TYPE = EventContext.class.getName();

    static final String LIST_TYPE = List.class.getName();

    private final static int ANY_NUMBER_OF_PARAMETERS = -1;

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(TransformMethodSignature signature)
            {
                return (hasCorrectPrefix(signature) || hasAnnotation(signature)) &&
                        !transformation.isMethodOverride(signature);
            }

            private boolean hasCorrectPrefix(TransformMethodSignature signature)
            {
                return signature.getMethodName().startsWith("on");
            }

            private boolean hasAnnotation(TransformMethodSignature signature)
            {
                return transformation.getMethodAnnotation(signature, OnEvent.class) != null;
            }
        };

        List<TransformMethodSignature> methods = transformation.findMethods(filter);

        // No methods, no work.

        if (methods.isEmpty()) return;

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        builder.addln("if ($1.isAborted()) return $_;");

        builder.addln("try");
        builder.begin();

        for (TransformMethodSignature method : methods)
            addCodeForMethod(builder, method, transformation, model);

        builder.end(); // try

        // Runtime exceptions pass right through.

        builder.addln("catch (RuntimeException ex) { throw ex; }");

        // Wrap others in a RuntimeException to communicate them up.

        builder.addln("catch (Exception ex) { throw new RuntimeException(ex); } ");

        builder.end();

        transformation.extendMethod(TransformConstants.DISPATCH_COMPONENT_EVENT, builder.toString());
    }


    private void addCodeForMethod(BodyBuilder builder, TransformMethodSignature method,
                                  ClassTransformation transformation, MutableComponentModel model)
    {
        // $1 is the event

        int parameterCount = getParameterCount(method);

        OnEvent annotation = transformation.getMethodAnnotation(method, OnEvent.class);

        String eventType = extractEventType(method, annotation);

        String componentId = extractComponentId(method, annotation);


        builder.addln("if ($1.matches(\"%s\", \"%s\", %d))", eventType, componentId, parameterCount);
        builder.begin();

        // Ensure that we return true, because *some* event handler method was invoked,
        // even if it chose not to abort the event.

        builder.addln("$_ = true;");

        builder.addln("$1.setMethodDescription(\"%s\");", transformation.getMethodIdentifier(method));

        boolean isNonVoid = !method.getReturnType().equals("void");

        // Store the result, converting primitives to wrappers automatically.

        if (isNonVoid) builder.add("if ($1.storeResult(($w) ");

        builder.add("%s(", method.getMethodName());

        buildMethodParameters(builder, method);

        if (isNonVoid) builder.addln("))) return true;");
        else builder.addln(");");

        builder.end();

        // Indicate that the eventType is handled.

        model.addEventHandler(eventType);
    }

    private String extractComponentId(TransformMethodSignature method, OnEvent annotation)
    {
        if (annotation != null) return annotation.component();

        // Method name started with "on". Extract the component id, if present.

        String name = method.getMethodName();

        int fromx = name.indexOf("From");

        if (fromx < 0) return "";

        return name.substring(fromx + 4);
    }

    private String extractEventType(TransformMethodSignature method, OnEvent annotation)
    {
        if (annotation != null) return annotation.value();

        // Method name started with "on". Extract the event type.

        String name = method.getMethodName();

        int fromx = name.indexOf("From");

        return fromx == -1 ? name.substring(2) : name.substring(2, fromx);
    }

    private int getParameterCount(TransformMethodSignature method)
    {
        String[] types = method.getParameterTypes();

        if (types.length == 0) return 0;

        if (types.length == 1)
        {
            String soloType = types[0];

            if (soloType.equals(OBJECT_ARRAY_TYPE) || soloType.equals(EVENT_CONTEXT_TYPE) || soloType.equals(LIST_TYPE))
                return ANY_NUMBER_OF_PARAMETERS;
        }

        return types.length;
    }

    private void buildMethodParameters(BodyBuilder builder, TransformMethodSignature method)
    {
        int contextIndex = 0;

        for (int i = 0; i < method.getParameterTypes().length; i++)
        {
            if (i > 0) builder.add(", ");

            String type = method.getParameterTypes()[i];

            // Type Object[] is a special case, it gets all of the context parameters in one go.

            if (type.equals(OBJECT_ARRAY_TYPE))
            {
                builder.add("$1.getContext()");
                continue;
            }

            // Added for TAPESTRY-2177

            if (type.equals(EVENT_CONTEXT_TYPE))
            {
                builder.add("$1.getEventContext()");
                continue;
            }

            // Added for TAPESTRY-1999

            if (type.equals(LIST_TYPE))
            {
                builder.add("%s.asList($1.getContext())", Arrays.class.getName());
                continue;
            }

            boolean isPrimitive = TransformUtils.isPrimitive(type);
            String wrapperType = TransformUtils.getWrapperTypeName(type);

            // Add a cast to the wrapper type up front

            if (isPrimitive) builder.add("(");

            // A cast is always needed (i.e. from java.lang.Object to, say, java.lang.String, etc.).
            // The wrapper type will be the actual type unless its a primitive, in which case it
            // really will be the wrapper type.

            builder.add("(%s)", wrapperType);

            // The strings for desired type name will likely repeat a bit; it may be
            // worth it to inject them as final fields. Could increase the number
            // of constructor parameters pretty dramatically, however, and will reduce
            // the readability of the output method bodies.

            builder.add("$1.coerceContext(%d, \"%s\")", contextIndex++, wrapperType);

            // and invoke a method on the cast value to get back to primitive
            if (isPrimitive) builder.add(").%s()", TransformUtils.getUnwrapperMethodName(type));
        }
    }
}
