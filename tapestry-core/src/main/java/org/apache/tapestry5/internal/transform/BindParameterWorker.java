// Copyright 2009, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.BindParameter;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentValueProvider;

/**
 * Responsible for identifying, via the {@link org.apache.tapestry5.annotations.BindParameter} annotation, mixin fields
 * that should be bound to a core-component parameter value.
 * 
 * @since 5.2.0
 */
public class BindParameterWorker implements ComponentClassTransformWorker
{
    private final TypeCoercer typeCoercer;

    private final ComponentClassCache componentClassCache;

    public BindParameterWorker(TypeCoercer typeCoercer, ComponentClassCache componentClassCache)
    {
        this.typeCoercer = typeCoercer;
        this.componentClassCache = componentClassCache;
    }

    public void transform(final ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> fieldNames = transformation.findFieldsWithAnnotation(BindParameter.class);

        for (String fieldName : fieldNames)
        {
            BindParameter annotation = transformation.getFieldAnnotation(fieldName,
                    BindParameter.class);

            convertFieldIntoContainerBoundParameter(fieldName, annotation, transformation);
        }

    }

    private void convertFieldIntoContainerBoundParameter(final String fieldName,
            final BindParameter annotation, ClassTransformation transformation)
    {
        final String fieldTypeName = transformation.getFieldType(fieldName);

        transformation.claimField(fieldName, annotation);

        ComponentValueProvider<FieldValueConduit> provider = new ComponentValueProvider<FieldValueConduit>()
        {

            public FieldValueConduit get(final ComponentResources resources)
            {
                if (!resources.isMixin())
                    throw new TapestryException(TransformMessages.bindParameterOnlyOnMixin(
                            fieldName, resources), null);

                final InternalComponentResources containerResources = (InternalComponentResources) resources
                        .getContainerResources();

                // Evaluate this early so that we get a fast fail.

                final String containerParameterName = identifyParameterName(resources,
                        InternalUtils.stripMemberName(fieldName), annotation.value());

                final Class fieldType = componentClassCache.forName(fieldTypeName);

                return new FieldValueConduit()
                {
                    private ParameterConduit conduit;

                    /**
                     * Defer obtaining the conduit object until needed, to deal with the complex
                     * lifecycle of
                     * parameters. Perhaps this can be addressed by converting constructors into
                     * methods invoked
                     * from the page loaded lifecycle method?
                     */
                    private ParameterConduit getParameterConduit()
                    {
                        if (conduit == null)
                        {
                            conduit = containerResources
                                    .getParameterConduit(containerParameterName);

                        }

                        return conduit;
                    }

                    public void set(Object newValue)
                    {
                        getParameterConduit().set(newValue);
                    }

                    @SuppressWarnings("unchecked")
                    public Object get()
                    {
                        // For the moment, this results in two passes through the TypeCoercer; we'll look
                        // to optimize that in the future. The first pass is deep inside ParameterConduit (coercing
                        // to the component parameter field type), the second is here (usually the same type so no
                        // real coercion necessary).

                        Object result = getParameterConduit().get();

                        return typeCoercer.coerce(result, fieldType);
                    }
                };
            }
        };

        transformation.getField(fieldName).replaceAccess(provider);
    }

    private String identifyParameterName(ComponentResources resources, String firstGuess,
            String... otherGuesses)
    {
        ComponentModel model = resources.getContainerResources().getComponentModel();

        List<String> guesses = CollectionFactory.newList();
        guesses.add(firstGuess);

        for (String name : otherGuesses)
        {
            guesses.add(name);
        }

        for (String name : guesses)
        {
            if (model.isFormalParameter(name))
                return name;
        }

        String message = String
                .format(
                        "Failed to bind parameter of mixin %s (type %s). Containing component %s does not contain a formal parameter %s %s. Formal parameters: %s.",
                        resources.getCompleteId(),

                        resources.getComponentModel().getComponentClassName(),

                        model.getComponentClassName(),

                        guesses.size() == 1 ? "matching" : "matching any of",

                        InternalUtils.joinSorted(guesses),

                        InternalUtils.joinSorted(model.getDeclaredParameterNames()));

        throw new TapestryException(message, resources.getLocation(), null);
    }
}
