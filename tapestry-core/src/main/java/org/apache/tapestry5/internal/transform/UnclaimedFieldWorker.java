// Copyright 2006, 2007, 2010, 2011 The Apache Software Foundation
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

import java.lang.reflect.Modifier;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.ComputedValue;
import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Designed to be just about the last worker in the pipeline. Its job is to convert each otherwise unclaimed
 * field into a value stored in the {@link PerthreadManager}.
 */
public final class UnclaimedFieldWorker implements ComponentClassTransformWorker2
{
    private final PerthreadManager perThreadManager;

    private final ComponentClassCache classCache;

    static class UnclaimedFieldConduit implements FieldConduit<Object>
    {
        private final InternalComponentResources resources;

        private final PerThreadValue<Object> fieldValue;

        // Set prior to the containingPageDidLoad lifecycle event
        private Object fieldDefaultValue;

        private UnclaimedFieldConduit(InternalComponentResources resources, PerThreadValue<Object> fieldValue,
                Object fieldDefaultValue)
        {
            this.resources = resources;

            this.fieldValue = fieldValue;
            this.fieldDefaultValue = fieldDefaultValue;
        }

        public Object get(Object instance, InstanceContext context)
        {
            return fieldValue.get(fieldDefaultValue);
        }

        public void set(Object instance, InstanceContext context, Object newValue)
        {
            fieldValue.set(newValue);

            // This catches the case where the instance initializer method sets a value for the field.
            // That value is captured and used when no specific value has been stored.

            if (!resources.isLoaded())
                fieldDefaultValue = newValue;
        }
    }

    public UnclaimedFieldWorker(ComponentClassCache classCache, PerthreadManager perThreadManager)
    {
        this.classCache = classCache;
        this.perThreadManager = perThreadManager;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getUnclaimedFields())
        {
            transformField(field);
        }
    }

    private void transformField(PlasticField field)
    {
        if (Modifier.isFinal(field.getModifiers()))
            return;

        ComputedValue<FieldConduit<Object>> computed = createComputedFieldConduit(field);

        field.setComputedConduit(computed);
    }

    private ComputedValue<FieldConduit<Object>> createComputedFieldConduit(PlasticField field)
    {
        final String fieldType = field.getTypeName();

        return new ComputedValue<FieldConduit<Object>>()
        {
            public FieldConduit<Object> get(InstanceContext context)
            {
                Object fieldDefaultValue = classCache.defaultValueForType(fieldType);
                InternalComponentResources resources = context.get(InternalComponentResources.class);

                return new UnclaimedFieldConduit(resources, perThreadManager.createValue(), fieldDefaultValue);
            }
        };
    }
}
