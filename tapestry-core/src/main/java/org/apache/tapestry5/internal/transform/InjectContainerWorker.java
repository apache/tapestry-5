// Copyright 2006, 2007, 2008, 2010, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Identifies the {@link org.apache.tapestry5.annotations.InjectContainer} annotation and adds code
 * to initialize it to
 * the core component.
 */
public class InjectContainerWorker implements ComponentClassTransformWorker2
{
    private final ComponentClassCache cache;

    public InjectContainerWorker(ComponentClassCache cache)
    {
        this.cache = cache;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (final PlasticField field : plasticClass.getFieldsWithAnnotation(InjectContainer.class))
        {
            transformField(field);
        }
    }

    private void transformField(PlasticField field)
    {
        InjectContainer annotation = field.getAnnotation(InjectContainer.class);

        field.claim(annotation);

        ComputedValue<FieldConduit<Object>> provider = createFieldValueConduitProvider(field);

        field.setComputedConduit(provider);
    }

    private ComputedValue<FieldConduit<Object>> createFieldValueConduitProvider(PlasticField field)
    {
        final String fieldName = field.getName();

        final String fieldTypeName = field.getTypeName();

        return new ComputedValue<FieldConduit<Object>>()
        {
            public FieldConduit<Object> get(InstanceContext context)
            {
                final Class fieldType = cache.forName(fieldTypeName);
                final ComponentResources resources = context.get(ComponentResources.class);

                return new ReadOnlyComponentFieldConduit(resources, fieldName)
                {
                    public Object get(Object instance, InstanceContext context)
                    {
                        Component container = resources.getContainer();

                        if (!fieldType.isInstance(container))
                        {
                            String message = String.format(
                                    "Component %s (type %s) is not assignable to field %s.%s (of type %s).", container
                                    .getComponentResources().getCompleteId(), container.getClass().getName(), resources.getComponentModel()
                                    .getComponentClassName(), fieldName, fieldTypeName);

                            throw new RuntimeException(message);
                        }

                        return container;
                    }
                };
            }
        };
    }
}
