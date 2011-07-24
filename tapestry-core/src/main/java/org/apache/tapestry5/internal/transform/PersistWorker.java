// Copyright 2006, 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.*;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Converts fields with the {@link org.apache.tapestry5.annotations.Persist} annotation into persistent fields.
 */
public class PersistWorker implements ComponentClassTransformWorker2
{
    class PersistentFieldConduit implements FieldConduit<Object>
    {
        private final InternalComponentResources resources;

        private final String name;

        private final PerThreadValue<Object> fieldValue = perThreadManager.createValue();

        private final Object defaultValue;

        public PersistentFieldConduit(InternalComponentResources resources, String name,
                                      Object defaultValue)
        {
            this.resources = resources;
            this.name = name;
            this.defaultValue = defaultValue;
        }

        public Object get(Object instance, InstanceContext context)
        {
            if (!fieldValue.exists())
            {
                Object persistedValue = resources.hasFieldChange(name) ? resources.getFieldChange(name) : defaultValue;

                fieldValue.set(persistedValue);
            }

            return fieldValue.get();
        }

        public void set(Object instance, InstanceContext context, Object newValue)
        {
            resources.persistFieldChange(name, newValue);

            fieldValue.set(newValue);
        }
    }

    private final ComponentClassCache classCache;

    private final PerthreadManager perThreadManager;

    public PersistWorker(ComponentClassCache classCache, PerthreadManager perThreadManager)
    {
        this.classCache = classCache;
        this.perThreadManager = perThreadManager;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(Persist.class))
        {
            makeFieldPersistent(field, model);
        }
    }

    private void makeFieldPersistent(PlasticField field, MutableComponentModel model)
    {
        Persist annotation = field.getAnnotation(Persist.class);

        field.claim(annotation);

        final String logicalFieldName = model.setFieldPersistenceStrategy(field.getName(), annotation.value());

        final Object defaultValue = determineDefaultValueFromFieldType(field);

        ComputedValue<FieldConduit<Object>> computed = new ComputedValue<FieldConduit<Object>>()
        {
            public FieldConduit<Object> get(InstanceContext context)
            {
                InternalComponentResources resources = context.get(InternalComponentResources.class);
                return new PersistentFieldConduit(resources, logicalFieldName, defaultValue);
            }
        };

        field.setComputedConduit(computed);
    }

    private Object determineDefaultValueFromFieldType(PlasticField field)
    {
        return classCache.defaultValueForType(field.getTypeName());
    }
}
