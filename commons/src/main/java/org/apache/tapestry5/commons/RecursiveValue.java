// Licensed to the Apache License, Version 2.0 (the "License");
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

package org.apache.tapestry5.commons;

import java.util.List;

/**
 * <p>
 * Interface that represents a parent-children relationship. It's used
 * in the Tapestry's <code>Recursive</code> component.
 * </p>
 * <p>
 * This was contributed by <a href="https://www.pubfactory.com">KGL PubFactory</a>.
 * </p>
 * @since 5.9.0
 */
public interface RecursiveValue<T> 
{

    /**
     * Returns the list of children for a given value.
     * @return a {@link java.util.List}.
     */
    List<RecursiveValue<?>> getChildren();
    
    /**
     * Returns the original object related to this value.
     * @return an {@link Object}
     */
    T getValue();
    
}