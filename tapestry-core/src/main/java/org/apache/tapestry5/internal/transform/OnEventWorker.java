//
// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.QueryParameter;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.TransformMethod;
import org.apache.tapestry5.services.TransformMethodSignature;
import org.apache.tapestry5.services.ValueEncoderSource;

/**
 * Provides implementations of the
 * {@link org.apache.tapestry5.runtime.Component#dispatchComponentEvent(org.apache.tapestry5.runtime.ComponentEvent)}
 * method, based on {@link org.apache.tapestry5.annotations.OnEvent} annotations.
 */
public class OnEventWorker implements ComponentClassTransformWorker
{

    private final Request request;

    private final ValueEncoderSource valueEncoderSource;

    private final ComponentClassCache classCache;

    /**
     * Stores a couple of special parameter type mappings that are used when matching the entire event context
     * (either as Object[] or EventContext).
     */
    private final Map<String, EventHandlerMethodParameterSource> parameterTypeToSource = CollectionFactory.newMap();

    {
        // Object[] and List are out-dated and may be deprecated some day

        parameterTypeToSource.put("java.lang.Object[]", new EventHandlerMethodParameterSource()
        {

            public Object valueForEventHandlerMethodParameter(ComponentEvent event)
            {
                return event.getContext();
            }
        });

        parameterTypeToSource.put(List.class.getName(), new EventHandlerMethodParameterSource()
        {

            public Object valueForEventHandlerMethodParameter(ComponentEvent event)
            {
                return Arrays.asList(event.getContext());
            }
        });

        // This is better, as the EventContext maintains the original objects (or strings)
        // and gives the event handler method access with coercion
        parameterTypeToSource.put(EventContext.class.getName(), new EventHandlerMethodParameterSource()
        {

            public Object valueForEventHandlerMethodParameter(ComponentEvent event)
            {
                return event.getEventContext();
            }
        });
    }

    public OnEventWorker(Request request, ValueEncoderSource valueEncoderSource, ComponentClassCache classCache)
    {
        this.request = request;
        this.valueEncoderSource = valueEncoderSource;
        this.classCache = classCache;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<TransformMethod> methods = matchEventHandlerMethods(transformation);

        if (methods.isEmpty())
            return;

        List<EventHandlerMethodInvoker> invokers = toInvokers(transformation.getClassName(), methods);

        updateModelWithHandledEvents(model, invokers);

        adviseDispatchComponentEventMethod(transformation, invokers);
    }

    private void adviseDispatchComponentEventMethod(ClassTransformation transformation,
            List<EventHandlerMethodInvoker> invokers)
    {
        ComponentMethodAdvice advice = createDispatchComponentEventAdvice(invokers);

        transformation.getOrCreateMethod(TransformConstants.DISPATCH_COMPONENT_EVENT).addAdvice(advice);
    }

    private ComponentMethodAdvice createDispatchComponentEventAdvice(final List<EventHandlerMethodInvoker> invokers)
    {
        return new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                // Invoke the super-class implementation first. If no super-class,
                // this will do nothing and return false.

                invocation.proceed();

                ComponentEvent event = (ComponentEvent) invocation.getParameter(0);

                if (invokeEventHandlers(event, invocation.getInstance()))
                    invocation.overrideResult(true);
            }

