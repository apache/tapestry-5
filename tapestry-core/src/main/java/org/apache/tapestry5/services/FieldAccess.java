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

/**
 * Used when accessing the private instance variables of a component instance.
 * 
 * @see TransformField#getAccess()
 * @since 5.2.0
 */
public interface FieldAccess
{
    /**
     * Reads the value of the field of the provided instance.
     * 
     * @param instance
     *            object containing field to read
     */
    Object read(Object instance);

    /**
     * Updates the value of the field within the instance.
     * 
     * @param instance
     *            object containing field to update
     * @param value
     *            new value for field
     */
    void write(Object instance, Object value);
}
