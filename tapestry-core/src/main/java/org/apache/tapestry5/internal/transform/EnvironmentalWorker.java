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

import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Obtains a value from the {@link Environment} service based on the field type. This is triggered by the presence of
 * the {@link Environmental} annotation.
 */
@SuppressWarnings("rawtypes")
public class EnvironmentalWorker implements ComponentClassTransformWorker2
{
    private final Environment environment;

    private final ComponentClassCache classCache;


    @SuppressWarnings("unchecked")
    private final class EnvironmentalConduit implements FieldConduit
    {
        private final String componentClassName;

        private final String fieldName;

        private final Class environmentalType;

        private final boolean required;

        private EnvironmentalConduit(String componentClassName, String fieldName, final Class environmentalType,
                boolean required)
        {
            this.componentClassName = componentClassName;
            this.fieldName = fieldName;
            this.environmentalType = environmentalType;
            this.required = required;
        }

        public Object get(Object instance, InstanceContext context)
        {
            return required ? environment.peekRequired(environmentalType) : environment.peek(environmentalType);
        }

        public void set(Object instance, InstanceContext context, Object newValue)
        {
            throw new RuntimeException(String.format("Field %s.%s is read only.", componentClassName, fieldName));
        }
    }

    public EnvironmentalWorker(Environment environment, ComponentClassCache classCache)
    {
        this.environment = environment;

        this.classCache = classCache;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(Environmental.class))
        {
            transform(model.getComponentClassName(), field);
        }
    }

    private void transform(final String componentClassName, PlasticField field)
    {
        Environmental annotation = field.getAnnotation(Environmental.class);

        field.claim(annotation);

        final String fieldName = field.getName();

        final Class fieldType = classCache.forName(field.getTypeName());

        final boolean required = annotation.value();

        ComputedValue<FieldConduit<Object>> provider = new ComputedValue<FieldConduit<Object>>()
        {
            public FieldConduit<Object> get(InstanceContext context)
            {
                return new EnvironmentalConduit(componentClassName, fieldName, fieldType, required);
            }

            public void set(Object instance, InstanceContext context, Object newValue)
            {
                throw new RuntimeException(
                        String.format("Field %s of component %s is read only.", fieldName, componentClassName));
            }
        };

        field.setComputedConduit(provider);
    }

}
