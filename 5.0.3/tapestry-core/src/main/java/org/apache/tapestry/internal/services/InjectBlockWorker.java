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

import java.util.List;

import org.apache.tapestry.Block;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformConstants;

/**
 * Identifies fields of type {@link Block} that have the {@link Inject} annotation and converts them
 * into read-only fields containing the injected Block from the template. The annotation's value is
 * the id of the block to inject; if ommitted, the block id is deduced from the field id.
 * <p>
 * Must be scheduled before {@link InjectNamedWorker} because it uses the same annotation, Inject,
 * with a different interpretation.
 */
public class InjectBlockWorker implements ComponentClassTransformWorker
{
    static final String BLOCK_TYPE_NAME = Block.class.getName();

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> fieldNames = transformation.findFieldsOfType(BLOCK_TYPE_NAME);

        if (fieldNames.isEmpty())
            return;

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        int count = 0;

        String resourcesFieldName = transformation.getResourcesFieldName();

        for (String fieldName : fieldNames)
        {
            Inject annotation = transformation.getFieldAnnotation(fieldName, Inject.class);

            if (annotation == null)
                continue;

            String blockId = getBlockId(fieldName, annotation);

            builder.addln("%s = %s.getBlock(\"%s\");", fieldName, resourcesFieldName, blockId);

            transformation.makeReadOnly(fieldName);
            transformation.claimField(fieldName, annotation);

            count++;
        }

        // Fields yes, but no annotations, so nothing to really do.

        if (count == 0)
            return;

        builder.end();

        transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, builder
                .toString());
    }

    private String getBlockId(String fieldName, Inject annotation)
    {
        String annotationId = annotation.value();

        if (InternalUtils.isNonBlank(annotationId))
            return annotationId;

        return InternalUtils.stripMemberPrefix(fieldName);
    }

}
