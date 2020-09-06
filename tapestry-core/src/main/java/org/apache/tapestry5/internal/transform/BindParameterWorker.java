// Copyright 2009-2013 The Apache Software Foundation
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


import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.BindParameter;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.ExceptionUtils;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import java.util.List;

/**
 * Responsible for identifying, via the {@link org.apache.tapestry5.annotations.BindParameter} annotation, mixin fields
 * that should be bound to a core-component parameter value.
 *
 * @since 5.2.0
 */
public class BindParameterWorker implements ComponentClassTransformWorker2
{
    private final class BoundParameterFieldValueConduit implements FieldConduit<Object>
    {
        private final String containerParameterName;

        private final InternalComponentResources containerResources;

        private final Class fieldType;

        // Guarded by this
        private ParameterConduit conduit;

        private BoundParameterFieldValueConduit(String containerParameterName,
                                                InternalComponentResources containerResources, Class fieldType)
        {
            this.containerParameterName = containerParameterName;
            this.containerResources = containerResources;
            this.fieldType = fieldType;
        }

        /**
         * Defer obtaining the conduit object until needed, to deal with the complex
         * lifecycle of
         * parameters. Perhaps this can be addressed by converting constructors into
         * methods invoked
         * from the page loaded lifecycle method?
         */
        private synchronized ParameterConduit getParameterConduit()
        {
            if (conduit == null)
            {
                // if the parameter is not a formal parameter then it must be a published parameter
                if (containerResources.getComponentModel().isFormalParameter(containerParameterName))
                    conduit = containerResources.getParameterConduit(containerParameterName);
                else
                    conduit = getEmbeddedComponentResourcesForPublishedParameter(containerResources, containerParameterName)
                            .getParameterConduit(containerParameterName);
            }

            return conduit;
        }


        public Object get(Object instance, InstanceContext context)
        {
            // For the moment, this results in two passes through the TypeCoercer; we'll look
            // to optimize that in the future. The first pass is deep inside ParameterConduit (coercing
            // to the component parameter field type), the second is here (usually the same type so no
            // real coercion necessary).

            Object result = getParameterConduit().get(instance, context);

            return typeCoercer.coerce(result, fieldType);
        }

        public void set(Object instance, InstanceContext context, Object newValue)
        {
            getParameterConduit().set(instance, context, newValue);

        }
    }

    private final TypeCoercer typeCoercer;

    private final ComponentClassCache componentClassCache;

