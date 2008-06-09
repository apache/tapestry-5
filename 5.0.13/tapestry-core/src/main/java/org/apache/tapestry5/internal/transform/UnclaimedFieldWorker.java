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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import static org.apache.tapestry5.services.TransformConstants.CONTAINING_PAGE_DID_DETACH_SIGNATURE;
import static org.apache.tapestry5.services.TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Designed to be just about the last worker in the pipeline. Its job is to add cleanup code that restores transient
 * fields back to their initial (null) value. Fields that have been previously {@link
 * org.apache.tapestry5.services.ClassTransformation#claimField(String, Object) claimed} are ignored, as are fields that
 * are final.
 */
public final class UnclaimedFieldWorker implements ComponentClassTransformWorker
{

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> fieldNames = transformation.findUnclaimedFields();

        for (String fieldName : fieldNames)
        {
            transformField(fieldName, transformation);
        }
    }

    private void transformField(String fieldName, ClassTransformation transformation)
    {
        int modifiers = transformation.getFieldModifiers(fieldName);

        if (Modifier.isFinal(modifiers))
            return;

        String type = transformation.getFieldType(fieldName);

        String defaultFieldName = transformation.addField(Modifier.PRIVATE, type, fieldName
                + "_default");

        transformation.extendMethod(CONTAINING_PAGE_DID_LOAD_SIGNATURE, defaultFieldName + " = "
                + fieldName + ";");

        // At the end of the request, we want to move the default value back over the
        // active field value. This will most often be null.

        transformation.extendMethod(CONTAINING_PAGE_DID_DETACH_SIGNATURE, fieldName + " = "
                + defaultFieldName + ";");
    }
}
