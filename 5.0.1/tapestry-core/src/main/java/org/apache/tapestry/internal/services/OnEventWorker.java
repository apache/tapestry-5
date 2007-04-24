// Copyright 2006 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.MethodFilter;
import org.apache.tapestry.services.MethodSignature;
import org.apache.tapestry.services.TransformConstants;
import org.apache.tapestry.services.TransformUtils;

/**
 * Provides implementations of the
 * {@link Component#handleComponentEvent(org.apache.tapestry.runtime.ComponentEvent)} method, based
 * on {@link OnEvent} annotations.
 */
public class OnEventWorker implements ComponentClassTransformWorker
{
    static final String OBJECT_ARRAY_TYPE = "java.lang.Object[]";

    private final String[] _empty = new String[0];

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(MethodSignature signature)
            {
                return signature.getMethodName().startsWith("on")
                        || transformation.getMethodAnnotation(signature, OnEvent.class) != null;
            };
        };

        List<MethodSignature> methods = transformation.findMethods(filter);

        // No methods, no work.

        if (methods.isEmpty())
            return;

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        builder.addln("if ($1.isAborted()) return $_;");

        for (MethodSignature method : methods)
            addCodeForMethod(builder, method, transformation);

        builder.end();

        transformation.extendMethod(TransformConstants.HANDLE_COMPONENT_EVENT, builder.toString());
    }

    private void addCodeForMethod(BodyBuilder builder, MethodSignature method,
            ClassTransformation transformation)
    {
        // $1 is the event

        int closeCount = 0;

        OnEvent annotation = transformation.getMethodAnnotation(method, OnEvent.class);

        String[] eventTypes = extractEventTypes(method, annotation);

        if (eventTypes.length > 0)
        {
            String fieldName = transformation.addInjectedField(
                    String[].class,
                    "eventTypes",
                    eventTypes);

            builder.addln("if ($1.matchesByEventType(%s))", fieldName);
            builder.begin();

            closeCount++;
        }

        String[] componentIds = extractComponentIds(method, annotation);

        if (componentIds.length > 0)
        {
            String fieldName = transformation.addInjectedField(
                    String[].class,
                    "componentIds",
                    componentIds);

            builder.addln("if ($1.matchesByComponentId(%s, %s))", transformation
                    .getResourcesFieldName(), fieldName);
            builder.begin();

            closeCount++;
        }

        // Ensure that we return true, because *some* event handler method was invoked,
        // even if it chose not to abort the event.

        builder.addln("$_ = true;");

        // Several subsequent calls need to know the method name.

        builder.addln("$1.setSource(this, \"%s.%s\");", transformation.getClassName(), method
                .getMediumDescription());

        boolean isNonVoid = !method.getReturnType().equals("void");

        // Store the result, converting primitives to wrappers automatically.

        if (isNonVoid)
            builder.add("if ($1.storeResult(($w) ");

        builder.add("%s(", method.getMethodName());

        buildMethodParameters(builder, method);

        if (isNonVoid)
            builder.addln("))) return true;");
        else
            builder.addln(");");

        for (int i = 0; i < closeCount; i++)
            builder.end();
    }

    private String[] extractComponentIds(MethodSignature method, OnEvent annotation)
    {
        if (annotation != null)
            return annotation.component();

        // Method name started with "on". Extract the component id, if present.

        String name = method.getMethodName();

        int fromx = name.indexOf("From");

        if (fromx < 0)
            return _empty;

        String componentId = name.substring(fromx + 4);

        return new String[]
        { componentId };
    }

    private String[] extractEventTypes(MethodSignature method, OnEvent annotation)
    {
        if (annotation != null)
            return annotation.value();

        // Method name started with "on". Extract the event type.

        String name = method.getMethodName();

        int fromx = name.indexOf("From");

        String eventName = fromx == -1 ? name.substring(2) : name.substring(2, fromx);

        // This is intended for onAnyFromComponentId, but just onAny works too (and is dangerous).

        if (eventName.equals("AnyEvent"))
            return _empty;

        return new String[]
        { eventName };
    }

    private void buildMethodParameters(BodyBuilder builder, MethodSignature method)
    {
        int contextIndex = 0;

        for (int i = 0; i < method.getParameterTypes().length; i++)
        {
            if (i > 0)
                builder.add(", ");

            String type = method.getParameterTypes()[i];

            // Type Object[] is a special case, it gets all of the context parameters in one go.

            if (type.equals(OBJECT_ARRAY_TYPE))
            {
                builder.add("$1.getContext()");
                continue;
            }

            boolean isPrimitive = TransformUtils.isPrimitive(type);
            String wrapperType = TransformUtils.getWrapperTypeName(type);

            // Add a cast to the wrapper type up front

            if (isPrimitive)
                builder.add("(");

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
            if (isPrimitive)
                builder.add(").%s()", TransformUtils.getUnwrapperMethodName(type));
        }
    }
}
