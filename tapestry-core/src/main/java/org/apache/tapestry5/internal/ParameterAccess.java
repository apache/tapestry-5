// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.ioc.AnnotationProvider;

/**
 * A wrapper around a named parameter provided by {@link org.apache.tapestry5.internal.InternalComponentResources}. The
 * parameter may be bound or unbound.
 *
 * @since 5.0.19
 */
public interface ParameterAccess extends AnnotationProvider
{
    /**
     * Is the parameter bound?
     *
     * @return true if bound
     */
    boolean isBound();

    /**
     * Reads the current value of the parameter via the parameter's {@link org.apache.tapestry5.Binding}. This method is
     * intended for use from generated code (where it is easier to specify the type as a name than a Class instance).
     *
     * @param desiredTypeName the fully qualified name of the Java type to coerce the result to
     * @return the value of the parameter, or null if not bound
     * @throws NullPointerException if the parameter's value is null and null is not allowed
     */
    Object read(String desiredTypeName);


    /**
     * Reads the value of a parameter, via the parameter's {@link org.apache.tapestry5.Binding}.
     *
     * @param <T>
     * @param expectedType the expected type of parameter
     * @return the value for the parameter, or null if the parameter is not bound.
     */
    <T> T read(Class<T> expectedType);

    /**
     * Updates the parameter to a new value.  If the parameter is not bound, this method does nothing.
     *
     * @param parameterValue new value for parameter
     * @param <T>
     */
    <T> void write(T parameterValue);

    /**
     * Returns true if the binding is bound, and the binding is invariant.
     *
     * @return true if invariant
     * @see org.apache.tapestry5.Binding#isInvariant()
     */
    boolean isInvariant();

    /**
     * Returns the actual type of the bound parameter, or null if the parameter is not bound. This is primarily used
     * with property bindings, and is used to determine the actual type of the property, rather than the type of
     * parameter (remember that type coercion automatically occurs, which can mask significant differences between the
     * parameter type and the bound property type).
     *
     * @return the type of the bound parameter, or null if the parameter is not bound
     * @see org.apache.tapestry5.Binding#getBindingType()
     */
    Class getBoundType();
}
