// Copyright 2011 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.EmbeddedMixin;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.ComputedValue;
import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Supports the {@link org.apache.tapestry5.annotations.EmbeddedMixin} annotation, which allows a mixin to be embedded
 * in another mixin. The annotation is applied to a field, which will become read-only, and contain a reference
 * to the mixin instance.
 */
public class EmbeddedMixinWorker implements ComponentClassTransformWorker2
{
    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(EmbeddedMixin.class))
        {
            replaceFieldWithMixin(model, field);
        }
    }

    private void replaceFieldWithMixin(MutableComponentModel model, PlasticField field)
    {
        EmbeddedMixin annotation = field.getAnnotation(EmbeddedMixin.class);

        field.claim(annotation);
        
        String embeddedComponentId = annotation.value();

        String[] order = annotation.order();

        String mixinClassName = field.getTypeName();

        model.addEmbeddedMixinClassName(mixinClassName, embeddedComponentId, order);

        replaceFieldAccessWithMixin(field, mixinClassName);
    }

    private void replaceFieldAccessWithMixin(PlasticField field, String mixinClassName)
    {
        ComputedValue<FieldConduit<Object>> provider = createMixinFieldProvider(field.getName(), mixinClassName);

        field.setComputedConduit(provider);
    }

    private ComputedValue<FieldConduit<Object>> createMixinFieldProvider(final String fieldName,
            final String mixinClassName)
    {
        return new ComputedValue<FieldConduit<Object>>()
        {
            public FieldConduit<Object> get(InstanceContext context)
            {
                ComponentResources resources = context.get(ComponentResources.class);
                final InternalComponentResources icr = (InternalComponentResources) resources;

                return new ReadOnlyComponentFieldConduit(resources, fieldName)
                {
                    public Object get(Object instance, InstanceContext context)
                    {
                        return icr.getMixinByClassName(mixinClassName);
                    }
                };
            }
        };
    }
}
