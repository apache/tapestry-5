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
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.PageLifecycleAdapter;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.TransformField;

/**
 * Designed to be just about the last worker in the pipeline. Its job is to add cleanup code that restores transient
 * fields back to their initial (null) value. Fields that have been previously {@linkplain TransformField#claim(Object)
 * claimed} are ignored, as are fields that are final.
 */
public final class UnclaimedFieldWorker implements ComponentClassTransformWorker
{
    private final ComponentClassCache classCache;

    public class UnclaimedFieldConduit implements FieldValueConduit
    {
        private final InternalComponentResources resources;

        private Object fieldValue, fieldDefaultValue;

        private UnclaimedFieldConduit(InternalComponentResources resources, Object fieldDefaultValue)
        {
            this.resources = resources;

            this.fieldValue = fieldDefaultValue;
            this.fieldDefaultValue = fieldDefaultValue;

            resources.addPageLifecycleListener(new PageLifecycleAdapter()
            {
                @Override
                public void containingPageDidDetach()
                {
                    reset();
                }
            });
        }

        public Object get()
        {
            return fieldValue;
        }

        public void set(Object newValue)
        {
            fieldValue = newValue;

            if (!resources.isLoaded())
                fieldDefaultValue = newValue;
        }

        public void reset()
        {
            fieldValue = fieldDefaultValue;
        }
    }

    public UnclaimedFieldWorker(ComponentClassCache classCache)
    {
        this.classCache = classCache;
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
        final String fieldType = field.getType();

        return new ComponentValueProvider<FieldValueConduit>()
        {
            public FieldValueConduit get(ComponentResources resources)
            {
                Object fieldDefaultValue = classCache.defaultValueForType(fieldType);

                return new UnclaimedFieldConduit((InternalComponentResources) resources, fieldDefaultValue);
            }
        };
    }
}
