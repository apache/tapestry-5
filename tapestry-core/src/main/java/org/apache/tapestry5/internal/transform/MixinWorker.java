// Copyright 2006, 2008, 2009, 2010, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Supports the {@link org.apache.tapestry5.annotations.Mixin} annotation, which allows a mixin to
 * be part of the
 * implementation of a component. The annotation is applied to a field, which will become read-only,
 * and contain a
 * reference to the mixin instance.
 */
public class MixinWorker implements ComponentClassTransformWorker2
{
    private final ComponentClassResolver resolver;

    public MixinWorker(final ComponentClassResolver resolver)
    {
        this.resolver = resolver;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(Mixin.class))
        {
            replaceFieldWithMixin(model, field);
        }
    }

    private void replaceFieldWithMixin(MutableComponentModel model, PlasticField field)
    {
        Mixin annotation = field.getAnnotation(Mixin.class);

        field.claim(annotation);

        String mixinType = annotation.value();

        String[] order = annotation.order();

        String fieldType = field.getTypeName();

        String mixinClassName = InternalUtils.isBlank(mixinType) ? fieldType : resolver
                .resolveMixinTypeToClassName(mixinType);

        model.addMixinClassName(mixinClassName, order);

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
            public FieldConduit get(InstanceContext context)
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
