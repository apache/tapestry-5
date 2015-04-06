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

package org.apache.tapestry5.services.javascript;

import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.services.AssetSource;

/**
 * Defines the types of extensions to a JavaScript stack that can be contributed to an extensible JavaScript stack.
 *
 * @see StackExtension
 * @see ExtensibleJavaScriptStack
 * @since 5.3
 */
public enum StackExtensionType
{
    /**
     * A JavaScript library. The extension value will be converted using {@link AssetSource#getExpandedAsset(String)},
     * meaning that {@linkplain SymbolSource symbols} will be expanded.
     *
     * @see JavaScriptSupport#importJavaScriptLibrary(org.apache.tapestry5.Asset)
     * @see org.apache.tapestry5.services.javascript.JavaScriptStack#getJavaScriptLibraries()
     */
    LIBRARY,

    /**
     * A dependency on another JavaScriptStack. Note that this is especially useful as, starting in 5.4, there is
     * no automatic dependency on the "core" stack, as there was in earlier versions.
     *
     * @see JavaScriptSupport#importStack(String)
     * @see org.apache.tapestry5.services.javascript.JavaScriptStack#getStylesheets()
     * @since 5.4
     */
    STACK,

    /**
     * A stylesheet. The extension value will be converted using {@link AssetSource#getExpandedAsset(String)},
     * meaning that {@linkplain SymbolSource symbols} will be expanded.
     *
     * @see JavaScriptSupport#importStylesheet(org.apache.tapestry5.Asset)
     * @see org.apache.tapestry5.services.javascript.JavaScriptStack#getStylesheets()
     */
    STYLESHEET,

    /**
     * A module to aggregate with the stack. The module's JavaScript is included after any libraries.
     * In development mode (with aggregation disabled), the library will be included individually.
     * Unlike the RequireJS {@code r.js} tool, this does not process
     * dependencies and is based on a simple regular expression parser.
     *
     * Note that this only loads the module's <em>code</em> and defines the module as available;
     * the module's function will not be invoked unless {@link JavaScriptSupport#require(String)} is invoked to establish
     * a dependency.
     *
     * Note that at this time, {@linkplain JavaScriptModuleConfiguration#exports(String) shimmed modules} can not
     * be aggregated into stacks properly; the shimmed module will be aggregated, but then will still be loaded via
     * a subsequent HTTP request.
     *
     * @since 5.4
     */
    MODULE,

    /**
     * Overrides the {@linkplain JavaScriptStack#getJavaScriptAggregationStrategy() JavaScript aggregation strategy}
     * for the stack. The value is string name of an {@link org.apache.tapestry5.services.javascript.JavaScriptAggregationStrategy} value.
     *
     * @since 5.4
     */
    AGGREGATION_STRATEGY,

    /**
     * Extra JavaScript initialization (rarely used). No symbol expansion takes place.
     *
     * @see JavaScriptSupport#addScript(String, Object...)
     * @see org.apache.tapestry5.services.javascript.JavaScriptStack#getInitialization()
     * @deprecated Deprecated in 5.4 with no replacement; initialization may be removed in the future.
     */
    INITIALIZATION;
}
