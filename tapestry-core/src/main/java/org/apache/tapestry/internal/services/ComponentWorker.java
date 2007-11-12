// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.MixinClasses;
import org.apache.tapestry.annotations.Mixins;
import org.apache.tapestry.internal.KeyValue;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.services.StringLocation;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.model.MutableEmbeddedComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformConstants;

/**
 * Finds fields with the {@link org.apache.tapestry.annotations.Component} annotation and updates
 * the model. Also checks for the {@link Mixins} and {@link MixinClasses} annotations and uses them
 * to update the {@link ComponentModel}.
 */
public class ComponentWorker implements ComponentClassTransformWorker
{
    private final ComponentClassResolver _resolver;

    public ComponentWorker(final ComponentClassResolver resolver)
    {
        _resolver = resolver;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (String fieldName : transformation.findFieldsWithAnnotation(Component.class))
        {
            Component annotation = transformation.getFieldAnnotation(fieldName, Component.class);

            String id = annotation.id();

            if (InternalUtils.isBlank(id)) id = InternalUtils.stripMemberPrefix(fieldName);

            String type = transformation.getFieldType(fieldName);

            Location location = new StringLocation(String.format("%s.%s", transformation
                    .getClassName(), fieldName), 0);

            MutableEmbeddedComponentModel embedded = model.addEmbeddedComponent(id, annotation
                    .type(), type, location);

            addParameters(embedded, annotation.parameters());

            transformation.makeReadOnly(fieldName);

            String body = String.format(
                    "%s = (%s) %s.getEmbeddedComponent(\"%s\");",
                    fieldName,
                    type,
                    transformation.getResourcesFieldName(),
                    id);

            transformation
                    .extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, body);

            addMixinClasses(fieldName, transformation, embedded);
            addMixinTypes(fieldName, transformation, embedded);

            transformation.claimField(fieldName, annotation);
        }
    }

    private void addMixinClasses(String fieldName, ClassTransformation transformation,
                                 MutableEmbeddedComponentModel model)
    {
        MixinClasses annotation = transformation.getFieldAnnotation(fieldName, MixinClasses.class);

        if (annotation == null) return;

        for (Class c : annotation.value())
            model.addMixin(c.getName());
    }

    private void addMixinTypes(String fieldName, ClassTransformation transformation,
                               MutableEmbeddedComponentModel model)
    {
        Mixins annotation = transformation.getFieldAnnotation(fieldName, Mixins.class);

        if (annotation == null) return;

        for (String typeName : annotation.value())
        {
            String mixinClassName = _resolver.resolveMixinTypeToClassName(typeName);
            model.addMixin(mixinClassName);
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
