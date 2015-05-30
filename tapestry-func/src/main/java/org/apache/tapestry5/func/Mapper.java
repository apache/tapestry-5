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
 * Interface for operation {@link Flow#map(Mapper)} to define how Flow elements are mapped from one type
 * to another (or otherwise transformed).
 *
 * This changed in 5.3 from an abstract base class to an interface.
 * 
 * @since 5.2.0
 * @param <S>
 *            type of source flow
 * @param <T>
 *            type of target (output) flow
 */
public interface Mapper<S, T>
{
    /**
     * Implemented in subclasses to map an element from the source flow to an element of the target
     * flow.
     */
    T map(S element);
}
