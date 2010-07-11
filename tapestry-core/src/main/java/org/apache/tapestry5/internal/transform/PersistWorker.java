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
import org.apache.tapestry5.ioc.services.PerthreadManager;
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
    class PerThreadState
    {
        Object value;

        PerThreadState(Object defaultValue)
        {
            value = defaultValue;
        }
    }

    class PersistentFieldConduit implements FieldValueConduit
    {
        private final InternalComponentResources resources;

        private final String name;

        private final Object defaultValue;

        private final String key;

        public PersistentFieldConduit(InternalComponentResources resources, String name, Object defaultValue)
        {
            this.resources = resources;
            this.name = name;
            this.defaultValue = defaultValue;

            this.key = String.format("PersistWorker:%s/%s", resources.getCompleteId(), name);

            resources.addPageLifecycleListener(new PageLifecycleAdapter()
            {
                @Override
                public void restoreStateBeforePageAttach()
                {
                    restoreStateAtPageAttach();
                }
            });
        }

        private PerThreadState getState()
        {
            PerThreadState state = (PerThreadState) perThreadManager.get(key);

            if (state == null)
            {
                state = new PerThreadState(defaultValue);
                perThreadManager.put(key, state);
            }

            return state;
        }

        public Object get()
        {
            return getState().value;
        }

        public void set(Object newValue)
        {
            resources.persistFieldChange(name, newValue);

            getState().value = newValue;
        }

        private void restoreStateAtPageAttach()
        {
            if (resources.hasFieldChange(name))
                getState().value = resources.getFieldChange(name);
        }
    }

    private final ComponentClassCache classCache;

    private final PerthreadManager perThreadManager;

    public PersistWorker(ComponentClassCache classCache, PerthreadManager perThreadManager)
    {
        this.classCache = classCache;
        this.perThreadManager = perThreadManager;
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

    private Object determineDefaultValueFromFieldType(TransformField field)
    {
        return classCache.defaultValueForType(field.getType());
    }
}
