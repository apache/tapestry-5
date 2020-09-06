// Copyright 2006-2013 The Apache Software Foundation
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

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.bindings.LiteralBinding;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 * Responsible for identifying parameters via the {@link org.apache.tapestry5.annotations.Parameter} annotation on
 * component fields. This is one of the most complex of the transformations.
 */
public class ParameterWorker implements ComponentClassTransformWorker2
{
    private final Logger logger = LoggerFactory.getLogger(ParameterWorker.class);

    /**
     * Contains the per-thread state about a parameter, as stored (using
     * a unique key) in the {@link PerthreadManager}. Externalizing such state
     * is part of Tapestry 5.2's pool-less pages.
     */
    private final class ParameterState
    {
        boolean cached;

        Object value;

        void reset(Object defaultValue)
        {
            cached = false;
            value = defaultValue;
        }
    }

    private final ComponentClassCache classCache;

    private final BindingSource bindingSource;

    private final ComponentDefaultProvider defaultProvider;

    private final TypeCoercer typeCoercer;

    private final PerthreadManager perThreadManager;

    public ParameterWorker(ComponentClassCache classCache, BindingSource bindingSource,
                           ComponentDefaultProvider defaultProvider, TypeCoercer typeCoercer, PerthreadManager perThreadManager)
    {
        this.classCache = classCache;
        this.bindingSource = bindingSource;
        this.defaultProvider = defaultProvider;
        this.typeCoercer = typeCoercer;
        this.perThreadManager = perThreadManager;
    }

    private final Comparator<PlasticField> byPrincipalThenName = new Comparator<PlasticField>()
    {
        public int compare(PlasticField o1, PlasticField o2)
        {
            boolean principal1 = o1.getAnnotation(Parameter.class).principal();
            boolean principal2 = o2.getAnnotation(Parameter.class).principal();

            if (principal1 == principal2)
            {
                return o1.getName().compareTo(o2.getName());
            }

            return principal1 ? -1 : 1;
        }
    };


    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        Flow<PlasticField> parametersFields = F.flow(plasticClass.getFieldsWithAnnotation(Parameter.class)).sort(byPrincipalThenName);

