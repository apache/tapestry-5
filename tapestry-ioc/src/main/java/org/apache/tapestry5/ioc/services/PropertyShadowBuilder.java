// Copyright 2006 The Apache Software Foundation
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

/**
 * Creates a "shadow" of a property of an object. The shadow has the same type as the property, and delegates all method
 * invocations to the property. Each method invocation on the shadow re-acquires the value of the property from the
 * underlying object and delegates to the current value of the property.
 * <p/>
 * Typically, the object in question is another service, one with the "perthread" service lifecycle. This allows a
 * global singleton to shadow a value that is specific to the current thread (and therefore, the current request).
 */
public interface PropertyShadowBuilder
{
    /**
     * @param <T>
     * @param source       the object from which a property will be extracted
     * @param propertyName the name of a property of the object, which must be readable
     * @param propertyType the expected type of the property, the actual property type must be assignable to this type
     * @return the shadow
     */
    <T> T build(Object source, String propertyName, Class<T> propertyType);
}
