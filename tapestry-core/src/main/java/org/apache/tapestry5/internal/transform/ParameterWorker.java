// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.bindings.LiteralBinding;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.ioc.util.func.Predicate;
import org.apache.tapestry5.ioc.util.func.Predicate;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for identifying parameters via the {@link org.apache.tapestry5.annotations.Parameter} annotation on
 * component fields. This is one of the most complex of the transformations.
 */
public class ParameterWorker implements ComponentClassTransformWorker
{
    private final Logger logger = LoggerFactory.getLogger(ParameterWorker.class);

    private final class InvokeResetOnParameterConduit implements ComponentMethodAdvice
    {
        private final FieldAccess conduitAccess;

        private InvokeResetOnParameterConduit(FieldAccess conduitAccess)
        {
            this.conduitAccess = conduitAccess;
        }

        public void advise(ComponentMethodInvocation invocation)
        {
            getConduit(invocation, conduitAccess).reset();

            invocation.proceed();
        }
    }

    private final class InvokeParameterDefaultMethod implements ComponentMethodAdvice
    {
        private final FieldAccess conduitAccess;

        private final MethodAccess defaultMethodAccess;

        private InvokeParameterDefaultMethod(FieldAccess conduitAccess, MethodAccess defaultMethodAccess)
        {
            this.conduitAccess = conduitAccess;
            this.defaultMethodAccess = defaultMethodAccess;
        }

        public void advise(ComponentMethodInvocation invocation)
        {
            logger.debug(String.format("%s invoking default method %s", invocation.getComponentResources()
                    .getCompleteId(), defaultMethodAccess));

            MethodInvocationResult result = defaultMethodAccess.invoke(invocation.getInstance());

            result.rethrow();

            getConduit(invocation, conduitAccess).setDefault(result.getReturnValue());

            invocation.proceed();
        }
    }

    private final class InvokeLoadOnParmeterConduit implements ComponentMethodAdvice
    {
        private final FieldAccess conduitAccess;

        private InvokeLoadOnParmeterConduit(FieldAccess conduitAccess)
        {
            this.conduitAccess = conduitAccess;
        }

        public void advise(ComponentMethodInvocation invocation)
        {
            getConduit(invocation, conduitAccess).load();

            invocation.proceed();
        }
    }

    private final ComponentClassCache classCache;

    private final BindingSource bindingSource;

    private final ComponentDefaultProvider defaultProvider;

    private final TypeCoercer typeCoercer;

    public ParameterWorker(ComponentClassCache classCache, BindingSource bindingSource,
            ComponentDefaultProvider defaultProvider, TypeCoercer typeCoercer)
    {
        this.classCache = classCache;
        this.bindingSource = bindingSource;
        this.defaultProvider = defaultProvider;
        this.typeCoercer = typeCoercer;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        transformFields(transformation, model, true);
        transformFields(transformation, model, false);
    }

    private void transformFields(ClassTransformation transformation, MutableComponentModel model, boolean principal)
    {
        for (TransformField field : matchParameterFields(transformation, principal))
        {
            convertFieldIntoParameter(transformation, model, field);
        }
    }

    private List<TransformField> matchParameterFields(ClassTransformation transformation, final boolean principal)
    {
        Predicate<TransformField> predicate = new Predicate<TransformField>()
        {
            public boolean accept(TransformField field)
            {
                Parameter annotation = field.getAnnotation(Parameter.class);

                return annotation != null && annotation.principal() == principal;
            }
        };

        return transformation.matchFields(predicate);
    }

    private void convertFieldIntoParameter(ClassTransformation transformation, MutableComponentModel model,
            TransformField field)
    {
        Parameter annotation = field.getAnnotation(Parameter.class);

        String fieldType = field.getType();

        String parameterName = getParameterName(field.getName(), annotation.name());

        field.claim(annotation);

        model.addParameter(parameterName, annotation.required(), annotation.allowNull(), annotation.defaultPrefix(),
                annotation.cache());

        ComponentValueProvider<ParameterConduit> provider = createParameterConduitProvider(parameterName, fieldType,
                annotation);

        TransformField conduitField = transformation.addIndirectInjectedField(ParameterConduit.class, parameterName
                + "$conduit", provider);

        FieldAccess conduitAccess = conduitField.getAccess();

        addCodeForParameterDefaultMethod(transformation, parameterName, conduitAccess);

        field.replaceAccess(conduitField);

        invokeLoadOnParameterConduitAtPageLoad(transformation, conduitAccess);

        invokeResetOnParameterConduitAtPostRenderCleanup(transformation, conduitAccess);
    }

