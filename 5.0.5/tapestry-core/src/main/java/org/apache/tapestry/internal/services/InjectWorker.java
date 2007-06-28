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

package org.apache.tapestry.internal.services;

import java.lang.annotation.Annotation;

import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;

/**
 * Worker for the {@link org.apache.tapestry.annotations.Inject} annotation that delegates out to
 * the master {@link ObjectProvider} to access the value. This worker must be scheduled after
 * certain other workers, such as {@link InjectBlockWorker} (which is keyed off a combination of
 * type and the Inject annotation).
 * 
 * @see ObjectProvider
 */
public class InjectWorker implements ComponentClassTransformWorker
{
    private final ObjectProvider _objectProvider;

    private final ObjectLocator _locator;

    public InjectWorker(ObjectProvider objectProvider, ObjectLocator locator)
    {
        _objectProvider = objectProvider;
        _locator = locator;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (String fieldName : transformation.findFieldsWithAnnotation(Inject.class))
        {
            Inject annotation = transformation.getFieldAnnotation(fieldName, Inject.class);

            inject(fieldName, transformation, model);

            transformation.claimField(fieldName, annotation);
        }

    }

    @SuppressWarnings("unchecked")
    private void inject(final String fieldName, final ClassTransformation transformation,
            MutableComponentModel model)
    {
        String fieldType = transformation.getFieldType(fieldName);

        Class type = transformation.toClass(fieldType);

        AnnotationProvider annotationProvider = new AnnotationProvider()
        {
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return transformation.getFieldAnnotation(fieldName, annotationClass);
            }
        };

        Object inject = null;

        try
        {
            inject = _objectProvider.provide(type, annotationProvider, _locator);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ServicesMessages.fieldInjectionError(transformation
                    .getClassName(), fieldName, ex), ex);
        }

        transformation.injectField(fieldName, inject);
    }

}
