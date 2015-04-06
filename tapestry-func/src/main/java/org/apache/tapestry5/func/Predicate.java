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

package org.apache.tapestry5.func;

/**
 * Used when filtering a collection of objects of a given type; the predicate is passed
 * each object in turn, and returns true to include the object in the result collection.
 *
 * The {@link F} class includes a number of Predicate factory methods.
 *
 * This was converted from a abstract base class to an interface in 5.3.
 * 
 * @since 5.2.0
 * @see Flow#filter(Predicate)
 * @see Flow#remove(Predicate)
 */
public interface Predicate<T>
{
    /**
     * This method is overridden in subclasses to define which objects the Predicate will accept
     * and which it will reject.
     * 
     * @param element
     *            the element from the flow to be evaluated by the Predicate
     */
    boolean accept(T element);
}
