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

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Service that allows replacing one component, page or mixin class by another without changing the sources.
 * This service shouldn't be used directly: it's not an internal service just because it receives
 * contributions.
 * 
 * Contributions to it are mapped: the key is the component, page or mixin class to be
 * replaced, the value is the replacement.
 *
 * @since 5.4
 * @see org.apache.tapestry5.services.ComponentClassResolver
 */
@UsesMappedConfiguration(key = Class.class, value = Class.class)
public interface ComponentOverride
{

    /**
     * Returns true if the service configuration is non-empty.
     * 
     */
   boolean hasReplacements();
    
    /**
     * Returns the replacement for a class given its name.
     * @param className the fully qualified class name.
     * @return a {@link Class} or null.
     */
    Class getReplacement(String className);
    
}
