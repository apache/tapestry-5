// Copyright 2008, 2010, 2011, 2012 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Recognizes the {@link org.apache.tapestry5.annotations.InjectComponent} annotation, and converts the field into a
 * read-only field containing the component. The id of the component may be explicitly stated or will be determined
 * from the field name.
 */
public class InjectComponentWorker implements ComponentClassTransformWorker2
{
    private final class InjectedComponentFieldValueConduit extends ReadOnlyComponentFieldConduit
    {
        private final ComponentResources resources;

        private final String fieldName, componentId, type;

        private Component embedded;

        private InjectedComponentFieldValueConduit(ComponentResources resources, String fieldName, String type,
                                                   String componentId)
        {
            super(resources, fieldName);

            this.resources = resources;
            this.fieldName = fieldName;
            this.componentId = componentId;
            this.type = type;

            resources.getPageLifecycleCallbackHub().addPageAttachedCallback(new Runnable()
            {
                public void run()
                {
                    load();
                }
            });
        }

        private void load()
        {
            try
            {
                embedded = resources.getEmbeddedComponent(componentId);
            } catch (UnknownValueException ex)
            {
                throw new RuntimeException(String.format("Unable to inject component into field %s of class %s: %s",
                        fieldName, getComponentClassName(), ex.getMessage()), ex);
            }

            Class fieldType = classCache.forName(type);

            if (!fieldType.isInstance(embedded))
                throw new RuntimeException(
                        String
                                .format(
                                        "Unable to inject component '%s' into field %s of %s. Class %s is not assignable to a field of type %s.",
                                        componentId, fieldName, getComponentClassName(),
                                        embedded.getClass().getName(), fieldType.getName()));
        }

        private String getComponentClassName()
        {
            return resources.getComponentModel().getComponentClassName();
        }

        public Object get(Object instance, InstanceContext context)
        {
            return embedded;
        }
    }

    private final ComponentClassCache classCache;

    public InjectComponentWorker(ComponentClassCache classCache)
    {
        this.classCache = classCache;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(InjectComponent.class))
        {
            InjectComponent annotation = field.getAnnotation(InjectComponent.class);

            field.claim(annotation);

            final String type = field.getTypeName();

            final String componentId = getComponentId(field, annotation);

            final String fieldName = field.getName();

            ComputedValue<FieldConduit<Object>> provider = new ComputedValue<FieldConduit<Object>>()
            {
                public FieldConduit<Object> get(InstanceContext context)
                {
                    ComponentResources resources = context.get(ComponentResources.class);

                    return new InjectedComponentFieldValueConduit(resources, fieldName, type, componentId);
                }
            };

            field.setComputedConduit(provider);
        }

    }

    private String getComponentId(PlasticField field, InjectComponent annotation)
    {
        String id = annotation.value();

        if (InternalUtils.isNonBlank(id))
            return id;

        return InternalUtils.stripMemberName(field.getName());
    }
}
