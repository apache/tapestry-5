// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.InjectionProvider;
import org.apache.tapestry5.services.transform.InjectionProvider2;


/**
 * Converts the deprecated {@link InjectionProvider} to the new {@link InjectionProvider2}.
 *
 * @since 5.3
 */
public class InjectionProviderToInjectionProvider2 implements Coercion<InjectionProvider, InjectionProvider2>
{
    private final ComponentClassCache classCache;

    public InjectionProviderToInjectionProvider2(ComponentClassCache classCache)
    {
        this.classCache = classCache;
    }

    public InjectionProvider2 coerce(final InjectionProvider input)
    {
        return new InjectionProvider2()
        {
            public boolean provideInjection(PlasticField field, ObjectLocator locator, MutableComponentModel componentModel)
            {
                Class fieldType = classCache.forName(field.getTypeName());

                // We don't currently provide a TransformationSupport into the BridgeClassTransformation,
                // as it should not be needed.

                return input.provideInjection(field.getName(), fieldType, locator,
                        new BridgeClassTransformation(field.getPlasticClass(), null, componentModel), componentModel);
            }
        };
    }
}
