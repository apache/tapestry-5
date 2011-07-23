//
// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Provides implementations of the
 * {@link org.apache.tapestry5.runtime.Component#dispatchComponentEvent(org.apache.tapestry5.runtime.ComponentEvent)}
 * method, based on {@link org.apache.tapestry5.annotations.OnEvent} annotations and naming conventions.
 */
public class OnEventWorker implements ComponentClassTransformWorker2
{
    private final Request request;

    private final ValueEncoderSource valueEncoderSource;

    private final ComponentClassCache classCache;

    /**
     * Stores a couple of special parameter type mappings that are used when matching the entire event context
     * (either as Object[] or EventContext).
     */
    private final Map<String, EventHandlerMethodParameterProvider> parameterTypeToProvider = CollectionFactory.newMap();

    {
        // Object[] and List are out-dated and may be deprecated some day

        parameterTypeToProvider.put("java.lang.Object[]", new EventHandlerMethodParameterProvider()
        {

            public Object valueForEventHandlerMethodParameter(ComponentEvent event)
            {
                return event.getContext();
            }
        });

        parameterTypeToProvider.put(List.class.getName(), new EventHandlerMethodParameterProvider()
        {

            public Object valueForEventHandlerMethodParameter(ComponentEvent event)
            {
                return Arrays.asList(event.getContext());
            }
        });

        // This is better, as the EventContext maintains the original objects (or strings)
        // and gives the event handler method access with coercion
        parameterTypeToProvider.put(EventContext.class.getName(), new EventHandlerMethodParameterProvider()
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

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        Flow<PlasticMethod> methods = matchEventHandlerMethods(plasticClass);

        if (methods.isEmpty())
            return;

        Flow<EventHandlerMethodInvoker> invokers = toInvokers(plasticClass.getClassName(), methods);

        updateModelWithHandledEvents(model, invokers);

        adviseDispatchComponentEventMethod(plasticClass, invokers);
    }

    private void adviseDispatchComponentEventMethod(PlasticClass plasticClass, Flow<EventHandlerMethodInvoker> invokers)
    {
        MethodAdvice advice = createDispatchComponentEventAdvice(invokers);

        plasticClass.introduceMethod(TransformConstants.DISPATCH_COMPONENT_EVENT_DESCRIPTION).addAdvice(advice);
    }

    private MethodAdvice createDispatchComponentEventAdvice(Flow<EventHandlerMethodInvoker> invokers)
    {
        final EventHandlerMethodInvoker[] invokersArray = invokers.toArray(EventHandlerMethodInvoker.class);

        return new MethodAdvice()
        {
            public void advise(MethodInvocation invocation)
            {
                // Invoke the super-class implementation first. If no super-class,
                // this will do nothing and return false.

                invocation.proceed();

                ComponentEvent event = (ComponentEvent) invocation.getParameter(0);

                if (invokeEventHandlers(event, invocation.getInstance()))
                    invocation.setReturnValue(true);
            }

            private boolean invokeEventHandlers(ComponentEvent event, Object instance)
            {
                // If the super-class aborted the event (some super-class method return non-null),
                // then it's all over, don't even check for handlers in this class.

                if (event.isAborted())
                    return false;

                boolean didInvokeSomeHandler = false;

                for (EventHandlerMethodInvoker invoker : invokersArray)
                {
                    if (event.matches(invoker.getEventType(), invoker.getComponentId(),
                            invoker.getMinContextValueCount()))
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

    private void updateModelWithHandledEvents(final MutableComponentModel model,
            Flow<EventHandlerMethodInvoker> invokers)
    {
        invokers.each(new Worker<EventHandlerMethodInvoker>()
        {
            public void work(EventHandlerMethodInvoker value)
            {
                model.addEventHandler(value.getEventType());
            }
        });
    }

    private Flow<PlasticMethod> matchEventHandlerMethods(PlasticClass plasticClass)
    {
        return F.flow(plasticClass.getMethods()).filter(new Predicate<PlasticMethod>()
        {
            public boolean accept(PlasticMethod method)
            {
                return (hasCorrectPrefix(method) || hasAnnotation(method)) && !method.isOverride();
            }

            private boolean hasCorrectPrefix(PlasticMethod method)
            {
                return method.getDescription().methodName.startsWith("on");
            }

            private boolean hasAnnotation(PlasticMethod method)
            {
                return method.hasAnnotation(OnEvent.class);
            }

        });
    }

    private Flow<EventHandlerMethodInvoker> toInvokers(final String componentClassName, Flow<PlasticMethod> methods)
    {
        return methods.map(new Mapper<PlasticMethod, EventHandlerMethodInvoker>()
        {
            public EventHandlerMethodInvoker map(PlasticMethod element)
            {
                return toInvoker(componentClassName, element);
            }
        });
    }

    private EventHandlerMethodInvoker toInvoker(final String componentClassName, PlasticMethod method)
    {
        OnEvent annotation = method.getAnnotation(OnEvent.class);

        final MethodDescription description = method.getDescription();

        String methodName = description.methodName;

        String eventType = extractEventType(methodName, annotation);
        String componentId = extractComponentId(methodName, annotation);

        String[] parameterTypes = description.argumentTypes;

        if (parameterTypes.length == 0)
            return new BaseEventHandlerMethodInvoker(method, eventType, componentId);

        final List<EventHandlerMethodParameterProvider> providers = CollectionFactory.newList();

        // I'd refactor a bit more of this if Java had covariant return types.

        int contextIndex = 0;

        for (int i = 0; i < parameterTypes.length; i++)
        {
            String type = parameterTypes[i];

            EventHandlerMethodParameterProvider provider = parameterTypeToProvider.get(type);

            if (provider != null)
            {
                providers.add(provider);
                continue;
            }

            RequestParameter parameterAnnotation = method.getParameters().get(i).getAnnotation(RequestParameter.class);

            if (parameterAnnotation != null)
            {
                String parameterName = parameterAnnotation.value();

                providers.add(createQueryParameterSource(componentClassName, description, i, parameterName, type,
                        parameterAnnotation.allowBlank()));
                continue;
            }

            // Note: probably safe to do the conversion to Class early (class load time)
            // as parameters are rarely (if ever) component classes.

            final int parameterIndex = contextIndex++;

            providers.add(createEventContextSource(type, parameterIndex));
        }

        return createInvoker(method, eventType, componentId, contextIndex, providers);
    }

    private EventHandlerMethodParameterProvider createQueryParameterSource(final String componentClassName,
            final MethodDescription description, final int parameterIndex, final String parameterName,
            final String parameterTypeName, final boolean allowBlank)
    {
        return new EventHandlerMethodParameterProvider()
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
                                String.format(
                                        "Query parameter '%s' evaluates to null, but the event method parameter is type %s, a primitive.",
                                        parameterName, parameterType.getName()));

                    return value;
                }
                catch (Exception ex)
                {
                    throw new RuntimeException(
                            String.format(
                                    "Unable process query parameter '%s' as parameter #%d of event handler method %s (in class %s): %s",
                                    parameterName, parameterIndex + 1, description, componentClassName,
                                    InternalUtils.toMessage(ex)), ex);
                }
            }
        };
    }

    private EventHandlerMethodInvoker createInvoker(PlasticMethod method, String eventType, String componentId,
            final int minContextCount, final List<EventHandlerMethodParameterProvider> providers)
    {
        return new BaseEventHandlerMethodInvoker(method, eventType, componentId)
        {
            final int count = providers.size();

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
                    parameters[i] = providers.get(i).valueForEventHandlerMethodParameter(event);
                }

                return parameters;
            }
        };
    }

    private EventHandlerMethodParameterProvider createEventContextSource(final String type, final int parameterIndex)
    {
        return new EventHandlerMethodParameterProvider()
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
