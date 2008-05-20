// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5;

import org.apache.tapestry5.ioc.AnnotationProvider;

/**
 * A binding is a connection between a component and its container (another component), that allows the embedded
 * component to gain access to <em>resources</em> defined by the container. Resources can represent any kind of value
 * that can be obtained from the parent component, but is often a JavaBean property that can be read and updated.
 * Different implementations of Binding as used to access different kinds of resources of the container.
 * <p/>
 * A binding ultimately must provide access to the underlying annotations. In most cases, there are no annotations, but
 * bindings that ultimate invoke methods or read and update fields must provide access to those annotations.
 */
public interface Binding extends AnnotationProvider
{
    /**
     * Reads the current value of the property (or other resource). When reading properties of objects that are
     * primitive types, this will return an instance of the wrapper type. In some cases, a binding is read only and this
     * method will throw a runtime exception.
     */
    Object get();

    /**
     * Updates the current value. Most types of bindings are read-only, and this method will throw a runtime exception.
     * It is the caller's responsibility to ensure that the value passed in is of the appropriate type.
     *
     * @param value
     */
    void set(Object value);

    /**
     * Returns true if the value of the binding does not ever change. Components will often cache such values
     * aggressively.
     */
    boolean isInvariant();

    /**
     * Returns the type of the binding, either the type of resource exposed by the binding, or the type of the property
     * bound.
     */
    Class getBindingType();
}
