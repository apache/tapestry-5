// Copyright 2007, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.transform.InjectionProvider2;

/**
 * A very late worker related to the {@link Inject} annotation that, when all other forms of injection have failed,
 * matches the field type to a service interface.
 */
public class ServiceInjectionProvider implements InjectionProvider2
{
    private final ObjectLocator locator;

    private final ComponentClassCache classCache;

    public ServiceInjectionProvider(ObjectLocator locator, ComponentClassCache classCache)
    {
        this.locator = locator;
        this.classCache = classCache;
    }

    public boolean provideInjection(PlasticField field, ObjectLocator locator, MutableComponentModel componentModel)
    {
        Class fieldType = classCache.forName(field.getTypeName());

        Object inject = this.locator.getService(fieldType);

        assert inject != null;

        field.inject(inject);

        // If we make it this far without an exception, then we were successful
        // and should claim the field.

        return true;
    }

}
