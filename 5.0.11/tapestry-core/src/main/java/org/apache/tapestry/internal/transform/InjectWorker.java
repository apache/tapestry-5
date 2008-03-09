// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.transform;

import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.InjectionProvider;

/**
 * Performs injection triggered by any field annotated with the {@link org.apache.tapestry.ioc.annotations.Inject}
 * annotation.
 * <p/>
 * The implementation of this worker mostly delegates to a chain of command of {@link
 * org.apache.tapestry.services.InjectionProvider}s.
 */
public class InjectWorker implements ComponentClassTransformWorker
{
    private final ObjectLocator _locator;

    // Really, a chain of command

    private final InjectionProvider _injectionProvider;

    public InjectWorker(ObjectLocator locator, InjectionProvider injectionProvider)
    {
        _locator = locator;
        _injectionProvider = injectionProvider;
    }

    public final void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (String fieldName : transformation.findFieldsWithAnnotation(Inject.class))
        {
            Inject annotation = transformation.getFieldAnnotation(fieldName, Inject.class);

            try
            {
                String fieldType = transformation.getFieldType(fieldName);

                Class type = transformation.toClass(fieldType);

                boolean success = _injectionProvider.provideInjection(
                        fieldName,
                        type,
                        _locator,
                        transformation,
                        model);

                if (success) transformation.claimField(fieldName, annotation);
            }
            catch (RuntimeException ex)
            {
                throw new RuntimeException(TransformMessages.fieldInjectionError(transformation
                        .getClassName(), fieldName, ex), ex);
            }

        }
    }
}
