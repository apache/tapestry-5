// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

/**
 * Provides support to all components that render. This is primarily about generating unique client-side ids (very
 * important for JavaScript generation) as well as accumulating JavaScript to be sent to the client. PageRenderSupport
 * also allows for the incremental addition of stylesheets.
 */
public interface RenderSupport
{
    /**
     * Allocates a unique id based on the component's id. In some cases, the return value will not precisely match the
     * input value (an underscore and a unique index value may be appended).
     *
     * @param id the component id from which a unique id will be generated
     * @return a unique id for this rendering of the page
     * @see org.apache.tapestry5.ioc.internal.util.IdAllocator
     */
    String allocateClientId(String id);

    /**
     * As with {@link #allocateClientId(String)} but uses the id of the component extracted from the resources.
     *
     * @param resources of the component which requires an id
     * @return a unique id for this rendering of the page
     */
    String allocateClientId(ComponentResources resources);

    /**
     * Adds one or more new script assets to the page. Assets are added uniquely, and appear as &lt;script&gt; elements
     * just inside the &lt;body&gt; element of the rendered page. Duplicate requests to add the same script are quietly
     * ignored.
     *
     * @param scriptAssets asset to the script to add
     */
    void addScriptLink(Asset... scriptAssets);

    /**
     * Used to add scripts that are stored on the classpath. Each element has {@linkplain SymbolSource symbols
     * expanded}, then is {@linkplain AssetSource converted to an asset} and added as a script link.
     *
     * @param classpaths array of paths. Symbols in the paths are expanded, then the paths are each converted into an
     *                   asset.
     */
    void addClasspathScriptLink(String... classpaths);

    /**
     * Adds a link to a CSS stylesheet. As with JavaScript libraries, each stylesheet is added at most once. Stylesheets
     * added this way will be ordered before any other content in the &lt;head&gt; element of the document. The
     * &lt;head&gt; element will be created, if necessary.
     *
     * @param stylesheet the asset referencing the stylesheet
     * @param media      the media value for the stylesheet, or null to not specify a specific media type
     */

    void addStylesheetLink(Asset stylesheet, String media);

    /**
     * Adds a script statement to the page's script block (which appears at the end of the page, just before the
     * &lt/body&gt; tag).
     *
     * @param format    base string format, to be passed through String.format
     * @param arguments additional arguments formatted to form the final script
     */
    void addScript(String format, Object... arguments);

    /**
     * Add an initialization call.
     *
     * @param functionName  the name of the function (on the client-side Tapestry.Initializer object) to invoke.
     * @param parameterList list of parameters for the method invocation.
     * @see #addScript(String, Object[])
     */
    void addInit(String functionName, JSONArray parameterList);

    /**
     * Alternate version of {@link #addInit(String, org.apache.tapestry5.json.JSONArray)} where just a single object is
     * passed.
     *
     * @param functionName the name of the function (on the client-side Tapestry object) to invoke.
     * @param parameter    the object to pass to the function
     */
    void addInit(String functionName, JSONObject parameter);

    /**
     * Alternate version of {@link #addInit(String, org.apache.tapestry5.json.JSONArray)} where one or more strings are
     * passed.  A single string is added to the initialization call as itself; otherwise, the parameters are combined to
     * form a {@link JSONArray}.
     *
     * @param functionName the name of the function (on the client-side Tapestry object) to invoke.
     * @param parameters
     */
    void addInit(String functionName, String... parameters);
}
