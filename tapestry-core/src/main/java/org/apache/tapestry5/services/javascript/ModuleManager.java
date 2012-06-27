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

import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.Resource;

/**
 * Responsible for managing access to the JavaScript modules.
 *
 * @since 5.4
 */
public interface ModuleManager
{
    /**
     * Invoked by the internal {@link org.apache.tapestry5.internal.services.DocumentLinker} service to write the configuration
     * of the module system into the page. This is the necessary initialization of the client-side {@code require} object, including
     * (critically) its baseUrl property.
     *
     * @param scriptElement
     *         {@code <script>} element to write configuration should be written (using {@link Element#raw(String)}
     */
    void writeConfiguration(Element scriptElement);

    /**
     * Given a module name (which may be a path of names separated by slashes), locates the corresponding {@link Resource}.
     *
     * @param moduleName
     *         name of module to locate
     * @return corresponding resource, or null if not found
     */
    Resource findResourceForModule(String moduleName);
}
