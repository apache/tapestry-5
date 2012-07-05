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

import java.util.List;

/**
 * Used to define a <a href="http://requirejs.org/docs/api.html#config-shim">module shim</a>, used to adapt non-AMD JavaScript libraries
 * to operate like proper modules.  This information is used to build up a list of dependencies for the contributed JavaScript module,
 * and to identify the resource to be streamed to the client.
 * <p/>
 * Instances of this class are contributed to the {@link ModuleManager} service;  the contribution key is the module name
 * (typically, a single word).
 * <p/>
 * Tapestry contributes a single module, {@code _} for the <a href="http://underscore.js.org">Underscore</a> JavaScript library.
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
    public final List<String> dependencies;

    /**
     * Optional (but desirable) value exported by the shim module.
     */
    public final String exports;

    public ShimModule(Resource resource, List<String> dependencies, String exports)
    {
        assert resource != null;

        this.resource = resource;
        this.dependencies = dependencies;
        this.exports = exports;
    }
}
