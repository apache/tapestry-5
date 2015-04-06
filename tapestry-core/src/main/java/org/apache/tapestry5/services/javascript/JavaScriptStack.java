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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.services.AssetSource;

import java.util.List;

/**
 * A high level description of a group of related JavaScript libraries and stylesheets. The built-in "core"
 * stack is used to define the core JavaScript libraries needed by Tapestry (though this has been largely replaced by
 * JavaScript modules in Tapestry 5.4,
 * and the may be no libraries in the core stack by Tapestry 5.5). Other component libraries may
 * define additional stacks for related sets of resources, for example, to bundle together some portion
 * of the ExtJS or YUI libraries.
 *
 * The JavaScript assets of a stack may (when {@linkplain SymbolConstants#COMBINE_SCRIPTS enabled}) be exposed to the
 * client as a single URL (identifying the stack by name). The individual JavaScript  assets are combined into a single virtual
 * asset, which is then streamed to the client. The individual JavaScript libraries, or the combined virtual library,
 * may be {@linkplain SymbolConstants#MINIFICATION_ENABLED minimized} and the content (both compressed and
 * uncompressed) cached.
 *
 * Implementations may need to inject the {@link ThreadLocale} service in order to determine the current locale (if any
 * of the JavaScript library or stylesheet assets are localized). They will generally need to inject the
 * {@link AssetSource} service as well.
 *
 * The {@link ExtensibleJavaScriptStack} is the best way to create new stacks.
 *
 * @see ThreadLocale
 * @see org.apache.tapestry5.services.javascript.ExtensibleJavaScriptStack
 * @since 5.2.0
 */
public interface JavaScriptStack
{
    /**
     * Returns a list of JavaScriptStack names that this stack depends on. Each stack will be processed before
     * the current stack (thus a dependency stack's libraries, stylesheets and initialization is emitted before
     * the dependent stack).
     */
    List<String> getStacks();

    /**
     * Returns a list of <em>localized</em> assets for JavaScript libraries that form the stack.
     */
    List<Asset> getJavaScriptLibraries();

    /**
     * Returns a list of <em>localized</em> links for stylesheets that form the stack.
     */
    List<StylesheetLink> getStylesheets();

    /**
     * Returns a list of modules to include with the stack, when aggregating the stack's JavaScript.
     * It is still necessary to explicitly {@linkplain org.apache.tapestry5.services.javascript.JavaScriptSupport#require(String) require}
     * such modules.
     *
     * @see ModuleManager
     * @see org.apache.tapestry5.SymbolConstants#COMBINE_SCRIPTS
     * @since 5.4
     */
    List<String> getModules();

    /**
     * Identifies how to aggregate JavaScript within the stack.
     * The default is {@link org.apache.tapestry5.services.javascript.JavaScriptAggregationStrategy#COMBINE_AND_MINIMIZE}.
     *
     * @since 5.4
     */
    JavaScriptAggregationStrategy getJavaScriptAggregationStrategy();

    /**
     * Returns static JavaScript initialization for the stack. This block of JavaScript code will be added to the
     * page that imports the stack. The code executes outside of any other function (i.e., the code is not deferred
     * until the DOM is loaded). As with the other methods, if localization is a factor, the result of this method
     * should be localized.
     *
     * @deprecated Deprecated in Tapestry 5.4; may be removed in a future release. Implementations
     * may return null.
     */
    String getInitialization();
}
