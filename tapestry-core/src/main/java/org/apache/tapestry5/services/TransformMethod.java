// Copyright 2010, 2011 The Apache Software Foundation
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

import java.lang.annotation.Annotation;

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

    /** Returns just the name of the method. */
    String getName();

    /**
     * Returns an object that can be used to invoke the method on an instance of the component class (regardless
     * of the actual visibility of the method).
     */
    MethodAccess getAccess();

    /**
     * Add advice for the method; the advice will be threaded into method invocations of the indicated method.
     * A method may be given multiple advice; each advice will receive control in turn (assuming
     * the previous advice invokes {@link ComponentMethodInvocation#proceed()}) in the order the advice
     * is added. The last advice will proceed to the original method implementation.
     * 
     * @param advice
     *            to receive control when the method is invoked
     * @see #addOperationAfter(ComponentInstanceOperation)
     * @see #addOperationBefore(ComponentInstanceOperation)
     */
    void addAdvice(ComponentMethodAdvice advice);

    /**
     * Adds an operation that will execute before any
     * further advice or operations. This is converted into
     * advice that invokes the operation, then invokes {@link ComponentMethodInvocation#proceed()}.
     */
    void addOperationBefore(ComponentInstanceOperation operation);

    /**
     * Adds an operation that will execute after any
     * further advice or operations. This is converted into
     * advice that invokes {@link ComponentMethodInvocation#proceed()} before invoking the operation.
     */
    void addOperationAfter(ComponentInstanceOperation operation);

    /**
     * Converts a signature to a string used to identify the method; this consists of the
     * {@link TransformMethodSignature#getMediumDescription()} appended with source file information
     * and line number
     * information (when available).
     * 
     * @return a string that identifies the class, method name, types of parameters, source file and
     *         source line number
     */
    String getMethodIdentifier();

    /**
     * Returns true if the method is an override of a method from the parent class.
     * 
     * @return true if the parent class contains a method with the name signature
     */
    boolean isOverride();

    /**
     * Gets an annotation on a parameter of the method.
     * 
     * @param index
     *            index of parameter
     * @param annotationType
     *            type of annotation to check for
     * @return the annotation, if found, or null
     */
    <A extends Annotation> A getParameterAnnotation(int index, Class<A> annotationType);
}
