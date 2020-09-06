// Copyright 2010, 2011 The Apache Software Foundation
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

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.transform.InjectionProvider2;

import javax.inject.Named;

/**
 * Processes the combination of {@link javax.inject.Inject} and {@link javax.inject.Named} annotations.
 *
 * @since 5.3
 */
public class InjectNamedProvider implements InjectionProvider2
{
    private final ObjectLocator locator;

    private final ComponentClassCache cache;

    public InjectNamedProvider(ObjectLocator locator, ComponentClassCache cache)
    {
        this.locator = locator;
        this.cache = cache;
    }

    public boolean provideInjection(PlasticField field, ObjectLocator locator, MutableComponentModel componentModel)
    {
        if (!field.hasAnnotation(Named.class))
        {
            return false;
        }

        Named annotation = field.getAnnotation(Named.class);

        Class fieldType = cache.forName(field.getTypeName());

        Object service = this.locator.getService(annotation.value(), fieldType);

        field.inject(service);

        return true;
    }
}
