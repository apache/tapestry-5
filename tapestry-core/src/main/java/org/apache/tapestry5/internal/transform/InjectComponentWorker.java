// Copyright 2008, 2010 The Apache Software Foundation
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
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleAdapter;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.TransformField;

/**
 * Recognizes the {@link org.apache.tapestry5.annotations.InjectComponent} annotation, and converts the field into a
 * read-only field containing the component. The id of the component may be explicitly stated or will be determined
 * from the field name.
 */
public class InjectComponentWorker implements ComponentClassTransformWorker
{
    private final ComponentClassCache classCache;

    public InjectComponentWorker(ComponentClassCache classCache)
    {
        this.classCache = classCache;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformField field : transformation.matchFieldsWithAnnotation(InjectComponent.class))
        {
            InjectComponent annotation = field.getAnnotation(InjectComponent.class);

            field.claim(annotation);

            final String type = field.getType();

            final String componentId = getComponentId(field, annotation);

            final String fieldName = field.getName();

            ComponentValueProvider<FieldValueConduit> provider = new ComponentValueProvider<FieldValueConduit>()
            {
                public FieldValueConduit get(final ComponentResources resources)
                {
                    return new ReadOnlyFieldValueConduit(resources, fieldName)
                    {
                        private Component embedded;

                        {
                            resources.addPageLifecycleListener(new PageLifecycleAdapter()
                            {
                                public void containingPageDidLoad()
                                {
                                    embedded = resources.getEmbeddedComponent(componentId);

                                    Class fieldType = classCache.forName(type);

                                    if (!fieldType.isInstance(embedded))
                                        throw new RuntimeException(
                                                String
                                                        .format(
                                                                "Unable to inject component '%s' into field %s of component %s.  Class %s is not assignable to a field of type %s.",
                                                                componentId, fieldName, resources.getCompleteId(),
                                                                embedded.getClass().getName(), fieldType.getName()));
                                };
                            });
                        }

                        public Object get()
                        {
                            return embedded;
                        }
                    };
                }
            };

            field.replaceAccess(provider);
        }

    }

    private String getComponentId(TransformField field, InjectComponent annotation)
    {
        String id = annotation.value();

        if (InternalUtils.isNonBlank(id))
            return id;

        return InternalUtils.stripMemberName(field.getName());
    }
}
