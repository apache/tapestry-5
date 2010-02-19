// Copyright 2006, 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.PageLifecycleAdapter;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.TransformField;

/**
 * Converts fields with the {@link org.apache.tapestry5.annotations.Persist} annotation into persistent fields.
 */
public class PersistWorker implements ComponentClassTransformWorker
{
    class PersistentFieldConduit implements FieldValueConduit
    {
        private final InternalComponentResources resources;

        private final String name;

        private final Object defaultValue;

        private Object currentValue;

        public PersistentFieldConduit(InternalComponentResources resources, String name, Object defaultValue)
        {
            this.resources = resources;
            this.name = name;
            this.currentValue = defaultValue;
            this.defaultValue = defaultValue;

            resources.addPageLifecycleListener(new PageLifecycleAdapter()
            {
                @Override
                public void containingPageDidDetach()
                {
                    resetToDefaultAtPageDetach();
                }

                @Override
                public void restoreStateBeforePageAttach()
                {
                    restoreStateAtPageAttach();
                }
            });
        }

        public Object get()
        {
            return currentValue;
        }

        public void set(Object newValue)
        {
            resources.persistFieldChange(name, newValue);

            currentValue = newValue;
        }

        private void resetToDefaultAtPageDetach()
        {
            currentValue = defaultValue;
        }

        private void restoreStateAtPageAttach()
        {
            if (resources.hasFieldChange(name))
                currentValue = resources.getFieldChange(name);
        }
    }

    private final TypeCoercer typeCoercer;

    private final ComponentClassCache classCache;

    public PersistWorker(TypeCoercer typeCoercer, ComponentClassCache classCache)
    {
        this.typeCoercer = typeCoercer;
        this.classCache = classCache;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<TransformField> fieldsWithAnnotation = transformation.matchFieldsWithAnnotation(Persist.class);

        for (TransformField field : fieldsWithAnnotation)
        {
            makeFieldPersistent(field, model);
        }
    }

    private void makeFieldPersistent(TransformField field, MutableComponentModel model)
    {
        Persist annotation = field.getAnnotation(Persist.class);

        field.claim(annotation);

        final String logicalFieldName = model.setFieldPersistenceStrategy(field.getName(), annotation.value());

        final Object defaultValue = determineDefaultValueFromFieldType(field);

        ComponentValueProvider<FieldValueConduit> provider = new ComponentValueProvider<FieldValueConduit>()
        {
            public FieldValueConduit get(ComponentResources resources)
            {
                return new PersistentFieldConduit((InternalComponentResources) resources, logicalFieldName,
                        defaultValue);
            }
        };

        field.replaceAccess(provider);
    }

    @SuppressWarnings("unchecked")
    private Object determineDefaultValueFromFieldType(TransformField field)
    {
        Class javaType = classCache.forName(field.getType());

        return javaType.isPrimitive() ? typeCoercer.coerce(0, javaType) : null;
    }
}