        for (PlasticField field : parametersFields)
        {
            convertFieldIntoParameter(plasticClass, model, field);
        }
    }

    private void convertFieldIntoParameter(PlasticClass plasticClass, MutableComponentModel model,
                                           PlasticField field)
    {

        Parameter annotation = field.getAnnotation(Parameter.class);

        String fieldType = field.getTypeName();

        String parameterName = getParameterName(field.getName(), annotation.name());

        field.claim(annotation);

        model.addParameter(parameterName, annotation.required(), annotation.allowNull(), annotation.defaultPrefix(),
                annotation.cache());

        MethodHandle defaultMethodHandle = findDefaultMethodHandle(plasticClass, parameterName);

        ComputedValue<FieldConduit<Object>> computedParameterConduit = createComputedParameterConduit(parameterName, fieldType,
                annotation, defaultMethodHandle);

        field.setComputedConduit(computedParameterConduit);
    }


    private MethodHandle findDefaultMethodHandle(PlasticClass plasticClass, String parameterName)
    {
        final String methodName = "default" + parameterName;

        Predicate<PlasticMethod> predicate = new Predicate<PlasticMethod>()
        {
            public boolean accept(PlasticMethod method)
            {
                return method.getDescription().argumentTypes.length == 0
                        && method.getDescription().methodName.equalsIgnoreCase(methodName);
            }
        };

        Flow<PlasticMethod> matches = F.flow(plasticClass.getMethods()).filter(predicate);

        // This will match exactly 0 or 1 (unless the user does something really silly)
        // methods, and if it matches, we know the name of the method.

        return matches.isEmpty() ? null : matches.first().getHandle();
    }

    @SuppressWarnings("all")
    private ComputedValue<FieldConduit<Object>> createComputedParameterConduit(final String parameterName,
                                                                               final String fieldTypeName, final Parameter annotation,
                                                                               final MethodHandle defaultMethodHandle)
    {
        boolean primitive = PlasticUtils.isPrimitive(fieldTypeName);

        final boolean allowNull = annotation.allowNull() && !primitive;

        return new ComputedValue<FieldConduit<Object>>()
        {
            public ParameterConduit get(InstanceContext context)
            {
                final InternalComponentResources icr = context.get(InternalComponentResources.class);

                final Class fieldType = classCache.forName(fieldTypeName);

                final PerThreadValue<ParameterState> stateValue = perThreadManager.createValue();

                // Rely on some code generation in the component to set the default binding from
                // the field, or from a default method.

                return new ParameterConduit()
                {
                    // Default value for parameter, computed *once* at
                    // page load time.

                    private Object defaultValue = classCache.defaultValueForType(fieldTypeName);

                    private Binding parameterBinding;

                    boolean loaded = false;

                    private boolean invariant = false;

                    {
                        // Inform the ComponentResources about the parameter conduit, so it can be
                        // shared with mixins.

                        icr.setParameterConduit(parameterName, this);
                        icr.getPageLifecycleCallbackHub().addPageLoadedCallback(new Runnable()
                        {
                            public void run()
                            {
                                load();
                            }
                        });
                    }

                    private ParameterState getState()
                    {
                        ParameterState state = stateValue.get();

                        if (state == null)
                        {
                            state = new ParameterState();
                            state.value = defaultValue;
                            stateValue.set(state);
                        }

                        return state;
                    }

                    private boolean isLoaded()
                    {
                        return loaded;
                    }

                    public void set(Object instance, InstanceContext context, Object newValue)
                    {
                        ParameterState state = getState();

                        // Assignments before the page is loaded ultimately exist to set the
                        // default value for the field. Often this is from the (original)
                        // constructor method, which is converted to a real method as part of the transformation.

                        if (!loaded)
                        {
                            state.value = newValue;
                            defaultValue = newValue;
                            return;
                        }

                        // This will catch read-only or unbound parameters.

                        writeToBinding(newValue);

                        state.value = newValue;

                        // If caching is enabled for the parameter (the typical case) and the
                        // component is currently rendering, then the result
                        // can be cached in this ParameterConduit (until the component finishes
                        // rendering).

                        state.cached = annotation.cache() && icr.isRendering();
                    }

                    private Object readFromBinding()
                    {
                        Object result;

                        try
                        {
                            Object boundValue = parameterBinding.get();

                            result = typeCoercer.coerce(boundValue, fieldType);
                        } catch (RuntimeException ex)
                        {
                            throw new TapestryException(String.format(
                                    "Failure reading parameter '%s' of component %s: %s", parameterName,
                                    icr.getCompleteId(), ExceptionUtils.toMessage(ex)), parameterBinding, ex);
                        }

                        if (result == null && !allowNull)
                        {
                            throw new TapestryException(
                                    String.format(
                                            "Parameter '%s' of component %s is bound to null. This parameter is not allowed to be null.",
                                            parameterName, icr.getCompleteId()), parameterBinding, null);
                        }

                        return result;
                    }

                    private void writeToBinding(Object newValue)
                    {
                        // An unbound parameter acts like a simple field
                        // with no side effects.

                        if (parameterBinding == null)
                        {
                            return;
                        }

                        try
                        {
                            Object coerced = typeCoercer.coerce(newValue, parameterBinding.getBindingType());

                            parameterBinding.set(coerced);
                        } catch (RuntimeException ex)
                        {
                            throw new TapestryException(String.format(
                                    "Failure writing parameter '%s' of component %s: %s", parameterName,
                                    icr.getCompleteId(), ExceptionUtils.toMessage(ex)), icr, ex);
                        }
                    }

                    public void reset()
                    {
                        if (!invariant)
                        {
                            getState().reset(defaultValue);
                        }
                    }

                    public void load()
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("{} loading parameter {}", icr.getCompleteId(), parameterName);
                        }

                        // If it's bound at this point, that's because of an explicit binding
                        // in the template or @Component annotation.

                        if (!icr.isBound(parameterName))
                        {
                            if (logger.isDebugEnabled())
                            {
                                logger.debug("{} parameter {} not yet bound", icr.getCompleteId(),
                                        parameterName);
                            }

                            // Otherwise, construct a default binding, or use one provided from
                            // the component.

                            Binding binding = getDefaultBindingForParameter();

                            if (logger.isDebugEnabled())
                            {
                                logger.debug("{} parameter {} bound to default {}", icr.getCompleteId(),
                                        parameterName, binding);
                            }

                            if (binding != null)
                            {
                                icr.bindParameter(parameterName, binding);
                            }
                        }

                        parameterBinding = icr.getBinding(parameterName);

                        loaded = true;

                        invariant = parameterBinding != null && parameterBinding.isInvariant();

                        getState().value = defaultValue;
                    }

                    public boolean isBound()
                    {
                        return parameterBinding != null;
                    }

                    public Object get(Object instance, InstanceContext context)
                    {
                        if (!isLoaded())
                        {
                            return defaultValue;
                        }

                        ParameterState state = getState();

                        if (state.cached || !isBound())
                        {
                            return state.value;
                        }

                        // Read the parameter's binding and cast it to the
                        // field's type.

                        Object result = readFromBinding();

                        // If the value is invariant, we can cache it until at least the end of the request (before
                        // 5.2, it would be cached forever in the pooled instance).
                        // Otherwise, we we may want to cache it for the remainder of the component render (if the
                        // component is currently rendering).

                        if (invariant || (annotation.cache() && icr.isRendering()))
                        {
                            state.value = result;
                            state.cached = true;
                        }

                        return result;
                    }

                    private Binding getDefaultBindingForParameter()
                    {
                        if (InternalUtils.isNonBlank(annotation.value()))
                        {
                            return bindingSource.newBinding("default " + parameterName, icr,
                                    annotation.defaultPrefix(), annotation.value());
                        }

                        if (annotation.autoconnect())
                        {
                            return defaultProvider.defaultBinding(parameterName, icr);
                        }

                        // Invoke the default method and install any value or Binding returned there.

                        invokeDefaultMethod();

                        return parameterBinding;
                    }

                    private void invokeDefaultMethod()
                    {
                        if (defaultMethodHandle == null)
                        {
                            return;
                        }

                        if (logger.isDebugEnabled())
                        {
                            logger.debug("{} invoking method {} to obtain default for parameter {}",
                                    icr.getCompleteId(), defaultMethodHandle, parameterName);
                        }

                        MethodInvocationResult result = defaultMethodHandle.invoke(icr.getComponent());

                        result.rethrow();

                        Object defaultValue = result.getReturnValue();

                        if (defaultValue == null)
                        {
                            return;
                        }

                        if (defaultValue instanceof Binding)
                        {
                            parameterBinding = (Binding) defaultValue;
                            return;
                        }

                        parameterBinding = new LiteralBinding(null, "default " + parameterName, defaultValue);
                    }


                };
            }
        };
    }

    private static String getParameterName(String fieldName, String annotatedName)
    {
        if (InternalUtils.isNonBlank(annotatedName))
        {
            return annotatedName;
        }

        return InternalUtils.stripMemberName(fieldName);
    }
}
