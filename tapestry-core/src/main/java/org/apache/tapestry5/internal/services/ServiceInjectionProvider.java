// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.InjectionProvider;

/**
 * A very late worker related to the {@link Inject} annotation that, when all other forms of injection have failed,
 * matches the field type to a service interface.
 */
public class ServiceInjectionProvider implements InjectionProvider
{
    private final ObjectLocator locator;

    public ServiceInjectionProvider(ObjectLocator locator)
    {
        this.locator = locator;
    }

    @SuppressWarnings("unchecked")
    public boolean provideInjection(String fieldName, Class fieldType, ObjectLocator locator,
                                    ClassTransformation transformation, MutableComponentModel componentModel)
    {
        Object inject = this.locator.getService(fieldType);

        assert inject != null;

        transformation.injectField(fieldName, inject);

        // If we make it this far without an exception, then we were successful
        // and should claim the field.

        return true;
    }

}
