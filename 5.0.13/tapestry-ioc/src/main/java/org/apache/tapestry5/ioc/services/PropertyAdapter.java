// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.AnnotationProvider;

import java.lang.reflect.Method;

/**
 * Provides access to a single property within a class. Acts as an {@link org.apache.tapestry5.ioc.AnnotationProvider};
 * when searching for annotations, the read method (if present) is checked first, followed by the write method, followed
 * by the underlying field (when the property name matches the field name).
 *
 * @see org.apache.tapestry5.ioc.services.ClassPropertyAdapter
 */
public interface PropertyAdapter extends AnnotationProvider
{
    /**
     * Returns the name of the property.
     */
    String getName();

    /**
     * Returns true if the property is readable (i.e., has a getter method).
     */
    boolean isRead();

    /**
     * Returns the method used to read the property, or null if the property is not readable.
     */
    public Method getReadMethod();

    /**
     * Returns true if the property is writeable (i.e., has a setter method).
     */
    boolean isUpdate();

    /**
     * Returns the method used to update the property, or null if the property is not writeable.
     */
    public Method getWriteMethod();

    /**
     * Reads the property value.
     *
     * @param instance to read from
     * @throws UnsupportedOperationException if the property is write only
     */
    Object get(Object instance);

    /**
     * Updates the property value. The provided value must not be null if the property type is primitive, and must
     * otherwise be of the proper type.
     *
     * @param instance to update
     * @param value    new value for the property
     * @throws UnsupportedOperationException if the property is read only
     */
    void set(Object instance, Object value);

    /**
     * Returns the type of the property.
     */
    Class getType();

    /**
     * Returns true if the return type of the read method is not the same as the property type. This can occur when the
     * property has been defined using generics, in which case, the method's type may be Object when the property type
     * is something more specific. This method is primarily used when generating runtime code related to the property.
     */
    boolean isCastRequired();


    /**
     * Returns the {@link org.apache.tapestry5.ioc.services.ClassPropertyAdapter} that provides access to other
     * properties defined by the same class.
     */
    ClassPropertyAdapter getClassAdapter();

    /**
     * Returns the type of bean to which this property belongs.  This is the same as {@link
     * org.apache.tapestry5.ioc.services.ClassPropertyAdapter#getBeanType()}.
     */
    Class getBeanType();
}
