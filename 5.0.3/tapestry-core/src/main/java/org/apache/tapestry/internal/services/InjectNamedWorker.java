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
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;

/**
 * Worker for the {@link org.apache.tapestry.annotations.Inject} annotation, but only works with
 * annotations where the annotation has a value (a name). In some cases, there are specific types
 * where the meaning of the value for the Inject annoation is different, works for those cases must
 * be scheduled <em>before</em> this worker, and must be sure to
 * {@link ClassTransformation#claimField(String, Object) claim the field}.
 * 
 * @see ObjectProvider
 */
public class InjectNamedWorker implements ComponentClassTransformWorker
{
    private final ObjectProvider _objectProvider;

    private final ServiceLocator _locator;

    public InjectNamedWorker(ObjectProvider objectProvider, ServiceLocator locator)
    {
        _objectProvider = objectProvider;
        _locator = locator;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (String fieldName : transformation.findFieldsWithAnnotation(Inject.class))
        {
            Inject annotation = transformation.getFieldAnnotation(fieldName, Inject.class);

            String value = annotation.value();

            // A later worker will tackle this.

            if (InternalUtils.isBlank(value))
                continue;

            injectNamed(fieldName, value, transformation, model);

            transformation.claimField(fieldName, annotation);
        }

    }

    @SuppressWarnings("unchecked")
    private void injectNamed(String fieldName, String value, ClassTransformation transformation,
            MutableComponentModel model)
    {
        String fieldType = transformation.getFieldType(fieldName);

        Class type = transformation.toClass(fieldType);

        Object inject = _objectProvider.provide(value, type, _locator);

        transformation.injectField(fieldName, inject);
    }

}
