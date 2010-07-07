// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.TransformField;

/**
 * Obtains a value from the {@link Environment} service based on the field type. This is triggered by the presence of
 * the {@link Environmental} annotation.
 */
@SuppressWarnings("rawtypes")
public class EnvironmentalWorker implements ComponentClassTransformWorker
{
    private final Environment environment;

    private final ComponentClassCache classCache;

    @SuppressWarnings("unchecked")
    private final class EnvironmentalConduit extends ReadOnlyFieldValueConduit
    {
        private final Class environmentalType;

        private final boolean required;

        private EnvironmentalConduit(ComponentResources resources, String fieldName, final Class environmentalType,
                boolean required)
        {
            super(resources, fieldName);

            this.environmentalType = environmentalType;
            this.required = required;
        }

        public Object get()
        {
            return required ? environment.peekRequired(environmentalType) : environment.peek(environmentalType);
        }
    }

    public EnvironmentalWorker(Environment environment, ComponentClassCache classCache)
    {
        this.environment = environment;

        this.classCache = classCache;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformField field : transformation.matchFieldsWithAnnotation(Environmental.class))
        {
            transform(field);
        }
    }

    private void transform(TransformField field)
    {
        Environmental annotation = field.getAnnotation(Environmental.class);

        field.claim(annotation);

        final String fieldName = field.getName();

        final Class fieldType = classCache.forName(field.getType());

        final boolean required = annotation.value();

        ComponentValueProvider<FieldValueConduit> provider = new ComponentValueProvider<FieldValueConduit>()
        {
            public FieldValueConduit get(ComponentResources resources)
            {
                return new EnvironmentalConduit(resources, fieldName, fieldType, required);
            }
        };

        field.replaceAccess(provider);
    }

}
