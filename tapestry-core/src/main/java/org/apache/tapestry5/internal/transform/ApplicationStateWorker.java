// Copyright 2007, 2008, 2010 The Apache Software Foundation
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
import java.util.Map;
import java.util.TreeMap;

import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.Predicate;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformField;

/**
 * Looks for the {@link ApplicationState} and {@link org.apache.tapestry5.annotations.SessionState} annotations and
 * converts read and write access on such fields into calls to the {@link ApplicationStateManager}.
 */
public class ApplicationStateWorker implements ComponentClassTransformWorker
{
    private final ApplicationStateManager applicationStateManager;

    private final ComponentClassCache componentClassCache;

    public ApplicationStateWorker(ApplicationStateManager applicationStateManager,
            ComponentClassCache componentClassCache)
    {
        this.applicationStateManager = applicationStateManager;
        this.componentClassCache = componentClassCache;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        Map<TransformField, Boolean> fields = new TreeMap<TransformField, Boolean>();

        for (TransformField field : transformation
                .matchFieldsWithAnnotation(ApplicationState.class))
        {
            ApplicationState annotation = field.getAnnotation(ApplicationState.class);

            fields.put(field, annotation.create());

            field.claim(annotation);
        }

        for (TransformField field : transformation.matchFieldsWithAnnotation(SessionState.class))
        {
            SessionState annotation = field.getAnnotation(SessionState.class);

            fields.put(field, annotation.create());

            field.claim(annotation);
        }

        for (Map.Entry<TransformField, Boolean> e : fields.entrySet())
        {
            transform(transformation, e.getKey(), e.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void transform(ClassTransformation transformation, TransformField field,
            final boolean create)
    {
        final Class fieldClass = componentClassCache.forName(field.getType());

        field.replaceAccess(new FieldValueConduit()
        {
            public void set(Object newValue)
            {
                applicationStateManager.set(fieldClass, newValue);
            }

            public Object get()
            {
                return create ? applicationStateManager.get(fieldClass) : applicationStateManager
                        .getIfExists(fieldClass);
            }
        });

        final String expectedName = field.getName() + "Exists";

        List<TransformField> fields = transformation.matchFields(new Predicate<TransformField>()
        {
            public boolean accept(TransformField field)
            {
                return field.getType().equals("boolean")
                        && field.getName().equalsIgnoreCase(expectedName);
            }
        });

        for (TransformField existsField : fields)
        {
            existsField.claim(this);

            String className = transformation.getClassName();

            String fieldName = existsField.getName();

            existsField.replaceAccess(new ReadOnlyFieldValueConduit(className, fieldName)
            {
                public Object get()
                {
                    return applicationStateManager.exists(fieldClass);
                }
            });
        }
    }
}