    private void invokeResetOnParameterConduitAtPostRenderCleanup(ClassTransformation transformation,
            final FieldAccess conduitAccess)
    {
        ComponentMethodAdvice advice = new InvokeResetOnParameterConduit(conduitAccess);

        addMethodAdvice(transformation, TransformConstants.POST_RENDER_CLEANUP_SIGNATURE, advice);
    }

    private void addMethodAdvice(ClassTransformation transformation, TransformMethodSignature methodSignature,
            ComponentMethodAdvice advice)
    {
        transformation.getOrCreateMethod(methodSignature).addAdvice(advice);
    }

    private void invokeLoadOnParameterConduitAtPageLoad(ClassTransformation transformation, FieldAccess conduitAccess)
    {
        ComponentMethodAdvice pageLoadAdvice = new InvokeLoadOnParmeterConduit(conduitAccess);

        addPageLoadAdvice(transformation, pageLoadAdvice);
    }

    private ComponentValueProvider<ParameterConduit> createParameterConduitProvider(final String parameterName,
            final String fieldTypeName, final Parameter annotation)
    {
        return new ComponentValueProvider<ParameterConduit>()
        {
            public ParameterConduit get(ComponentResources resources)
            {
                final InternalComponentResources icr = (InternalComponentResources) resources;

                final Class fieldType = classCache.forName(fieldTypeName);

                // Rely on some code generation in the component to set the default binding from
                // the field, or from a default method.

                return new ParameterConduit()
                {
                    // Current cached value for the parameter.
                    private Object value;

                    // Default value for parameter, computed *once* at
                    // page load time.

                    private Object defaultValue = classCache.defaultValueForType(fieldTypeName);

                    private Binding parameterBinding;

                    boolean loaded = false;

                    private boolean invariant = false;

                    // Is the current value of the binding cached in the
                    // value field?
                    private boolean cached = false;

                    {
                        // Inform the ComponentResources about the parameter conduit, so it can be
                        // shared with mixins.

                        icr.setParameterConduit(parameterName, this);
                    }

                    private boolean isLoaded()
                    {
                        return loaded;
                    }

                    public void set(Object newValue)
                    {
                        // Assignments before the page is loaded ultimately exist to set the
                        // default value for the field. Often this is from the (original)
                        // constructor method, which is converted to a real method as part of the transformation.

                        if (!loaded)
                        {
                            value = newValue;
                            defaultValue = newValue;
                            return;
                        }

                        // This will catch read-only or unbound parameters.

                        writeToBinding(newValue);

                        value = newValue;

                        // If caching is enabled for the parameter (the typical case) and the
                        // component is currently rendering, then the result
                        // can be cached in this ParameterConduit (until the component finishes
                        // rendering).

                        cached = annotation.cache() && icr.isRendering();
                    }

                    private Object readFromBinding()
                    {
                        Object result = null;

                        try
                        {
                            Object boundValue = parameterBinding.get();

                            result = typeCoercer.coerce(boundValue, fieldType);
                        }
                        catch (RuntimeException ex)
                        {
                            throw new TapestryException(String.format(
                                    "Failure reading parameter '%s' of component %s: %s", parameterName, icr
                                            .getCompleteId(), InternalUtils.toMessage(ex)), parameterBinding, ex);
                        }

                        if (result != null || annotation.allowNull())
                            return result;

                        throw new TapestryException(
                                String
                                        .format(
                                                "Parameter '%s' of component %s is bound to null. This parameter is not allowed to be null.",
                                                parameterName, icr.getCompleteId()), parameterBinding, null);
                    }

                    @SuppressWarnings("unchecked")
                    private void writeToBinding(Object newValue)
                    {
                        // An unbound parameter acts like a simple field
                        // with no side effects.

                        if (parameterBinding == null)
                            return;

                        try
                        {
                            Object coerced = typeCoercer.coerce(newValue, parameterBinding.getBindingType());

                            parameterBinding.set(coerced);
                        }
                        catch (RuntimeException ex)
                        {
                            throw new TapestryException(String.format(
                                    "Failure writing parameter '%s' of component %s: %s", parameterName, icr
                                            .getCompleteId(), InternalUtils.toMessage(ex)), icr, ex);
                        }
                    }

                    public void reset()
                    {
                        if (!invariant)
                        {
                            value = defaultValue;
                            cached = false;
                        }
                    }

                    public void load()
                    {
                        logger.debug(String.format("%s loading parameter %s", icr.getCompleteId(), parameterName));

                        // If it's bound at this point, that's because of an explicit binding
                        // in the template or @Component annotation.

                        if (!icr.isBound(parameterName))
                        {
                            logger.debug(String.format("%s parameter %s not yet bound", icr.getCompleteId(),
                                    parameterName));

                            // Otherwise, construct a default binding, or use one provided from
                            // the component.

                            Binding binding = getDefaultBindingForParameter();

                            logger.debug(String.format("%s parameter %s bound to default %s", icr.getCompleteId(),
                                    parameterName, binding));

                            if (binding != null)
                                icr.bindParameter(parameterName, binding);
                        }

                        parameterBinding = icr.getBinding(parameterName);

                        loaded = true;

                        invariant = parameterBinding != null && parameterBinding.isInvariant();

                        value = defaultValue;
                    }

                    public boolean isBound()
                    {
                        return parameterBinding != null;
                    }

                    @SuppressWarnings("unchecked")
                    public Object get()
                    {
                        if (!isLoaded()) { return defaultValue; }

                        if (cached || !isBound()) { return value; }

                        // Read the parameter's binding and cast it to the
                        // field's type.

                        Object result = readFromBinding();

                        // If the value is invariant, we can cache it forever. Otherwise, we
                        // we may want to cache it for the remainder of the component render (if the
                        // component is currently rendering).

                        if (invariant || (annotation.cache() && icr.isRendering()))
                        {
                            value = result;
                            cached = true;
                        }

                        return result;
                    }

                    private Binding getDefaultBindingForParameter()
                    {
                        if (InternalUtils.isNonBlank(annotation.value()))
                            return bindingSource.newBinding("default " + parameterName, icr,
                                    annotation.defaultPrefix(), annotation.value());

                        if (annotation.autoconnect())
                            return defaultProvider.defaultBinding(parameterName, icr);

                        // Return (if not null) the binding from the setDefault() method which is
                        // set via a default method on the component, or from the field's initial
                        // value.

                        return parameterBinding;
                    }

                    public void setDefault(Object value)
                    {
                        if (value == null)
                            return;

                        if (value instanceof Binding)
                        {
                            parameterBinding = (Binding) value;
                            return;
                        }

                        parameterBinding = new LiteralBinding(null, "default " + parameterName, value);
                    }
                };
            }
        };
    }

