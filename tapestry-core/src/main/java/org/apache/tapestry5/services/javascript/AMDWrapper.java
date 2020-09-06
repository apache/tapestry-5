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
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.util.VirtualResource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

/**
 * Used to wrap plain JavaScript libraries as AMD modules. The underlying
 * resource is transformed before it is sent to the client.
 *
 * This is an alternative to configuring RequireJS module shims for the
 * libraries. As opposed to shimmed libraries, the modules created using the
 * AMDWrapper can be added to JavaScript stacks.
 *
 * If the library depends on global variables, these can be added as module
 * dependencies. For a library that expects jQuery to be available as
 * <code>$</code>, the wrapper should be setup calling <code>require("jQuery", "$")</code>
 * on the respective wrapper.
 *
 * @since 5.4
 * @see JavaScriptModuleConfiguration
 * @see ModuleManager
 */
public class AMDWrapper {

    /**
     * The underlying resource, usually a JavaScript library
     */
    private final Resource resource;

    /**
     * The modules that this module requires, the keys being module names and
     * the values being the respective parameter names for the module's factory
     * function.
     */
    private final Map<String, String> requireConfig = new LinkedHashMap<String, String>();

    /**
     * The expression that determines what is returned from the factory function
     */
    private String returnExpression;

    public AMDWrapper(final Resource resource) {
        this.resource = resource;
    }

    /**
     * Add a dependency on another module. The module will be passed into the
     * generated factory function as a parameter.
     *
     * @param moduleName
     *            the name of the required module, e.g. <code>jQuery</code>
     * @param parameterName
     *            the module's corresponding parameter name of the factory
     *            function, e.g. <code>$</code>
     * @return this AMDWrapper for further configuration
     */
    public AMDWrapper require(final String moduleName,
            final String parameterName) {
        requireConfig.put(moduleName, parameterName);
        return this;
    }

    /**
     * Add a dependency on another module. The module will be loaded but not
     * passed to the factory function. This is useful for dependencies on other
     * modules that do not actually return a value.
     *
     * @param moduleName
     *            the name of the required module, e.g.
     *            <code>bootstrap/transition</code>
     * @return this AMDWrapper for further configuration
     */
    public AMDWrapper require(final String moduleName) {
        requireConfig.put(moduleName, null);
        return this;
    }

    /**
     * Optionally sets a return expression for this module. If the underlying
     * library creates a global variable, this is usually what is returned here.
     *
     * @param returnExpression
     *            the expression that is returned from this module (e.g.
     *            <code>Raphael</code>)
     * @return this AMDWrapper for further configuration
     */
    public AMDWrapper setReturnExpression(final String returnExpression) {
        this.returnExpression = returnExpression;
        return this;
    }

    /**
     * Return this wrapper instance as a {@link JavaScriptModuleConfiguration},
     * so it can be contributed to the {@link ModuleManager}'s configuration.
     * The resulting {@link JavaScriptModuleConfiguration} should not be
     * changed.
     *
     * @return a {@link JavaScriptModuleConfiguration} for this AMD wrapper
     */
    public JavaScriptModuleConfiguration asJavaScriptModuleConfiguration() {
        return new JavaScriptModuleConfiguration(transformResource());
    }

    private Resource transformResource() {
        return new AMDModuleWrapperResource(resource, requireConfig,
                returnExpression);
    }

    /**
     * A virtual resource that wraps a plain JavaScript library as an AMD
     * module.
     *
     */
    private final static class AMDModuleWrapperResource extends VirtualResource {
        private final Resource resource;
        private final Map<String, String> requireConfig;
        private final String returnExpression;

        public AMDModuleWrapperResource(final Resource resource,
                final Map<String, String> requireConfig,
                final String returnExpression) {
            this.resource = resource;
            this.requireConfig = requireConfig;
            this.returnExpression = returnExpression;

        }

        @Override
        public InputStream openStream() throws IOException {
            InputStream leaderStream;
            InputStream trailerStream;

            StringBuilder sb = new StringBuilder();

            // create a Flow of map entries (module name to factory function
            // parameter name)
            Flow<Entry<String, String>> requiredModulesToNames = F
                    .flow(requireConfig.entrySet());

            // some of the modules are not passed to the factory, sort them last
            Flow<Entry<String, String>> requiredModulesToNamesNamedFirst = requiredModulesToNames
                    .remove(VALUE_IS_NULL).concat(
                            requiredModulesToNames.filter(VALUE_IS_NULL));

            sb.append("define([");
            sb.append(InternalUtils.join(requiredModulesToNamesNamedFirst
                    .map(GET_KEY).map(QUOTE).toList()));
            sb.append("], function(");

            // append only the modules that should be passed to the factory
            // function, i.e. those whose map entry value is not null
            sb.append(InternalUtils.join(F.flow(requireConfig.values())
                    .filter(F.notNull()).toList()));
            sb.append("){\n");
            leaderStream = toInputStream(sb);
            sb.setLength(0);

            if (returnExpression != null)
            {
                sb.append("\nreturn ");
                sb.append(returnExpression);
                sb.append(';');
            }
            sb.append("\n});");
            trailerStream = toInputStream(sb);

            Vector<InputStream> v = new Vector<InputStream>(3);
            v.add(leaderStream);
            v.add(resource.openStream());
            v.add(trailerStream);

            return new SequenceInputStream(v.elements());
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
            return "AMD module wrapper for " + resource.toString();
        }

        private static InputStream toInputStream(final StringBuilder sb) {
            return new ByteArrayInputStream(sb.toString().getBytes(UTF8));

        }
    }

    private final static Mapper<Entry<String, String>, String> GET_KEY = new Mapper<Entry<String, String>, String>() {

        @Override
        public String map(final Entry<String, String> element) {
            return element.getKey();
        }

    };

    private final static Predicate<Entry<String, String>> VALUE_IS_NULL = new Predicate<Entry<String, String>>() {

        @Override
        public boolean accept(final Entry<String, String> element) {
            return element.getValue() == null;
        }

    };

    private final static Mapper<String, String> QUOTE = new Mapper<String, String>() {

        @Override
        public String map(final String element) {
            StringBuilder sb = new StringBuilder(element.length() + 2);
            sb.append('"');
            sb.append(element);
            sb.append('"');
            return sb.toString();
        }
    };

}
