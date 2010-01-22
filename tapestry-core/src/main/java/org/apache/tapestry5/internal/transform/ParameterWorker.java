// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import java.util.Iterator;
import java.util.List;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.ParameterAccess;
import org.apache.tapestry5.internal.bindings.LiteralBinding;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.MethodFilter;
import org.apache.tapestry5.services.TransformConstants;
import org.apache.tapestry5.services.TransformMethodSignature;

/**
 * Responsible for identifying parameters via the {@link org.apache.tapestry5.annotations.Parameter} annotation on
 * component fields. This is one of the most complex of the transformations.
 */
public class ParameterWorker implements ComponentClassTransformWorker
{
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

    @Override
    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> fieldNames = transformation.findFieldsWithAnnotation(Parameter.class);

        for (int pass = 0; pass < 2; pass++)
        {
            Iterator<String> i = fieldNames.iterator();

            while (i.hasNext())
            {
                String fieldName = i.next();

                Parameter annotation = transformation
                        .getFieldAnnotation(fieldName, Parameter.class);

                // Process the principal annotations on the first pass, handle the others
                // on the second pass.

                boolean process = pass == 0 ? annotation.principal() : true;

                if (process)
                {
                    convertFieldIntoParameter(fieldName, annotation, transformation, model);

                    i.remove();
                }
            }
        }

    }

    private void convertFieldIntoParameter(String fieldName, final Parameter annotation,
            ClassTransformation transformation, MutableComponentModel model)
    {
        final String fieldTypeName = transformation.getFieldType(fieldName);

        final String parameterName = getParameterName(fieldName, annotation.name());

        final boolean enableCaching = annotation.cache();

        model.addParameter(parameterName, annotation.required(), annotation.allowNull(), annotation
                .defaultPrefix(), enableCaching);

        transformation.claimField(fieldName, annotation);
        
        ComponentValueProvider<ParameterConduit> provider = new ComponentValueProvider<ParameterConduit>()
        {
            // Invoked from the components' constructor. This causes a few issues (it would be
            // better
            // if there was a way to defer until the component's page loaded lifecycle method). The
            // issues
            // are addressed by deferring some behaviors until the load() method.

            @Override
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

                    private Object defaultValue;

                    private ParameterAccess parameterAccess;

                    private Binding defaultBinding;

                    boolean loaded = false;

                    // Is the current value of the binding cached in the
                    // value field?
                    private boolean cached = false;

                    // If the field is a primitive type, set its default value to false
                    // or zero. For non-primitives, null until we know better.

                    {
                        Class javaType = classCache.forName(fieldTypeName);

                        if (javaType.isPrimitive())
                        {
                            // Reminder: 0 coerces to false
                            defaultValue = typeCoercer.coerce(0l, javaType);
                        }

                        icr.setParameterConduit(parameterName, this);
                    }

                    private boolean isInvariant()
                    {
                        return parameterAccess.isInvariant();
                    }

                    private boolean isLoaded()
                    {
                        return loaded;
                    }

                    @Override
                    public void set(Object newValue)
                    {
                        // Assignments before the page is loaded ultimately exist to set the
                        // default value for the field. Often this is from the (original)
                        // constructor method,
                        // which is converted to a real method as part of the transformation.

                        if (!loaded)
                        {
                            defaultValue = newValue;
                            return;
                        }

                        // This will catch read-only or unbound parameters.

                        parameterAccess.write(newValue);

                        value = newValue;

                        // If caching is enabled for the parameter (the typical case) and the
                        // component is currently rendering, then the result
                        // can be cached in the ParameterConduit (until the component finishes
                        // rendering).

                        cached = enableCaching && icr.isRendering();
                    }

                    @Override
                    public void reset()
                    {
                        if (!isInvariant())
                        {
                            value = defaultValue;
                            cached = false;
                        }
                    }

                    @Override
                    public void load()
                    {
                        // If it's bound at this point, that's because of an explicit binding
                        // in the template or @Component annotation.

                        if (!icr.isBound(parameterName))
                        {
                            // Otherwise, construct a default binding, or use one provided from
                            // the component.

                            Binding binding = getDefaultBindingForParameter();

                            if (binding != null)
                                icr.bindParameter(parameterName, binding);

                            defaultBinding = null;
                        }

                        parameterAccess = icr.getParameterAccess(parameterName);

                        loaded = true;

                        value = defaultValue;
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

                        return defaultBinding;
                    }

                    @Override
                    public boolean isBound()
                    {
                        return parameterAccess.isBound();
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public Object get()
                    {
                        if (!isLoaded()) { return defaultValue; }

                        if (cached || !isBound()) { return value; }

                        // Read the parameter's binding and cast it to the
                        // field's type.
                        Object result = parameterAccess.read(fieldType);

                        // If the value is invariant, we can cache it forever. Otherwise, we
                        // we may want to cache it for the remainder of the component render (if the
                        // component is currently rendering).

                        if (isInvariant() || (enableCaching && icr.isRendering()))
                        {
                            value = result;
                            cached = true;
                        }

                        return result;
                    }

                    @Override
                    public void setDefault(Object value)
                    {
                        if (value == null)
                            return;

                        if (value instanceof Binding)
                        {
                            defaultBinding = (Binding) value;
                            return;
                        }

                        defaultBinding = new LiteralBinding(null, "default " + parameterName, value);
                    }
                };
            }

        };

        // This has to be done in the constructor, to handle any field initializations

        String conduitFieldName = transformation.addIndirectInjectedField(ParameterConduit.class,
                parameterName + "$conduit", provider);

        addCodeForParameterDefaultMethod(transformation, parameterName, conduitFieldName);

        transformation.replaceFieldAccess(fieldName, conduitFieldName);

        transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, String
                .format("%s.load();", conduitFieldName));

        transformation.extendMethod(TransformConstants.POST_RENDER_CLEANUP_SIGNATURE, String
                .format("%s.reset();", conduitFieldName));

    }

    private void addCodeForParameterDefaultMethod(ClassTransformation transformation,
            final String parameterName, String conduitFieldName)
    {
        final String methodName = "default" + parameterName;

        MethodFilter filter = new MethodFilter()
        {
            public boolean accept(TransformMethodSignature signature)
            {
                return signature.getParameterTypes().length == 0
                        && signature.getMethodName().equalsIgnoreCase(methodName);
            }
        };

        // This will match exactly 0 or 1 methods, and if it matches, we know the name
        // of the method.

        List<TransformMethodSignature> signatures = transformation.findMethods(filter);

        if (signatures.isEmpty())
            return;

        String actualMethodName = signatures.get(0).getMethodName();

        transformation.extendExistingMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE,
                String.format("%s.setDefault(($w) %s());", conduitFieldName, actualMethodName));
    }

    private static String getParameterName(String fieldName, String annotatedName)
    {
        if (InternalUtils.isNonBlank(annotatedName))
            return annotatedName;

        return InternalUtils.stripMemberName(fieldName);
    }
}
