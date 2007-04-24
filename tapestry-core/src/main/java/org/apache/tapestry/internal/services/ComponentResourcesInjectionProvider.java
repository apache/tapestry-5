// Copyright 2006 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.internal.services;

import static java.lang.String.format;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.InjectionProvider;
import org.apache.tapestry.services.TransformConstants;

/**
 * Allows for the injection of the component's {@link org.apache.tapestry.ComponentResources}.
 */
public class ComponentResourcesInjectionProvider implements InjectionProvider
{
    private static final String COMPONENT_RESOURCES_CLASS_NAME = ComponentResources.class.getName();

    public boolean provideInjection(String fieldName, String fieldType, ServiceLocator locator,
            ClassTransformation transformation, MutableComponentModel componentModel)
    {
        if (fieldType.equals(COMPONENT_RESOURCES_CLASS_NAME))
        {
            String body = format("%s = %s;", fieldName, transformation.getResourcesFieldName());

            transformation
                    .extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, body);

            transformation.makeReadOnly(fieldName);

            return true;
        }

        return false;
    }
}
