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

import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.InjectionProvider;

/**
 * Performs anonymous injection, for the cases where a field is labled with the {@link Inject}
 * annotation, but no specific value was provided. This worker must be scheduled <em>after</em>
 * {@link InjectNamedWorker}.
 * <p>
 * The implementation of this worker mostly delegates to a chain of command of
 * {@link InjectionProvider}s.
 */
public class InjectAnonymousWorker implements ComponentClassTransformWorker
{

    private final ServiceLocator _locator;

    // Really, a chain of command

    private final InjectionProvider _injectionProvider;

    public InjectAnonymousWorker(final ServiceLocator locator,
            final InjectionProvider injectionProvider)
    {
        _locator = locator;
        _injectionProvider = injectionProvider;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (String fieldName : transformation.findFieldsWithAnnotation(Inject.class))
        {
            Inject annotation = transformation.getFieldAnnotation(fieldName, Inject.class);

            String fieldType = transformation.getFieldType(fieldName);

            boolean result = _injectionProvider.provideInjection(
                    fieldName,
                    fieldType,
                    _locator,
                    transformation,
                    model);

            if (!result)
                throw new RuntimeException(ServicesMessages.noInjectionFound(transformation
                        .getClassName(), fieldName, fieldType));

            transformation.claimField(fieldName, annotation);
        }
    }

}
