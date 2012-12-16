// Copyright 2012 The Apache Software Foundation
//
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

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

/**
 * Used globally to track what compatibility traits are enabled.
 * <p/>
 * Temporarily, all traits are enabled.
 *
 * @since 5.4
 */
@UsesConfiguration(Trait.class)
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
