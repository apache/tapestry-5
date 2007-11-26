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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;

/**
 * Worker triggered by {@link Inject} annotation after all other injection related works have had a
 * chance. This implementation is just a wrapper around {@link ObjectLocator#getService(Class)}.
 */
public class DefaultInjectWorker implements ComponentClassTransformWorker
{
    private final ObjectLocator _locator;

    public DefaultInjectWorker(ObjectLocator locator)
    {
        _locator = locator;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (String fieldName : transformation.findFieldsWithAnnotation(Inject.class))
        {
            Inject annotation = transformation.getFieldAnnotation(fieldName, Inject.class);

            inject(fieldName, transformation);

            transformation.claimField(fieldName, annotation);
        }
    }

    @SuppressWarnings("unchecked")
    private void inject(String fieldName, ClassTransformation transformation)
    {
        String fieldType = transformation.getFieldType(fieldName);

        Class type = transformation.toClass(fieldType);

        try
        {
            Object inject = _locator.getService(type);

            transformation.injectField(fieldName, inject);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ServicesMessages.fieldInjectionError(transformation
                    .getClassName(), fieldName, ex), ex);
        }

    }

}
