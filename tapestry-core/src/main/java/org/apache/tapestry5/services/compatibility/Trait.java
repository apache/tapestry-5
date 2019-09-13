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

import org.apache.tapestry5.SymbolConstants;

/**
 * Defines different traits that may be enabled or disabled. This was introduced in Tapestry 5.4 to allow certain
 * features that exist in Tapestry 5.3 to be optionally enabled for compatibility.
 */
public enum Trait
{
    /**
     * Indicates that the Scriptaculous JavaScript libraries should be included.  Tapestry 5.3 includes options for performing
     * some kinds of animations when certain elements were updated or removed; that is no longer present in Tapestry 5.4
     * and Scriptaculous is not used. This trait is only used if the
     * {@linkplain SymbolConstants#JAVASCRIPT_INFRASTRUCTURE_PROVIDER JavaScript infrastructure provider}
     * is set to "prototype".
     */
    SCRIPTACULOUS,

    /**
     * Support for Tapestry 5.3 style initializers (the client-side {@code T5.initializers} namespace).
     */
    INITIALIZERS,
    
    /**
     * Indicates that Twitter Bootstrap 3 CSS and JavaScript should be included in all pages by default.
     * @see <a href="https://getbootstrap.com/">Bootstrap site</a>
     * @since 5.5
     */
    BOOTSTRAP_3,
    
    /**
     * Indicates that Twitter Bootstrap 4 CSS and JavaScript should be included in all pages by default.
     * @see <a href="https://getbootstrap.com/">Bootstrap site</a>
     * @since 5.5
     */
    BOOTSTRAP_4,
    
}
