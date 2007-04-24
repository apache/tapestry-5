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

package org.apache.tapestry.internal.beaneditor;

import java.util.Arrays;
import java.util.List;

import org.apache.tapestry.AnnotationProvider;
import org.apache.tapestry.beaneditor.Validate;
import org.apache.tapestry.services.ValidationConstraintGenerator;

/**
 * Checks for the {@link Validate} annotation, and extracts its value to form the result.
 */
public class ValidateAnnotationConstraintGenerator implements ValidationConstraintGenerator
{

    public List<String> buildConstraints(Class propertyType, AnnotationProvider annotationProvider)
    {
        Validate annotation = annotationProvider.getAnnotation(Validate.class);

        if (annotation == null)
            return null;

        return Arrays.asList(annotation.value().split(","));
    }

}
