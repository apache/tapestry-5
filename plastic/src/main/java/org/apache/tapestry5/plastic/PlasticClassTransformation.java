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
 * A wrapper around a {@link PlasticClass} that allows the class to be converted into a concrete class, once all
 * transformations are complete.
 * 
 * @param <T> the type being generated.
 */
public interface PlasticClassTransformation<T>
{
    /**
     * Returns the PlasticClass being transformed.
     * 
     * @return PlasticClass instance
     */
    PlasticClass getPlasticClass();

    /**
     * Terminates the class transformation process, finishes any final bookkeeping, and
     * returns an object used to instantiate the transformed class. Once this method is invoked,
     * no other methods of the {@link PlasticClass} (or related objects) can be invoked.
     *
     * The returned ClassInstantiator has an empty {@link InstanceContext} map. Use
     * {@link ClassInstantiator#with(Class, Object)} to create a new ClassInstantiator with new InstanceContext entries.
     */
    ClassInstantiator<T> createInstantiator();
}
