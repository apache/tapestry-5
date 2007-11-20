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

import org.apache.tapestry.annotations.Mixin;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformConstants;

import java.util.List;

/**
 * Supports the {@link Mixin} annotation, which allows a mixin to be part of the implementation of a
 * component. The annotation is applied to a field, which will become read-only, and contain a
 * reference to the mixin instance.
 */
public class MixinWorker implements ComponentClassTransformWorker
{
    private final ComponentClassResolver _resolver;

    public MixinWorker(final ComponentClassResolver resolver)
    {
        _resolver = resolver;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> fields = transformation.findFieldsWithAnnotation(Mixin.class);

        for (String fieldName : fields)
        {
            Mixin annotation = transformation.getFieldAnnotation(fieldName, Mixin.class);

            String mixinType = annotation.value();

            String fieldType = transformation.getFieldType(fieldName);

            String mixinClassName = InternalUtils.isBlank(mixinType) ? fieldType : _resolver
                    .resolveMixinTypeToClassName(mixinType);

            model.addMixinClassName(mixinClassName);

            transformation.makeReadOnly(fieldName);

            String body = String.format("%s = (%s) %s.getMixinByClassName(\"%s\");", fieldName, fieldType,
                                        transformation.getResourcesFieldName(), mixinClassName);

            transformation
                    .extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, body);

            transformation.claimField(fieldName, annotation);
        }
    }
}
