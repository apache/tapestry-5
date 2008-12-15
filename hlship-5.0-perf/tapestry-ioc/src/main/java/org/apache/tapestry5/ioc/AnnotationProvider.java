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

package org.apache.tapestry5.ioc;

import java.lang.annotation.Annotation;

/**
 * A source of annotations. This interface is used to mask where the annotations come from (for example, from a Method,
 * a Class, or some other source).
 */
public interface AnnotationProvider
{
    /**
     * Searches for the specified annotation, returning the matching annotation instance.
     *
     * @param <T>
     * @param annotationClass used to select the annotation to return«
     * @return the annotation, or null if not found
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);
}
