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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.internal.util.VirtualResource;

/**
 * Used to wrap plain JavaScript libraries as ES modules. The underlying
 * resource is transformed before it is sent to the client.
 *
 * @since 5.10.0
 */
public class EsShim {

    /**
     * The underlying resource, usually a JavaScript library
     */
    private final Resource resource;

    /**
     * The modules that this module requires, the keys being module names and
     * the values being the respective parameter names for the module's factory
     * function.
     */
    private final Map<String, String> importConfig = new LinkedHashMap<String, String>();

    public EsShim(final Resource resource) {
        this.resource = resource;
    }

    /**
     * Adds a dependency on another module. 
     * 
     * @param moduleName
     *            the name of the required module, e.g. <code>jQuery</code>
     * @param variableName
     *            the module's corresponding variable name
     * @return this ESWrapper for further configuration
     */
    public EsShim importModule(final String moduleName,
            final String variableName) {
        importConfig.put(moduleName, variableName);
        return this;
    }

    /**
     * Adds a dependency on another module. The module will be loaded but not
     * passed to a variable. This is useful for dependencies on other
     * modules that do not actually return a value.
     *
     * @param moduleName
     *            the name of the required module, e.g.
     *            <code>bootstrap/transition</code>
     * @return this ESWrapper for further configuration
     */
    public EsShim importModule(final String moduleName) {
        importConfig.put(moduleName, null);
        return this;
    }

    /**
     * Returns a virtual resource representing this wrapper.
     * @return a {@linkplain Resource}.
     */
    public Resource getResource() {
        return new ESModuleWrapperResource(resource, importConfig);
    }

    /**
     * A virtual resource that wraps a plain JavaScript library as an AMD
     * module.
     *
     */
    private final static class ESModuleWrapperResource extends VirtualResource 
    {
        private final Resource resource;
        private final Map<String, String> importConfig;

        public ESModuleWrapperResource(final Resource resource,
                final Map<String, String> importConfig) 
        {
            this.resource = resource;
            this.importConfig = importConfig;
        }

        @Override
        public InputStream openStream() throws IOException 
        {
            StringBuilder sb = new StringBuilder();
            
            for (String module : importConfig.keySet())
            {
                final String variableName = importConfig.get(module);
                sb.append("import ");
                if (variableName != null) 
                {
                    sb.append(variableName);
                    sb.append(" from ");
                }
                sb.append("\"");
                sb.append(module);
                sb.append("\";\n");
            }
            
            return new SequenceInputStream(toInputStream(sb), resource.openStream());
        }

        @Override
        public String getFile() {
            return "generated-module-for-" + resource.getFile();
        }

        @Override
        public URL toURL() {
            return null;
        }

        @Override
        public String toString() {
            return "ES module wrapper for " + resource.toString();
        }

        private static InputStream toInputStream(final StringBuilder sb) 
        {
            return new ByteArrayInputStream(sb.toString().getBytes(UTF8));
        }
    }

}
