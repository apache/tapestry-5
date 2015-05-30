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

package org.apache.tapestry5.services.compatibility;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Used globally to track what compatibility traits are enabled.  By default, in Tapestry 5.4,
 * all traits are enabled unless explicitly disabled. This behavior may change in Tapestry 5.5.
 *
 * The configuration allows traits to be explicitly enabled or disabled.
 *
 * @since 5.4
 */
@UsesMappedConfiguration(key = Trait.class, value = Boolean.class)
public interface Compatibility
{
    /**
     * Returns true if the indicated compatibility trait is enabled.
     *
     * @param trait
     * @return true if enabled, false otherwise
     */
    boolean enabled(Trait trait);
}
