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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.annotations.Retain;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;

/**
 * Identifies fields with the {@link org.apache.tapestry5.annotations.Retain} annotation, and "claims" them so that no
 * special work will occur on them.
 */
public final class RetainWorker implements ComponentClassTransformWorker
{
    /**
     * Claims each field with the {@link org.apache.tapestry5.annotations.Retain} annotation, claiming it using the
     * annotation as the tag.
     */
    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (String fieldName : transformation.findFieldsWithAnnotation(Retain.class))
        {
            Retain annotation = transformation.getFieldAnnotation(fieldName, Retain.class);

            transformation.claimField(fieldName, annotation);
        }
    }

}
