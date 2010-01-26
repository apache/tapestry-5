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
 * A method defined by (or created within) a {@link ClassTransformation}, allowing
 * for access and manipulation of the method.
 * <p>
 * The natural sorting order of TransformMethods is the same as {@link TransformMethodSignature}.
 * 
 * @since 5.2.0
 */
public interface TransformMethod extends AnnotationProvider, Comparable<TransformMethod>
{
    /**
     * @return the signature for the method, defining name, visibility, return type, parameter types and thrown
     *         exceptions
     */
    TransformMethodSignature getSignature();

    /**
     * Extends an existing method. The provided method body is inserted at the end of the existing
     * method (i.e. {@link javassist.CtBehavior#insertAfter(java.lang.String)}). To access or change
     * the return value, use the <code>$_</code> pseudo variable.
     * <p/>
     * The method may be declared in the class, or may be inherited from a super-class. For inherited methods, a method
     * body is added that first invokes the super implementation.
     * <p/>
     * The extended method is considered <em>new</em>. New methods <em>are not</em> scanned for removed fields, field
     * access changes, etc.
     * 
     * @param body
     *            the body of Javassist psuedo-code
     * @throws RuntimeException
     *             if the provided Javassist method body can not be compiled
     */
    void extend(String body);
}
