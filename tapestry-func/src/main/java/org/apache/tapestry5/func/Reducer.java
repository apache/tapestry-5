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
 * A reducer takes an accumulator value and a single value from a collection and computes a new
 * accumulator value.
 *
 * @param A type of accumulator
 * @param T type of collection value
 * 
 * @since 5.2.0
 */
public interface Reducer<A, T>
{
    /**
     * Run a computation using the current value of the accumulator and an element (from a Flow),
     * and return the new accumulator.
     */
    A reduce(A accumulator, T element);
}
