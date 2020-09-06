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

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

import java.util.List;

/**
 * Responsible for managing access to the JavaScript modules.
 *
 * The configuration of the service allows overrides of the default search path; the configuration keys
 * are module names, and the configuration values are the {@link JavaScriptModuleConfiguration} definitions for those module names.
 * This is primarily used to wrap non-AMD compliant libraries for use with RequireJS (via contributed {@link JavaScriptModuleConfiguration}s).
 *
 * @since 5.4
 * @see ModuleConfigurationCallback
 * @see AMDWrapper
 */
@UsesMappedConfiguration(JavaScriptModuleConfiguration.class)
public interface ModuleManager
{
    /**
     * Invoked by the internal {@link org.apache.tapestry5.internal.services.DocumentLinker} service to write the configuration
     * for the module system into the page.
     *
     * @param body
     *         {@code <body>} element of the page, to which new {@code <script>} element(s) will be added.
     * @param moduleConfigurationCallbacks
     *         a list of {@link org.apache.tapestry5.services.javascript.ModuleConfigurationCallback}s, which
     *         is used to customize the configuration before it is written.
     */
    void writeConfiguration(Element body,
                            List<ModuleConfigurationCallback> moduleConfigurationCallbacks);

    /**
     * Invoked by the internal {@link org.apache.tapestry5.internal.services.DocumentLinker} service to write the initializations
     * (as per {@link JavaScriptSupport#require(String)} into the page; this occurs after the module infrastructure
     * has been written into the page, along with the core libraries.
     *
     * @param body
     *         {@code <body>} element of the page, to which new {@code <script>} element(s) will be added.
     * @param libraryURLs
     *         additional libraries that should be dynamically loaded before evaluating the inits
     * @param inits
     *         specify initialization on the page, based on loading modules, extacting functions from modules, and invoking those functions
     */
    void writeInitialization(Element body, List<String> libraryURLs, List<?> inits);

    /**
     * Given a module name (which may be a path of names separated by slashes), locates the corresponding {@link Resource}.
     * First checks for {@linkplain JavaScriptModuleConfiguration contributed shim modules}, then searches for possible matches among the
     * {@linkplain org.apache.tapestry5.services.ComponentClassResolver#getLibraryNames() defined library names}.  As a special
     * case, the folder name "app" is mapped to the application's package.
     *
     * @param moduleName
     *         name of module to locate
     * @return corresponding resource, or null if not found
     */
    Resource findResourceForModule(String moduleName);

}