    public BindParameterWorker(TypeCoercer typeCoercer, ComponentClassCache componentClassCache)
    {
        this.typeCoercer = typeCoercer;
        this.componentClassCache = componentClassCache;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(BindParameter.class))
        {
            convertFieldIntoContainerBoundParameter(field);
        }
    }

    private void convertFieldIntoContainerBoundParameter(PlasticField field)
    {
        BindParameter annotation = field.getAnnotation(BindParameter.class);

        field.claim(annotation);

        final String[] possibleNames = annotation.value();

        final String fieldTypeName = field.getTypeName();

        final String fieldName = field.getName();

        ComputedValue<FieldConduit<Object>> computedConduit = new ComputedValue<FieldConduit<Object>>()
        {
            public FieldConduit<Object> get(InstanceContext context)
            {
                ComponentResources resources = context.get(ComponentResources.class);

                try
                {
                    return createConduit(resources, fieldTypeName, fieldName, possibleNames);
                } catch (Exception ex)
                {
                    throw new TapestryException(String.format(
                            "Failure binding parameter field '%s' of mixin %s (type %s): %s", fieldName, resources
                            .getCompleteId(), resources.getComponentModel().getComponentClassName(),
                            ExceptionUtils.toMessage(ex)), ex);
                }
            }

        };

        field.setComputedConduit(computedConduit);
    }

    private FieldConduit<Object> createConduit(final ComponentResources resources, final String fieldTypeName,
                                               final String fieldName, final String[] possibleNames)
    {
        if (!resources.isMixin())
            throw new TapestryException(String.format("@BindParameter was used on field '%s' of component class '%s', but @BindParameter should only be used in mixins.", fieldName, resources.getComponentModel()
                    .getComponentClassName()), null);

        InternalComponentResources containerResources = (InternalComponentResources) resources.getContainerResources();

        // Evaluate this early so that we get a fast fail.

        String containerParameterName = identifyParameterName(resources, InternalUtils.stripMemberName(fieldName),
                possibleNames);

        Class fieldType = componentClassCache.forName(fieldTypeName);

        return new BoundParameterFieldValueConduit(containerParameterName, containerResources, fieldType);
    }

    private String identifyParameterName(ComponentResources resources, String firstGuess, String... otherGuesses)
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

            if (isPublishedParameter(model, name))
                return name;
        }

        String message = String.format("Containing component %s does not contain a formal parameter or a published parameter %s %s.",

                model.getComponentClassName(),

                guesses.size() == 1 ? "matching" : "matching any of",

                InternalUtils.joinSorted(guesses));

        List<String> formalAndPublishedParameters = CollectionFactory.newList(model.getParameterNames());
        formalAndPublishedParameters.addAll(getPublishedParameters(model));

        throw new UnknownValueException(message, new AvailableValues("Formal and published parameters", formalAndPublishedParameters));
    }

    /**
     * Returns true if the parameter with the given parameterName is a published parameter
     * of any of the embedded components for the component with the given model.
     */
    private boolean isPublishedParameter(ComponentModel model, String parameterName)
    {
        for (String embeddedComponentId : model.getEmbeddedComponentIds())
        {
            EmbeddedComponentModel embeddedComponentModel = model
                    .getEmbeddedComponentModel(embeddedComponentId);
            if (embeddedComponentModel.getPublishedParameters().contains(parameterName)) return true;
        }

        return false;
    }

    private List<String> getPublishedParameters(ComponentModel model)
    {
        List<String> publishedParameters = CollectionFactory.newList();
        for (String embeddedComponentId : model.getEmbeddedComponentIds())
        {
            EmbeddedComponentModel embeddedComponentModel = model.getEmbeddedComponentModel(embeddedComponentId);
            publishedParameters.addAll(embeddedComponentModel.getPublishedParameters());
        }
        return publishedParameters;
    }

    /**
     * Returns the {@link InternalComponentResources} of an embeddedComponent that contains the published parameter
     * publishedParameterName. This is basically a recursive search for published parameters.
     */
    private InternalComponentResources getEmbeddedComponentResourcesForPublishedParameter(InternalComponentResources containerResources,
                                                                                          String publishedParameterName)
    {
        List<InternalComponentResources> embeddedComponentResourcesList = CollectionFactory.newList();

        embeddedComponentResourcesList.add(containerResources);

        while (!embeddedComponentResourcesList.isEmpty())
        {
            InternalComponentResources resources = embeddedComponentResourcesList.remove(0);

            ComponentModel containerComponentModel = resources.getComponentModel();

            for (String embeddedComponentId : containerComponentModel.getEmbeddedComponentIds())
            {
                EmbeddedComponentModel embeddedComponentModel = containerComponentModel
                        .getEmbeddedComponentModel(embeddedComponentId);

                InternalComponentResources embeddedComponentResources = (InternalComponentResources) resources
                        .getEmbeddedComponent(embeddedComponentId).getComponentResources();
                /**
                 * If the parameter is not a formal parameter, then the parameter must be a published parameter
                 * of an embeddedComponent of the component we are currently examining.
                 */
                if (embeddedComponentModel.getPublishedParameters().contains(publishedParameterName)
                        && embeddedComponentResources.getComponentModel().isFormalParameter(publishedParameterName))
                {
                    return embeddedComponentResources;
                }

                embeddedComponentResourcesList.add(embeddedComponentResources);
            }
        }

        return null;
    }
}

