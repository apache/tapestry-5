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

package org.apache.tapestry5.commons.internal;

import java.lang.annotation.Annotation;

import org.apache.tapestry5.commons.AnnotationProvider;

/**
 * A null implementation of {@link AnnotationProvider}, used when there is not appropriate source of annotations.
 */
public class NullAnnotationProvider implements AnnotationProvider
{
    /**
     * Always returns null.
     */
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        return null;
    }

}
