// Copyright 2009 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;

import java.util.List;

/**
 * Processes the {@link org.apache.tapestry5.ioc.annotations.InjectService} annotation.
 *
 * @since 5.1.0.0
 */
public class InjectServiceWorker implements ComponentClassTransformWorker
{
    private final ObjectLocator locator;

    private final ComponentClassCache cache;

    public InjectServiceWorker(ObjectLocator locator, ComponentClassCache cache)
    {
        this.locator = locator;
        this.cache = cache;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> names = transformation.findFieldsWithAnnotation(InjectService.class);

        if (names.isEmpty()) return;

        for (String name : names)
        {
            InjectService annotation = transformation.getFieldAnnotation(name, InjectService.class);

            String typeName = transformation.getFieldType(name);

            Class fieldType = cache.forName(typeName);

            Object service = locator.getService(annotation.value(), fieldType);

            transformation.injectField(name, service);

            transformation.claimField(name, annotation);
        }
    }
}
