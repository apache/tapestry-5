// Copyright 2007, 2008, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import java.util.List;

/**
 * Looks for the {@link org.apache.tapestry5.annotations.SessionState} annotations and
 * converts read and write access on such fields into calls to the {@link ApplicationStateManager}.
 */
public class ApplicationStateWorker implements ComponentClassTransformWorker2
{
    private final ApplicationStateManager applicationStateManager;

    private final ComponentClassCache componentClassCache;

    public ApplicationStateWorker(ApplicationStateManager applicationStateManager,
            ComponentClassCache componentClassCache)
    {
        this.applicationStateManager = applicationStateManager;
        this.componentClassCache = componentClassCache;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(SessionState.class))
        {
            SessionState annotation = field.getAnnotation(SessionState.class);

            transform(plasticClass, field, annotation.create());

            field.claim(annotation);
        }
    }

    @SuppressWarnings("unchecked")
    private void transform(PlasticClass transformation, PlasticField field, final boolean create)
    {
        final Class fieldClass = componentClassCache.forName(field.getTypeName());

        field.setConduit(new FieldConduit()
        {
            public void set(Object instance, InstanceContext context, Object newValue)
            {
                applicationStateManager.set(fieldClass, newValue);
            }

            public Object get(Object instance, InstanceContext context)
            {
                return create ? applicationStateManager.get(fieldClass) : applicationStateManager
                        .getIfExists(fieldClass);
            }
        });

        final String expectedName = field.getName() + "Exists";

        List<PlasticField> fields = F.flow(transformation.getAllFields()).filter(new Predicate<PlasticField>()
        {
            public boolean accept(PlasticField field)
            {
                return field.getTypeName().equals("boolean") && field.getName().equalsIgnoreCase(expectedName);
            }
        }).toList();

        for (PlasticField existsField : fields)
        {
            existsField.claim(this);

            final String className = transformation.getClassName();

            final String fieldName = existsField.getName();

            existsField.setConduit(new ReadOnlyComponentFieldConduit(className, fieldName)
            {
                public Object get(Object instance, InstanceContext context)
                {
                    return applicationStateManager.exists(fieldClass);
                }
            });
        }
    }
}
