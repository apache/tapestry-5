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

import java.lang.reflect.Modifier;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.MixinClasses;
import org.apache.tapestry5.annotations.Mixins;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.KeyValue;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.Orderable;
import org.apache.tapestry5.ioc.internal.services.StringLocation;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.model.MutableEmbeddedComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.TransformField;

/**
 * Finds fields with the {@link org.apache.tapestry5.annotations.Component} annotation and updates
 * the model. Also
 * checks for the {@link Mixins} and {@link MixinClasses} annotations and uses them to update the {@link ComponentModel}
 * .
 */
public class ComponentWorker implements ComponentClassTransformWorker
{
    private final ComponentClassResolver resolver;

    public ComponentWorker(ComponentClassResolver resolver)
    {
        this.resolver = resolver;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformField field : transformation.matchFieldsWithAnnotation(Component.class))
        {
            transformField(transformation, model, field);
        }
    }

    private void transformField(ClassTransformation transformation, MutableComponentModel model, TransformField field)
    {
        Component annotation = field.getAnnotation(Component.class);

        field.claim(annotation);

        String annotationId = annotation.id();

        String fieldName = field.getName();

        String id = InternalUtils.isNonBlank(annotationId) ? annotationId : InternalUtils.stripMemberName(fieldName);

        String type = field.getType();

        Location location = new StringLocation(String.format("%s.%s", transformation.getClassName(), fieldName), 0);

        MutableEmbeddedComponentModel embedded = model.addEmbeddedComponent(id, annotation.type(), type, annotation
                .inheritInformalParameters(), location);

        addParameters(embedded, annotation.parameters());

        updateModelWithPublishedParameters(embedded, annotation);

        convertAccessToField(transformation, field, id);

        addMixinClasses(field, embedded);
        addMixinTypes(field, embedded);
    }

    private void convertAccessToField(ClassTransformation transformation, TransformField field, String id)
    {
        String fieldName = field.getName();

        ComponentValueProvider<FieldValueConduit> provider = createProviderForEmbeddedComponentConduit(fieldName, id);

        field.replaceAccess(provider);
    }

    private ComponentValueProvider<FieldValueConduit> createProviderForEmbeddedComponentConduit(final String fieldName,
            final String id)
    {
        return new ComponentValueProvider<FieldValueConduit>()
        {
            public FieldValueConduit get(final ComponentResources resources)
            {
                return new ReadOnlyFieldValueConduit(resources, fieldName)
                {
                    public Object get()
                    {
                        return resources.getEmbeddedComponent(id);
                    }
                };
            }
        };
    }

    private void updateModelWithPublishedParameters(MutableEmbeddedComponentModel embedded, Component annotation)
    {
        String names = annotation.publishParameters();

        if (InternalUtils.isNonBlank(names))
            embedded.setPublishedParameters(CollectionFactory.newList(TapestryInternalUtils.splitAtCommas(names)));
    }

    private void addMixinClasses(TransformField field, MutableEmbeddedComponentModel model)
    {
        MixinClasses annotation = field.getAnnotation(MixinClasses.class);

        if (annotation == null)
            return;

        boolean orderEmpty = annotation.order().length == 0;

        if (!orderEmpty && annotation.order().length != annotation.value().length)
            throw new TapestryException(TransformMessages.badMixinConstraintLength(annotation, field.getName()), model,
                    null);

        for (int i = 0; i < annotation.value().length; i++)
        {
            String[] constraints = orderEmpty ? InternalConstants.EMPTY_STRING_ARRAY : TapestryInternalUtils
                    .splitMixinConstraints(annotation.order()[i]);

            model.addMixin(annotation.value()[i].getName(), constraints);
        }
    }

    private void addMixinTypes(TransformField field, MutableEmbeddedComponentModel model)
    {
        Mixins annotation = field.getAnnotation(Mixins.class);

        if (annotation == null)
            return;

        for (String typeName : annotation.value())
        {
            Orderable<String> typeAndOrder = TapestryInternalUtils.mixinTypeAndOrder(typeName);
            String mixinClassName = resolver.resolveMixinTypeToClassName(typeAndOrder.getTarget());
            model.addMixin(mixinClassName, typeAndOrder.getConstraints());
        }
    }

    private void addParameters(MutableEmbeddedComponentModel embedded, String[] parameters)
    {
        for (String parameter : parameters)
        {
            KeyValue kv = TapestryInternalUtils.parseKeyValue(parameter);

            embedded.addParameter(kv.getKey(), kv.getValue());
        }
    }
}
