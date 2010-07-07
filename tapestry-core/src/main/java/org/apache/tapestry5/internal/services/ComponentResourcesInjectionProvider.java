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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.transform.ReadOnlyFieldValueConduit;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.services.FieldValueConduit;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentValueProvider;
import org.apache.tapestry5.services.InjectionProvider;
import org.apache.tapestry5.services.TransformField;

/**
 * Allows for the injection of the component's {@link org.apache.tapestry5.ComponentResources}.
 */
public class ComponentResourcesInjectionProvider implements InjectionProvider
{

    public boolean provideInjection(final String fieldName, Class fieldType, ObjectLocator locator,
            ClassTransformation transformation, MutableComponentModel componentModel)
    {
        if (!fieldType.equals(ComponentResources.class))
            return false;

        TransformField field = transformation.getField(fieldName);

        ComponentValueProvider<FieldValueConduit> provider = createResourcesFieldConduitProvider(fieldName);

        field.replaceAccess(provider);

        return true;
    }

    private ComponentValueProvider<FieldValueConduit> createResourcesFieldConduitProvider(final String fieldName)
    {
        return new ComponentValueProvider<FieldValueConduit>()
        {
            public FieldValueConduit get(final ComponentResources resources)
            {
                return new ReadOnlyFieldValueConduit(resources, fieldName)
                {
                    public Object get()
                    {
                        return resources;
                    }
                };
            }
        };
    }
}