    private ParameterConduit getConduit(ComponentMethodInvocation invocation, FieldAccess access)
    {
        return (ParameterConduit) access.read(invocation.getInstance());
    }

    private void addCodeForParameterDefaultMethod(ClassTransformation transformation, final String parameterName,
            final FieldAccess conduitAccess)
    {
        final String methodName = "default" + parameterName;

        Predicate<TransformMethod> predicate = new Predicate<TransformMethod>()
        {
            public boolean accept(TransformMethod method)
            {
                return method.getSignature().getParameterTypes().length == 0
                        && method.getName().equalsIgnoreCase(methodName);
            }
        };

        List<TransformMethod> matches = transformation.matchMethods(predicate);

        // This will match exactly 0 or 1 (unless the user does something really silly)
        // methods, and if it matches, we know the name of the method.

        if (matches.isEmpty())
            return;

        TransformMethod defaultMethod = matches.get(0);

        captureDefaultValueFromDefaultMethod(transformation, defaultMethod, conduitAccess);
    }

    private void captureDefaultValueFromDefaultMethod(ClassTransformation transformation,
            TransformMethod defaultMethod, final FieldAccess conduitAccess)
    {
        final MethodAccess access = defaultMethod.getAccess();

        ComponentMethodAdvice advice = new InvokeParameterDefaultMethod(conduitAccess, access);

        addPageLoadAdvice(transformation, advice);
    }

    private void addPageLoadAdvice(ClassTransformation transformation, ComponentMethodAdvice advice)
    {
        addMethodAdvice(transformation, TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, advice);
    }

    private static String getParameterName(String fieldName, String annotatedName)
    {
        if (InternalUtils.isNonBlank(annotatedName))
            return annotatedName;

        return InternalUtils.stripMemberName(fieldName);
    }
}
