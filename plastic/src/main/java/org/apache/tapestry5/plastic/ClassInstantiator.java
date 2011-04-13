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
 * The end result of a class transformation is a ClassInstantiator that can be used to
 * instantiate an instance of the transformed class.
 */
public interface ClassInstantiator<T>
{
    /**
     * Creates and returns a new instance of the transformed class.
     */
    T newInstance();

    /**
     * Returns a <em>new</em> instantiator that adds the indicated value to the
     * instance's {@link InstanceContext}.
     * 
     * @param valueType
     *            defines the type of value, and acts as a key to retrieve the value
     * @param instanceContextValue
     *            the non-null value stored
     * @throws AssertionError
     *             if instanceContextValue is null
     * @throws IllegalStateException
     *             if a value of the given value type has already been stored
     */
    <V> ClassInstantiator<T> with(Class<V> valueType, V instanceContextValue);
}
