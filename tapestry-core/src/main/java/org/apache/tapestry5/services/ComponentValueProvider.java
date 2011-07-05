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

import org.apache.tapestry5.ComponentResources;

/**
 * An object used to provide a value of a specific type to a component (represented by an
 * instance of {@link ComponentResources}). The provider will create and return the value
 * (some providers may be smart enough to cache a value, but should be implemented in
 * a thread-safe manner). Often the provider is an inner class of a {@link ComponentClassTransformWorker}.
 * 
 * @param <T>
 *            type of object provided
 * @since 5.2.0
 * @deprecated Deprecated in 5.3, using Plastic equivalents
 * @see org.apache.tapestry5.plastic.PlasticField
 * @see org.apache.tapestry5.plastic.ComputedValue
 */
@SuppressWarnings("deprecation")
public interface ComponentValueProvider<T>
{
    /**
     * Provide the object for the indicated component.
     * 
     * @param resources
     *            Identifies the component
     * @return the object
     */
    T get(ComponentResources resources);
}
