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

package org.apache.tapestry5;

import org.apache.tapestry5.ioc.AnnotationProvider;

/**
 * Used to read or update the value associated with a property. A PropertyConduit provides access to the annotations on
 * the underlying getter and/or setter methods.
 */
public interface PropertyConduit extends AnnotationProvider
{
    /**
     * Reads the property from the instance.
     *
     * @param instance object containing the property
     * @return the current value of the property
     */
    Object get(Object instance);

    /**
     * Changes the current value of the property.
     *
     * @param instance object containing the property
     * @param value    to change the property to
     */
    void set(Object instance, Object value);

    /**
     * Returns the type of the property read or updated by the conduit.
     */
    Class getPropertyType();
}
