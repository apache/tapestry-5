// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.plastic;

/**
 * Allows read/write access directly to a field (bypassing accessors). Does not use reflection, even
 * if the field is private (the normal case for Plastic classes).
 */
public interface FieldHandle
{
    /**
     * Gets the current value of the field. If the field is a primitive value, then the primitive
     * will be wrapped.
     * 
     * @throws NullPointerException
     *             if the instance is null
     * @throws ClassCastException
     *             if the instance is not the type that contains the field
     */
    Object get(Object instance);

    /**
     * Updates the current value of the field. If the field is a primitive value, then the newValue
     * will be unwrapped automatically.
     * 
     * @throws NullPointerException
     *             if the instance is null
     * @throws NullPointerException
     *             if the newValue is null and the field is a primitive type
     * @throws ClassCastException
     *             if the instance is not the type that contains the field
     * @throws ClassCastException
     *             if the newValue is not assignable to the field type (or not the matching wrapper type
     *             for a primitive field)
     */
    void set(Object instance, Object newValue);
}
