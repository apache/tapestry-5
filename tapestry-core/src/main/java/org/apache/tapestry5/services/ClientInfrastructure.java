// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.services;

import org.apache.tapestry5.Asset;

import java.util.List;

/**
 * Client infrastructure is a base set of JavaScript libraries and CSS stylesheet files, The core JavaScript libraries
 * are added to any page that {@linkplain org.apache.tapestry5.RenderSupport#addScript(String, Object[]) adds JavaScript
 * to the page}. The CSS stylesheet files are added to any page with a root &lt;html&gt; element.
 * <p/>
 * Tapestry's default JavaScript stack includes Prototype, Scriptaculous, and a Tapestry-specific library.  Note that
 * these individual library files will {@linkplain org.apache.tapestry5.SymbolConstants#COMBINE_SCRIPTS be combined into
 * a single virtual resource} (from the client's point of view).
 * <p/>
 * Tapestry's default CSS stack contains the Tapestry default stylesheet, and the stylesheet used by Tapestry's
 * Blackbird console.
 * <p/>
 * Overriding the default ClientInfrastructure service gives an application complete freedom to replace any part of
 * Tapestry's default client-side resources.
 *
 * @since 5.1.0.2
 */
public interface ClientInfrastructure
{
    /**
     * Returns the (localized) assets for the scripts to be included as core JavaScript stack. The assets for the stack
     * will be added before any other JavaScript libraries included in the render of the page. Adding a library or any
     * initialization JavaScript triggers the inclusion of the JavaScript stack.
     *
     * @return list of assets
     */
    List<Asset> getJavascriptStack();

    /**
     * Returns the (localized) assets for CSS stylesheet files to be included on any page. These are ordered before any
     * stylesheets specifically included (to allow default rules to be easily overridden). The default core stack
     * includes the Tapestry default stylesheet, and an additional stylesheet for the Blackbird JavaScript console.
     *
     * @return list of assets
     */
    List<Asset> getStylesheetStack();
}
