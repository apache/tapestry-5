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
// limitations under the License.package org.apache.tapestry5.internal.services;
package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.InternalSymbols;

import java.util.Set;

/**
 * Service providing methods related to names that shouldn't be used as form control element
 * names or ids.
 *
 * @see InternalSymbols#RESERVED_FORM_CONTROL_NAMES
 */
public interface FormControlNameManager
{
    /**
     * Returns the set of reserved form names (ones that shouldn't be used as HTML elements
     * client ids).
     *
     * @return a {@link Set} of {@link String}s.
     */
    Set<String> getReservedNames();

    /**
     * Tells whether a given name is reserved.
     *
     */
    boolean isReserved(String name);

}
