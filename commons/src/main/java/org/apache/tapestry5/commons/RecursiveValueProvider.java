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

import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * <p>
 * Interface implemented by classes which converts objects to
 * {@link RecursiveValue} instances.
 * </p>
 * <p>
 * This was contributed by <a href="https://www.pubfactory.com">KGL PubFactory</a>.
 * </p>
 * @since 5.9.0
 */
@UsesOrderedConfiguration(RecursiveValueProvider.class)
public interface RecursiveValueProvider
{

    /**
     * Returns a {@link RecursiveValue} for this <code>object</code> or
     * returns <code>null</code> if the object isn't handled.
     * @param object an {@link Object}.
     * @return a {@link RecursiveValue} or <code>null</code>.
     */
    RecursiveValue<?> get(Object object);
    
}