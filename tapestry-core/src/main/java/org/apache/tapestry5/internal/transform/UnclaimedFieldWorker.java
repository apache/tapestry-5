// Copyright 2006, 2007, 2010 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.ioc.services.PerThreadValue;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.TransformField;

/**
 * Designed to be just about the last worker in the pipeline. Its job is to convert each otherwise unclaimed
 * field into a value stored in the {@link PerthreadManager}.
 */
public final class UnclaimedFieldWorker implements ComponentClassTransformWorker
{
    private final PerthreadManager perThreadManager;

    private final ComponentClassCache classCache;

    static class UnclaimedFieldConduit implements FieldValueConduit
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

        public Object get()
        {
            return fieldValue.exists() ? fieldValue.get() : fieldDefaultValue;
        }

        public void set(Object newValue)
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

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformField field : transformation.matchUnclaimedFields())
        {
            transformField(field);
        }
    }

    private void transformField(TransformField field)
    {
        int modifiers = field.getModifiers();

        if (Modifier.isFinal(modifiers) || Modifier.isStatic(modifiers))
            return;

        ComponentValueProvider<FieldValueConduit> provider = createFieldValueConduitProvider(field);

        field.replaceAccess(provider);
    }

    private ComponentValueProvider<FieldValueConduit> createFieldValueConduitProvider(TransformField field)
    {
        final String fieldName = field.getName();
        final String fieldType = field.getType();

        return new ComponentValueProvider<FieldValueConduit>()
        {
            public FieldValueConduit get(ComponentResources resources)
            {
                Object fieldDefaultValue = classCache.defaultValueForType(fieldType);

                String key = String.format("UnclaimedFieldWorker:%s/%s", resources.getCompleteId(), fieldName);

                return new UnclaimedFieldConduit((InternalComponentResources) resources,
                        perThreadManager.createValue(key), fieldDefaultValue);
            }
        };
    }
}
