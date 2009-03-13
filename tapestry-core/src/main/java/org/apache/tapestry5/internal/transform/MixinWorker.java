// Copyright 2006, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformConstants;

import java.util.List;

/**
 * Supports the {@link org.apache.tapestry5.annotations.Mixin} annotation, which allows a mixin to be part of the
 * implementation of a component. The annotation is applied to a field, which will become read-only, and contain a
 * reference to the mixin instance.
 */
public class MixinWorker implements ComponentClassTransformWorker
{
    private final ComponentClassResolver resolver;

    public MixinWorker(final ComponentClassResolver resolver)
    {
        this.resolver = resolver;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> fields = transformation.findFieldsWithAnnotation(Mixin.class);

        for (String fieldName : fields)
        {
            Mixin annotation = transformation.getFieldAnnotation(fieldName, Mixin.class);

            transformation.claimField(fieldName, annotation);
            
            String mixinType = annotation.value();

            String fieldType = transformation.getFieldType(fieldName);

            String mixinClassName = InternalUtils.isBlank(mixinType) ? fieldType : resolver
                    .resolveMixinTypeToClassName(mixinType);

            model.addMixinClassName(mixinClassName);

            transformation.makeReadOnly(fieldName);

            String body = String.format("%s = (%s) %s.getMixinByClassName(\"%s\");", fieldName, fieldType,
                                        transformation.getResourcesFieldName(), mixinClassName);

            transformation
                    .extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, body);
        }
    }
}
