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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.DisableStrictChecks;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.PublishEvent;
import org.apache.tapestry5.annotations.RequestBody;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.StaticActivationContextValue;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.corelib.mixins.PublishServerSideEvents;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.RestSupport;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.Condition;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.LocalVariable;
import org.apache.tapestry5.plastic.LocalVariableCallback;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.MethodParameter;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.runtime.Event;
import org.apache.tapestry5.runtime.PageLifecycleListener;
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
    
    private final RestSupport restSupport;

    private final ComponentClassCache classCache;

    private final OperationTracker operationTracker;

    private final InstructionBuilderCallback RETURN_TRUE = new InstructionBuilderCallback()
    {
        public void doBuild(InstructionBuilder builder)
        {
            builder.loadConstant(true).returnResult();
        }
    };

    private final static Predicate<PlasticMethod> IS_EVENT_HANDLER = new Predicate<PlasticMethod>()
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
  };

    class ComponentIdValidator
    {
        final String componentId;

        final String methodIdentifier;

        ComponentIdValidator(String componentId, String methodIdentifier)
        {
            this.componentId = componentId;
            this.methodIdentifier = methodIdentifier;
        }

        void validate(ComponentResources resources)
        {
            try
            {
                resources.getEmbeddedComponent(componentId);
            } catch (UnknownValueException ex)
            {
                throw new TapestryException(String.format("Method %s references component id '%s' which does not exist.",
                        methodIdentifier, componentId), resources.getLocation(), ex);
            }
        }
    }

    class ValidateComponentIds implements MethodAdvice
    {
        final ComponentIdValidator[] validators;

        ValidateComponentIds(ComponentIdValidator[] validators)
        {
            this.validators = validators;
        }

        public void advise(MethodInvocation invocation)
        {
            ComponentResources resources = invocation.getInstanceContext().get(ComponentResources.class);

            for (ComponentIdValidator validator : validators)
            {
                validator.validate(resources);
            }

            invocation.proceed();
        }
    }

    /**
     * Encapsulates information needed to invoke a method as an event handler method, including the logic
     * to construct parameter values, and match the method against the {@link ComponentEvent}.
     */
    class EventHandlerMethod
    {
        final PlasticMethod method;

        final MethodDescription description;

        final String eventType, componentId;

        final EventHandlerMethodParameterSource parameterSource;

        int minContextValues = 0;

        boolean handleActivationEventContext = false;
        
        final String[] staticActivationContextValues;
        
        final PublishEvent publishEvent;

        EventHandlerMethod(PlasticMethod method)
        {
            this.method = method;
            description = method.getDescription();

            parameterSource = buildSource();

            String methodName = method.getDescription().methodName;

            OnEvent onEvent = method.getAnnotation(OnEvent.class);

            eventType = extractEventType(methodName, onEvent);
            componentId = extractComponentId(methodName, onEvent);
            
            publishEvent = method.getAnnotation(PublishEvent.class);
            staticActivationContextValues = extractStaticActivationContextValues(method);
        }
        
        final private Pattern WHITESPACE = Pattern.compile(".*\\s.*");

        private String[] extractStaticActivationContextValues(PlasticMethod method)
        {
            String[] values = null;
            for (int i = 0; i < method.getParameters().size(); i++) 
            {
                MethodParameter parameter = method.getParameters().get(i);
                final StaticActivationContextValue staticValue = parameter.getAnnotation(StaticActivationContextValue.class);
                if (staticValue != null) 
                {
                    if (values == null) 
                    {
                        values = new String[method.getParameters().size()];
                    }
                    String value = staticValue.value();
                    if (value != null && !value.isEmpty() && !WHITESPACE.matcher(value).matches())
                    {
                        values[i] = value;
                    }
                    else 
                    {
                        throw new RuntimeException(String.format("%s has at least one parameter "
                                + "with a @%s annotation with an invalid value (empty string or "
                                + "value containing whitespace)",
                                method.getMethodIdentifier(),
                                StaticActivationContextValue.class.getSimpleName()));
                    }
                }
            }
            return values;
        }

        void buildMatchAndInvocation(InstructionBuilder builder, final LocalVariable resultVariable)
        {
            final PlasticField sourceField =
                    parameterSource == null ? null
                            : method.getPlasticClass().introduceField(EventHandlerMethodParameterSource.class, description.methodName + "$parameterSource").inject(parameterSource);

            final PlasticField staticActivationContextValueField =
                    staticActivationContextValues == null ? null
                            : method.getPlasticClass().introduceField(String[].class, description.methodName + "$staticActivationContextValues").inject(staticActivationContextValues);
            
            builder.loadArgument(0).loadConstant(eventType).loadConstant(componentId).loadConstant(minContextValues);
            if (staticActivationContextValueField != null)
            {
                builder.loadThis().getField(staticActivationContextValueField);
            }
            else
            {
                builder.loadNull();
            }
            
        builder.invoke(ComponentEvent.class, boolean.class, "matches", String.class, String.class, int.class, String[].class);

            builder.when(Condition.NON_ZERO, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadArgument(0).loadConstant(method.getMethodIdentifier()).invoke(Event.class, void.class, "setMethodDescription", String.class);

                    builder.loadThis();

                    int count = description.argumentTypes.length;

                    for (int i = 0; i < count; i++)
                    {
                        builder.loadThis().getField(sourceField).loadArgument(0).loadConstant(i);

                        builder.invoke(EventHandlerMethodParameterSource.class, Object.class, "get",
                                ComponentEvent.class, int.class);

                        builder.castOrUnbox(description.argumentTypes[i]);
                    }

                    builder.invokeVirtual(method);

                    if (!method.isVoid())
                    {
                        builder.boxPrimitive(description.returnType);
                        builder.loadArgument(0).swap();

                        builder.invoke(Event.class, boolean.class, "storeResult", Object.class);

                        // storeResult() returns true if the method is aborted. Return true since, certainly,
                        // a method was invoked.
                        builder.when(Condition.NON_ZERO, RETURN_TRUE);
                    }

                    // Set the result to true, to indicate that some method was invoked.

                    builder.loadConstant(true).storeVariable(resultVariable);
                }
            });
        }


        private EventHandlerMethodParameterSource buildSource()
        {
            final String[] parameterTypes = method.getDescription().argumentTypes;

            if (parameterTypes.length == 0)
            {
                return null;
            }

            final List<EventHandlerMethodParameterProvider> providers = CollectionFactory.newList();

            int contextIndex = 0;
            boolean hasBodyRequestParameters = false;

            for (int i = 0; i < parameterTypes.length; i++)
            {
                String type = parameterTypes[i];

                EventHandlerMethodParameterProvider provider = parameterTypeToProvider.get(type);

                if (provider != null)
                {
                    providers.add(provider);
                    this.handleActivationEventContext = true;
                    continue;
                }

                RequestParameter parameterAnnotation = method.getParameters().get(i).getAnnotation(RequestParameter.class);

                if (parameterAnnotation != null)
                {
                    String parameterName = parameterAnnotation.value();

                    providers.add(createQueryParameterProvider(method, i, parameterName, type,
                            parameterAnnotation.allowBlank()));
                    continue;
                }

                RequestBody bodyAnnotation = method.getParameters().get(i).getAnnotation(RequestBody.class);

                if (bodyAnnotation != null)
                {
                    if (!hasBodyRequestParameters)
                    {
                        providers.add(createRequestBodyProvider(method, i, type,
                                bodyAnnotation.allowEmpty()));
                        hasBodyRequestParameters = true;
                    }
                    else
                    {
                        throw new RuntimeException(
                                String.format("Method %s has more than one @RequestBody parameter", method.getDescription()));
                    }
                    continue;
                }

                // Note: probably safe to do the conversion to Class early (class load time)
                // as parameters are rarely (if ever) component classes.

                providers.add(createEventContextProvider(type, contextIndex++));
            }


            minContextValues = contextIndex;

            EventHandlerMethodParameterProvider[] providerArray = providers.toArray(new EventHandlerMethodParameterProvider[providers.size()]);

            return new EventHandlerMethodParameterSource(method.getMethodIdentifier(), operationTracker, providerArray);
        }
    }


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

    public OnEventWorker(Request request, ValueEncoderSource valueEncoderSource, ComponentClassCache classCache, OperationTracker operationTracker, RestSupport restSupport)
    {
        this.request = request;
        this.valueEncoderSource = valueEncoderSource;
        this.classCache = classCache;
        this.operationTracker = operationTracker;
        this.restSupport = restSupport;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        Flow<PlasticMethod> methods = matchEventHandlerMethods(plasticClass);

        if (methods.isEmpty())
        {
            return;
        }

        addEventHandlingLogic(plasticClass, support.isRootTransformation(), methods, model);
    }

    private static final Set<String> HTTP_EVENT_HANDLER_NAMES = InternalConstants.SUPPORTED_HTTP_METHOD_EVENT_HANDLER_METHOD_NAMES;
    
    private static final Set<String> HTTP_METHOD_EVENTS = InternalConstants.SUPPORTED_HTTP_METHOD_EVENTS;

    private void addEventHandlingLogic(final PlasticClass plasticClass, final boolean isRoot, final Flow<PlasticMethod> plasticMethods, final MutableComponentModel model)
    {
        Flow<EventHandlerMethod> eventHandlerMethods = plasticMethods.map(new Mapper<PlasticMethod, EventHandlerMethod>()
        {
            public EventHandlerMethod map(PlasticMethod element)
            {
                return new EventHandlerMethod(element);
            }
        });

        implementDispatchMethod(plasticClass, isRoot, model, eventHandlerMethods);

        addComponentIdValidationLogicOnPageLoad(plasticClass, eventHandlerMethods);
        
        addPublishEventInfo(eventHandlerMethods, model);
    }

    private void addPublishEventInfo(Flow<EventHandlerMethod> eventHandlerMethods,
            MutableComponentModel model)
    {
        JSONArray publishEvents = new JSONArray();
        for (EventHandlerMethod eventHandlerMethod : eventHandlerMethods)
        {
            if (eventHandlerMethod.publishEvent != null)
            {
                publishEvents.add(eventHandlerMethod.eventType.toLowerCase());
            }
        }
        
        // If we do have events to publish, we apply the mixin and pass
        // event information to it.
        if (publishEvents.size() > 0) {
            model.addMixinClassName(PublishServerSideEvents.class.getName(), "after:*");
            model.setMeta(InternalConstants.PUBLISH_COMPONENT_EVENTS_META, publishEvents.toString());
        }
    }

	private void addComponentIdValidationLogicOnPageLoad(PlasticClass plasticClass, Flow<EventHandlerMethod> eventHandlerMethods)
    {
        ComponentIdValidator[] validators = extractComponentIdValidators(eventHandlerMethods);

        if (validators.length > 0)
        {
            plasticClass.introduceInterface(PageLifecycleListener.class);
            plasticClass.introduceMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_DESCRIPTION).addAdvice(new ValidateComponentIds(validators));
        }
    }

    private ComponentIdValidator[] extractComponentIdValidators(Flow<EventHandlerMethod> eventHandlerMethods)
    {
        return eventHandlerMethods.map(new Mapper<EventHandlerMethod, ComponentIdValidator>()
        {
            public ComponentIdValidator map(EventHandlerMethod element)
            {
                if (element.componentId.equals(""))
                {
                    return null;
                }
                if (element.method.getAnnotation(DisableStrictChecks.class) != null)
                {
                    return null;
                }

                return new ComponentIdValidator(element.componentId, element.method.getMethodIdentifier());
            }
        }).removeNulls().toArray(ComponentIdValidator.class);
    }

    private void implementDispatchMethod(final PlasticClass plasticClass, final boolean isRoot, final MutableComponentModel model, final Flow<EventHandlerMethod> eventHandlerMethods)
    {
        plasticClass.introduceMethod(TransformConstants.DISPATCH_COMPONENT_EVENT_DESCRIPTION).changeImplementation(new InstructionBuilderCallback()
        {
            public void doBuild(InstructionBuilder builder)
            {
                builder.startVariable("boolean", new LocalVariableCallback()
                {
                    public void doBuild(LocalVariable resultVariable, InstructionBuilder builder)
                    {
                        if (!isRoot)
                        {
                            // As a subclass, there will be a base class implementation (possibly empty).

                            builder.loadThis().loadArguments().invokeSpecial(plasticClass.getSuperClassName(), TransformConstants.DISPATCH_COMPONENT_EVENT_DESCRIPTION);

                            // First store the result of the super() call into the variable.
                            builder.storeVariable(resultVariable);
                            builder.loadArgument(0).invoke(Event.class, boolean.class, "isAborted");
                            builder.when(Condition.NON_ZERO, RETURN_TRUE);
                        } else
                        {
                            // No event handler method has yet been invoked.
                            builder.loadConstant(false).storeVariable(resultVariable);
                        }

                        boolean hasRestEndpointEventHandlerMethod = false;
                        JSONArray restEndpointEventHandlerMethods = null;
                        for (EventHandlerMethod method : eventHandlerMethods)
                        {
                            method.buildMatchAndInvocation(builder, resultVariable);

                            model.addEventHandler(method.eventType);

                            if (method.handleActivationEventContext)
                            {
                                model.doHandleActivationEventContext();
                            }

                            // We're collecting this info for all components, even considering REST
                            // events are only triggered in pages, because we can have REST event
                            // handler methods in base classes too, and we need this info
                            // for generating complete, correct OpenAPI documentation.
                            final OnEvent onEvent = method.method.getAnnotation(OnEvent.class);
                            final String methodName = method.method.getDescription().methodName;
                            if (isRestEndpointEventHandlerMethod(onEvent, methodName))
                            {
                                hasRestEndpointEventHandlerMethod = true;
                                if (restEndpointEventHandlerMethods == null)
                                {
                                    restEndpointEventHandlerMethods = new JSONArray();
                                }
                                JSONObject methodMeta = new JSONObject();
                                methodMeta.put("name", methodName);
                                JSONArray parameters = new JSONArray();
                                for (MethodParameter parameter : method.method.getParameters())
                                {
                                    parameters.add(parameter.getType());
                                }
                                methodMeta.put("parameters", parameters);
                                restEndpointEventHandlerMethods.add(methodMeta);
                            }
                        }
                        
                        // This meta property is only ever checked in pages, so we avoid using more
                        // memory by not setting it to all component models.
                        if (model.isPage())
                        {
                            model.setMeta(InternalConstants.REST_ENDPOINT_EVENT_HANDLER_METHOD_PRESENT, 
                                    hasRestEndpointEventHandlerMethod ? InternalConstants.TRUE : InternalConstants.FALSE);
                        }
                        
                        // See comment on the top of isRestEndpointEventHandlerMethod() above.
                        // This shouldn't waste memory unless there are REST event handler
                        // methods in components, something that would be ignored anyway.
                        if (restEndpointEventHandlerMethods != null)
                        {
                            model.setMeta(InternalConstants.REST_ENDPOINT_EVENT_HANDLER_METHODS, 
                                    restEndpointEventHandlerMethods.toCompactString());
                        }

                        builder.loadVariable(resultVariable).returnResult();
                    }

                });
            }
        });
    }

    private Flow<PlasticMethod> matchEventHandlerMethods(PlasticClass plasticClass)
    {
        return F.flow(plasticClass.getMethods()).filter(IS_EVENT_HANDLER);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private EventHandlerMethodParameterProvider createRequestBodyProvider(PlasticMethod method, final int parameterIndex, 
            final String parameterTypeName, final boolean allowEmpty)
    {
        final String methodIdentifier = method.getMethodIdentifier();
        return (event) -> {
            Invokable<Object> operation = () -> {
                Class parameterType = classCache.forName(parameterTypeName);
                Optional result = restSupport.getRequestBodyAs(parameterType);
                if (!allowEmpty && !result.isPresent())
                {
                    throw new RuntimeException(
                            String.format("The request has an empty body and %s has one parameter with @RequestBody(allowEmpty=false)", methodIdentifier));
                }
                return result.orElse(null);
            };
            return operationTracker.invoke(
                    "Converting HTTP request body for @RequestBody parameter", 
                    operation);
        };
    }

    private EventHandlerMethodParameterProvider createQueryParameterProvider(PlasticMethod method, final int parameterIndex, final String parameterName,
                                                                             final String parameterTypeName, final boolean allowBlank)
    {
        final String methodIdentifier = method.getMethodIdentifier();

        return new EventHandlerMethodParameterProvider()
        {
            @SuppressWarnings("unchecked")
            public Object valueForEventHandlerMethodParameter(ComponentEvent event)
            {
                try
                {

                    Class parameterType = classCache.forName(parameterTypeName);
                    boolean isArray = parameterType.isArray();

                    if (isArray)
                    {
                        parameterType = parameterType.getComponentType();
                    }

                    ValueEncoder valueEncoder = valueEncoderSource.getValueEncoder(parameterType);

                    String parameterValue = request.getParameter(parameterName);

                    if (!allowBlank && InternalUtils.isBlank(parameterValue))
                        throw new RuntimeException(String.format(
                                "The value for query parameter '%s' was blank, but a non-blank value is needed.",
                                parameterName));

                    Object value;

                    if (!isArray)
                    {
                        value = coerce(parameterName, parameterType, parameterValue, valueEncoder, allowBlank);
                    } else
                    {
                        String[] parameterValues = request.getParameters(parameterName);
                        Object[] array = (Object[]) Array.newInstance(parameterType, parameterValues.length);
                        for (int i = 0; i < parameterValues.length; i++)
                        {
                            array[i] = coerce(parameterName, parameterType, parameterValues[i], valueEncoder, allowBlank);
                        }
                        value = array;
                    }

                    return value;
                } catch (Exception ex)
                {
                    throw new RuntimeException(
                            String.format(
                                    "Unable process query parameter '%s' as parameter #%d of event handler method %s: %s",
                                    parameterName, parameterIndex + 1, methodIdentifier,
                                    ExceptionUtils.toMessage(ex)), ex);
                }
            }

            private Object coerce(final String parameterName, Class parameterType,
                                  String parameterValue, ValueEncoder valueEncoder, boolean allowBlank)
            {

                if (!allowBlank && InternalUtils.isBlank(parameterValue))
                {
                    throw new RuntimeException(String.format(
                            "The value for query parameter '%s' was blank, but a non-blank value is needed.",
                            parameterName));
                }

                Object value = valueEncoder.toValue(parameterValue);

                if (parameterType.isPrimitive() && value == null)
                    throw new RuntimeException(
                            String.format(
                                    "Query parameter '%s' evaluates to null, but the event method parameter is type %s, a primitive.",
                                    parameterName, parameterType.getName()));
                return value;
            }
        };
    }
    
    private EventHandlerMethodParameterProvider createEventContextProvider(final String type, final int parameterIndex)
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
    
    /**
     * Tells whether a method with a given name and possibly {@link OnEvent} annotation
     * is a REST endpoint event handler method or not.
     */
    public static boolean isRestEndpointEventHandlerMethod(final OnEvent onEvent, final String methodName) {
        return onEvent != null && HTTP_METHOD_EVENTS.contains(onEvent.value().toLowerCase())
            || HTTP_EVENT_HANDLER_NAMES.contains(methodName.toLowerCase());
    }

}
