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
package org.apache.tapestry5.internal.services.ajax;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.services.javascript.AbstractInitialization;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Internal service to encapsulate the logic of calling 
 * {@linkplain JavaScriptSupport#require(String)} or 
 * {@linkplain JavaScriptSupport#importEsModule(String)}
 * depending on the value of {@linkplain SymbolConstants#REQUIRE_JS_ENABLED}.
 * Should only be used inside Tapestry itself. In user projects, 
 * explicit calls to the {@linkplain JavaScriptSupport} method should be used
 * instead, since they'll be using just one mode (i.e. Require.js enabled or not).
 * @since 5.10.0
 */
public interface RequireJsModeHelper
{

    /**
     * Returns either {@linkplain JavaScriptSupport#require(String)} or 
     * {@linkplain JavaScriptSupport#importEsModule(String)} depending on the
     * value of the {@linkplain SymbolConstants#REQUIRE_JS_ENABLED} configuration
     * symbol value.

     * @param moduleName the module name or id.
     * @return an {@linkplain AbstractInitialization} instance.
     */
    AbstractInitialization<?> importModule(String moduleName);
}