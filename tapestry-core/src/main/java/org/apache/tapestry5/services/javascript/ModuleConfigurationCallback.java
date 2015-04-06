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

import org.apache.tapestry5.json.JSONObject;

/**
 * Used to change the configuration object which will be used to
 * <a href="http://requirejs.org/docs/api.html#config">configure RequireJS</a>; callbacks can modify
 * and override the configuration after it was created
 * by the {@link ModuleManager} service based on contributed {@link JavaScriptModuleConfiguration}s.
 * This allows components, pages, mixins and services to configure Require.JS dynamically in a
 * per-request basis by using the
 * {@link JavaScriptSupport#addModuleConfigurationCallback(ModuleConfigurationCallback)} method.
 *
 * Note that RequireJS is only configured during a full page render; on Ajax requests, RequireJS
 * will already be loaded and configured.
 *
 *
 * @see JavaScriptSupport#addModuleConfigurationCallback(ModuleConfigurationCallback)
 * @since 5.4
 */
public interface ModuleConfigurationCallback
{
    /**
     * Receives the current configuration, which can be copied or returned, or (more typically) modified and returned.
     *
     * @param configuration
     *         a {@link JSONObject} containing the current configuration.
     * @return a {@link JSONObject} containing the changed configuration, most probably the same
     *         one received as a parameter.
     */
    JSONObject configure(JSONObject configuration);
}
