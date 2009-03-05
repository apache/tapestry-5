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

package org.apache.tapestry5.internal.beaneditor;

import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.services.ValidationConstraintGenerator;

import java.util.Arrays;
import java.util.List;

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

        //TAP5-520: Commas within regular expressions like {n,m} or {n,} or a\,b .
        //We use Negative Lookahead to avoid matching the case a\,b .
        //We use Positive Lookahead to avoid matching cases {n,m} and {n,}.
        //http://www.regular-expressions.info/lookaround.html
        return Arrays.asList(annotation.value().split("(?<!\\\\),(?!([0-9]*\\}))"));
    }

}
