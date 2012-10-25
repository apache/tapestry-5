// Copyright 2012 The Apache Software Foundation
//
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

import org.apache.tapestry5.ioc.Resource;

import java.util.Arrays;
import java.util.List;

/**
 * Used to define a <a href="http://requirejs.org/docs/api.html#config-shim">module shim</a>, used to adapt non-AMD JavaScript libraries
 * to operate like proper modules.  This information is used to build up a list of dependencies for the contributed JavaScript module,
 * and to identify the resource to be streamed to the client.
 * <p/>
 * Instances of this class are contributed to the {@link ModuleManager} service;  the contribution key is the module name
 * (typically, a single word).
 *
 * @since 5.4
 */
public final class ShimModule
{
    /**
     * The resource for this shim module.
     */
    public final Resource resource;

    /**
     * The names of other shim modules that should be loaded before this shim module.
     */
    private List<String> dependencies;

    /**
     * Optional (but desirable) value exported by the shim module.
     */
    private String exports;

    private String initExpression;

    private boolean needsConfiguration;

    public ShimModule(Resource resource)
    {
        assert resource != null;

        this.resource = resource;
    }

    /**
     * A list of other module names the shim depends on.
     *
     * @param moduleNames
     * @return this ShimModule for further configuration
     */
    public ShimModule dependsOn(String... moduleNames)
    {
        assert moduleNames.length > 0;

        dependencies = Arrays.asList(moduleNames);

        needsConfiguration = true;

        return this;
    }

    public List<String> getDependencies()
    {
        return dependencies;
    }

    /**
     * The name of a global variable exported by the module. This will be the value injected into
     * modules that depend on the shim.
     *
     * @return this ShimModule for further configuration
     */
    public ShimModule exports(String exports)
    {
        assert exports != null;

        this.exports = exports;

        needsConfiguration = true;

        return this;
    }

    public String getExports()
    {
        return exports;
    }

    /**
     * Used as an alternative to {@linkplain #exports(String)}, this allows a short expression to be specified; the
     * expression is used to initialize, clean up, and (usually) return the module's export value. For Underscore, this
     * would be "_.noConflict()".  If the expression returns null, then the exports value is used.
     *
     * @param expression
     *         initialization expression
     * @return this ShimModule, for further configuration
     */
    public ShimModule initializeWith(String expression)
    {
        assert expression != null;

        this.initExpression = expression;

        needsConfiguration = true;

        return this;
    }

    public String getInitExpression()
    {
        return initExpression;
    }

    /**
     * Returns true if the module contains any additional configuration beyond its {@link Resource}.
     */
    public boolean getNeedsConfiguration()
    {
        return needsConfiguration;
    }
}
