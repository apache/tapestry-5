// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.plastic;

import java.lang.annotation.Annotation;

/** Interface implemented by members that may hold annotations. */
public interface AnnotationAccess
{
    /** Checks to see if the target has an annotation of the given type. */
    <T extends Annotation> boolean hasAnnotation(Class<T> annotationType);

    /** Returns an instantiated annotation, or null if the target does not have the indicated annotation. */
    <T extends Annotation> T getAnnotation(Class<T> annotationType);
}
