// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.AnnotationProvider;

/**
 * A field defined by (or created within) a {@link ClassTransformation},
 * allowing the details of the field to be
 * accessed or modified.
 * 
 * @since 5.2.0
 */
public interface TransformField extends AnnotationProvider
{
    /**
     * Returns the name of the field.
     */
    String getName();

    /**
     * Returns the field's type, either a primitive name
     * or a fully qualified class name, or an array type name
     * (in Java source syntax).
     */
    String getType();

    /**
     * True if the field is a primitive type, not an object type.
     * Array types are object types.
     */
    boolean isPrimitive();

    /**
     * Claims the field so as to ensure that only a single annotation is applied to any single field.
     * When a transformation occurs (driven by a field annotation), the field is claimed (using the
     * annotation object as the tag). If a field has multiple conflicting annotations, this will be discovered when
     * the code attempts to claim the field a second time.
     * 
     * @param tag
     *            a non-null object that represents why the field is being tagged (this is typically
     *            a specific annotation on the field)
     * @throws IllegalStateException
     *             if the field is already claimed for some other tag
     */
    void claim(Object tag);
}
