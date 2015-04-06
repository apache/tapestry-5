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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.ModuleConfigurationCallback;
import org.apache.tapestry5.services.javascript.StylesheetLink;

/**
 * Responsible for injecting script and style links into the &lt;head&gt; and &lt;body&gt; element of the rendered HTML
 * document.
 *
 * @see org.apache.tapestry5.services.javascript.ModuleManager#writeInitialization(org.apache.tapestry5.dom.Element, java.util.List, java.util.List)
 * @since 5.4
 */
public interface DocumentLinker
{

    /**
     * Adds a link to load a non-core JavaScript library. These libraries are loaded, sequentially, only once
     * the core libraries have loaded and initialized. Thus difference between core libraries and other libraries
     * is new in 5.4, and represents a conflict between asynchronous loading of modules (introduced in 5.4) and
     * sequential loading of libraries (in 5.3 and earlier).
     */
    void addLibrary(String libraryURL);

    /**
     * A special case used only for the libraries that are part of the core stack, which itself contains RequireJS
     * and is used to bootstrap up to adding non-core libraries.
     *
     * @since 5.4
     */
    void addCoreLibrary(String libraryURL);

    /**
     * Adds a link to load a CSS stylesheet.
     */
    void addStylesheetLink(StylesheetLink stylesheet);
    
    /**
     * Adds a module configuration callback for this request.
     * 
     * @param callback a {@link ModuleConfigurationCallback}. It cannot be null.
     * @since 5.4
     */
    void addModuleConfigurationCallback(ModuleConfigurationCallback callback);

    /**
     * Adds JavaScript code. The code is collected into a single block that is injected just before the close body tag
     * of the page (in a full page render) and collected as the "script" property of the partial page render response.
     * The JavaScript is executed after the page loads (or in an Ajax update, after external JavaScript libraries are
     * loaded and the DOM is updated).
     *
     * This method may be called multiple times for the same priority and the script will be accumulated.
     *
     * @param priority
     *         when to execute the provided script
     * @param script
     *         statement to add to the block (a newline will be appended as well)
     */
    void addScript(InitializationPriority priority, String script);

    /**
     * Adds initialization, based on invoking functions exported by JavaScript modules.
     *
     * @param priority
     *         priority at which to perform initialization
     * @param moduleName
     *         name of module; the module exports a single function, or a map of functions
     * @param functionName
     *         name of function exported by module, or null (if the module exports a single function)
     * @param arguments
     *         arguments to pass to the function, or null if no arguments
     * @since 5.4
     */
    void addInitialization(InitializationPriority priority,
                           String moduleName,
                           String functionName,
                           JSONArray arguments);
}
