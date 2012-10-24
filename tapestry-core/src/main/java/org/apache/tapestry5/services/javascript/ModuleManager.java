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
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;
import org.apache.tapestry5.json.JSONArray;

import java.util.List;

/**
 * Responsible for managing access to the JavaScript modules.
 * <p/>
 * The configuration of the service allows overrides of the default search path; the configuration keys
 * are module names, and the configuration values are the {@link ShimModule} definitions for those module names.
 * This is primarily used to wrap non-AMD compliant libraries for use with RequireJS (via contributed {@link ShimModule}s).
 *
 * @since 5.4
 */
@UsesMappedConfiguration(ShimModule.class)
public interface ModuleManager
{
    /**
     * Invoked by the internal {@link org.apache.tapestry5.internal.services.DocumentLinker} service to write the configuration
     * of the module system into the page, including the tag to load the RequireJS library, and the
     * necessary initialization of the client-side {@code require} object, including
     * (critically) its baseUrl property. In addition, a call to the client-side function {@code core/pageinit:loadScriptsAndInitialize}
     * is constructed to load static scripts and perform page initializations.
     *
     * @param body
     *         {@code <body>} element of the page, to which new {@code <script>>} element(s) will be added.
     * @param libraryURLs
     *         list of additional static JavaScript library URLs that must be loaded on the page, after the
     *         coreLibraryURLs, and before an initializations
     * @param immediateInits
 *         list of immediate initializations that occur as soon as the static JavaScript libraries are loaded
     * @param deferredInits
     */
    void writeInitialization(Element body, List<String> libraryURLs, List<JSONArray> immediateInits, List<JSONArray> deferredInits);

    /**
     * Given a module name (which may be a path of names separated by slashes), locates the corresponding {@link Resource}.
     * First checks for {@linkplain ShimModule contributed shim modules}, then searches for possible matches among the
     * {@linkplain org.apache.tapestry5.services.ComponentClassResolver#getLibraryNames() defined library names}.  As a special
     * case, the folder name "app" is mapped to the application's package.
     *
     * @param moduleName
     *         name of module to locate
     * @return corresponding resource, or null if not found
     */
    Resource findResourceForModule(String moduleName);
}
