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

import java.util.List;

import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;
import org.apache.tapestry5.services.javascript.EsModuleManager.EsModuleManagerContribution;

/**
 * Responsible for managing access to the ES modules. This service's distributed
 * configuration allows the initial import map JSON object to be customized
 * in a webapp-wide basis.
 *
 * @since 5.10.0
 * @see EsModuleConfigurationCallback
 */
@UsesOrderedConfiguration(EsModuleManagerContribution.class)
public interface EsModuleManager
{
    /**
     * Invoked by the internal {@link org.apache.tapestry5.internal.services.DocumentLinker} service to 
     * write the import map into the page.
     *
     * @param head
     *         {@code <body>} element of the page, to which new {@code <script>} element(s) may be added.
     * @param moduleConfigurationCallbacks
     *         a list of {@link org.apache.tapestry5.services.javascript.ModuleConfigurationCallback}s, which
     *         is used to customize the configuration before it is written.
     */
    void writeImportMap(Element head,
                            List<EsModuleConfigurationCallback> moduleConfigurationCallbacks);

    /**
     * Invoked by the internal {@link org.apache.tapestry5.internal.services.DocumentLinker} service to write the 
     * ES module imports (as per {@link JavaScriptSupport#importEsModule(String)} into the page. 
     * this occurs after the ES module infrastructure
     * has been written into the page, along with the core libraries.
     *
     * @param root
     *         {@code <root>} element of the page.
     * @param inits
     *         specify initialization on the page, based on loading modules, extacting functions from modules, and invoking those functions
     */
    void writeImports(Element root, List<EsModuleInitialization> inits);
    
    /**
     * Encapsulates a contribution to {@linkplain EsModuleManager}.
     *
     * @since 5.10.0
     * @see EsModuleManager
     * @see EsModuleConfigurationCallback
     */
    public final class EsModuleManagerContribution
    {
        private final EsModuleConfigurationCallback callback;
        
        private final boolean isBase;

        EsModuleManagerContribution(EsModuleConfigurationCallback callback, boolean isBase) 
        {
            super();
            this.callback = callback;
            this.isBase = isBase;
        }
        
        /**
         * Creates a base contribution (one that contributes a callback used 
         * when creating the base import map to be used for all requests).
         * @param callback an {@linkplain EsModuleConfigurationCallback} instance.
         * @return a corresponding {@linkplain EsModuleManagerContribution}.
         */
        public static EsModuleManagerContribution base(EsModuleConfigurationCallback callback) 
        {
            return new EsModuleManagerContribution(callback, true);
        }

        /**
         * Creates a global per-request contribution (one that contributes a callback used 
         * in all requests after the callbacks added through 
         * {@linkplain JavaScriptSupport#addEsModuleConfigurationCallback(EsModuleConfigurationCallback)} 
         * were called).
         * @param callback an {@linkplain EsModuleConfigurationCallback} instance.
         * @return a corresponding {@linkplain EsModuleManagerContribution}.
         */
        public static EsModuleManagerContribution globalPerRequest(EsModuleConfigurationCallback callback) 
        {
            return new EsModuleManagerContribution(callback, false);
        }
        
        public EsModuleConfigurationCallback getCallback() {
            return callback;
        }
        
        public boolean isBase() {
            return isBase;
        }

    }

}