            private boolean invokeEventHandlers(ComponentEvent event, Object instance)
            {
                // If the super-class aborted the event (some super-class method return non-null),
                // then it's all over, don't even check for handlers in this class.

                if (event.isAborted())
                    return false;

                boolean didInvokeSomeHandler = false;

                for (EventHandlerMethodInvoker invoker : invokers)
                {
                    if (event.matches(invoker.getEventType(), invoker.getComponentId(), invoker
                            .getMinContextValueCount()))
                    {
                        didInvokeSomeHandler = true;

                        invoker.invokeEventHandlerMethod(event, instance);

                        if (event.isAborted())
                            break;
                    }
                }

                return didInvokeSomeHandler;
            }
        };
    }

    private void updateModelWithHandledEvents(MutableComponentModel model,
            final List<EventHandlerMethodInvoker> invokers)
    {
        for (EventHandlerMethodInvoker invoker : invokers)
        {
            model.addEventHandler(invoker.getEventType());
        }
    }

    private List<TransformMethod> matchEventHandlerMethods(ClassTransformation transformation)
    {
        return transformation.matchMethods(new Predicate<TransformMethod>()
        {
            public boolean accept(TransformMethod method)
            {
                return (hasCorrectPrefix(method) || hasAnnotation(method)) && !method.isOverride();
            }

            private boolean hasCorrectPrefix(TransformMethod method)
            {
                return method.getName().startsWith("on");
            }

            private boolean hasAnnotation(TransformMethod method)
            {
                return method.getAnnotation(OnEvent.class) != null;
            }
        });
    }

    private List<EventHandlerMethodInvoker> toInvokers(String componentClassName, List<TransformMethod> methods)
    {
        List<EventHandlerMethodInvoker> result = CollectionFactory.newList();

        for (TransformMethod method : methods)
        {
            result.add(toInvoker(componentClassName, method));
        }

        return result;
    }

    private EventHandlerMethodInvoker toInvoker(final String componentClassName, TransformMethod method)
    {
        OnEvent annotation = method.getAnnotation(OnEvent.class);

        String methodName = method.getName();

        String eventType = extractEventType(methodName, annotation);
        String componentId = extractComponentId(methodName, annotation);

        final TransformMethodSignature signature = method.getSignature();

        String[] parameterTypes = signature.getParameterTypes();

        if (parameterTypes.length == 0)
            return new BaseEventHandlerMethodInvoker(method, eventType, componentId);

        final List<EventHandlerMethodParameterSource> sources = CollectionFactory.newList();

        // I'd refactor a bit more of this if Java had covariant return types.

        int contextIndex = 0;

        for (int i = 0; i < parameterTypes.length; i++)
        {
            String type = parameterTypes[i];

            EventHandlerMethodParameterSource source = parameterTypeToSource.get(type);

            if (source != null)
            {
                sources.add(source);
                continue;
            }

            QueryParameter parameterAnnotation = method.getParameterAnnotation(i, QueryParameter.class);

            if (parameterAnnotation != null)
            {
                String parameterName = parameterAnnotation.value();

                sources.add(createQueryParameterSource(componentClassName, signature, i, parameterName, type,
                        parameterAnnotation.allowBlank()));
                continue;
            }

            // Note: probably safe to do the conversion to Class early (class load time)
            // as parameters are rarely (if ever) component classes.

            final int parameterIndex = contextIndex++;

            sources.add(createEventContextSource(type, parameterIndex));
        }

        return createInvoker(method, eventType, componentId, contextIndex, sources);
    }

    private EventHandlerMethodParameterSource createQueryParameterSource(final String componentClassName,
            final TransformMethodSignature signature, final int parameterIndex, final String parameterName,
            final String parameterTypeName, final boolean allowBlank)
    {
        return new EventHandlerMethodParameterSource()
        {
            @SuppressWarnings("unchecked")
            public Object valueForEventHandlerMethodParameter(ComponentEvent event)
            {
                try
                {
                    String parameterValue = request.getParameter(parameterName);

                    if (!allowBlank && InternalUtils.isBlank(parameterValue))
                        throw new RuntimeException(String.format(
                                "The value for query parameter '%s' was blank, but a non-blank value is needed.",
                                parameterName));

                    Class parameterType = classCache.forName(parameterTypeName);

                    ValueEncoder valueEncoder = valueEncoderSource.getValueEncoder(parameterType);

                    Object value = valueEncoder.toValue(parameterValue);

                    if (parameterType.isPrimitive() && value == null)
                        throw new RuntimeException(
                                String
                                        .format(
                                                "Query parameter '%s' evaluates to null, but the event method parameter is type %s, a primitive.",
                                                parameterName, parameterType.getName()));

                    return value;
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(
                            String
                                    .format(
                                            "Unable process query parameter '%s' as parameter #%d of event handler method %s (in class %s): %s",
                                            parameterName, parameterIndex + 1, signature, componentClassName,
                                            InternalUtils.toMessage(ex)), ex);
                }
            }
        };
    }

    private EventHandlerMethodInvoker createInvoker(TransformMethod method, String eventType, String componentId,
            final int minContextCount, final List<EventHandlerMethodParameterSource> sources)
    {
        return new BaseEventHandlerMethodInvoker(method, eventType, componentId)
        {
            final int count = sources.size();

            @Override
            public int getMinContextValueCount()
            {
                return minContextCount;
            }

            @Override
            protected Object[] constructParameters(ComponentEvent event)
            {
                Object[] parameters = new Object[count];

                for (int i = 0; i < count; i++)
                {
                    parameters[i] = sources.get(i).valueForEventHandlerMethodParameter(event);
                }

                return parameters;
            }
        };
    }

    private EventHandlerMethodParameterSource createEventContextSource(final String type, final int parameterIndex)
    {
        return new EventHandlerMethodParameterSource()
        {
            public Object valueForEventHandlerMethodParameter(ComponentEvent event)
            {
                return event.coerceContext(parameterIndex, type);
            }
        };
    }

    /**
     * Returns the component id to match against, or the empty
     * string if the component id is not specified. The component id
     * is provided by the OnEvent annotation or (if that is not present)
     * by the part of the method name following "From" ("onActionFromFoo").
     */
    private String extractComponentId(String methodName, OnEvent annotation)
    {
        if (annotation != null)
            return annotation.component();

        // Method name started with "on". Extract the component id, if present.

        int fromx = methodName.indexOf("From");

        if (fromx < 0)
            return "";

        return methodName.substring(fromx + 4);
    }

    /**
     * Returns the event name to match against, as specified in the annotation
     * or (if the annotation is not present) extracted from the name of the method.
     * "onActionFromFoo" or just "onAction".
     */
    private String extractEventType(String methodName, OnEvent annotation)
    {
        if (annotation != null)
            return annotation.value();

        int fromx = methodName.indexOf("From");

        // The first two characters are always "on" as in "onActionFromFoo".
        return fromx == -1 ? methodName.substring(2) : methodName.substring(2, fromx);
    }
}
