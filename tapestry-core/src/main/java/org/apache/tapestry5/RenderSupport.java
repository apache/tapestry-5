// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.AssetSource;

/**
 * Provides support to all components that render. This is primarily about generating unique client-side ids (very
 * important for JavaScript generation) as well as accumulating JavaScript to be sent to the client. PageRenderSupport
 * also allows for the incremental addition of stylesheets.
 * <p/>
 * When rendering, a &lt;script&gt; block will be added to the bottom of the page (just before the &lt;/body&gt; tag).
 * The scripting statements added to this block will be executed, on the client, only once the page has fully loaded.
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
     * Adds some number of script links as strings representations of URLs.  The scripts are passed down to the client
     * as-is.  Typically, this is used to reference a script stored outside the web application entirely.
     *
     * @param scriptURLs URL strings of scripts
     */
    void addScriptLink(String... scriptURLs);

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
     * added this way will be ordered before any other content, in the &lt;head&gt; element of the document. The
     * &lt;head&gt; element will be created, if necessary.
     *
     * @param stylesheet the asset referencing the stylesheet
     * @param media      the media value for the stylesheet, or null to not specify a specific media type
     */

    void addStylesheetLink(Asset stylesheet, String media);

    /**
     * Adds a stylesheet as a URL. See notes in {@link #addScriptLink(String[])}.
     *
     * @param stylesheetURL URL string of stylesheet
     * @param media         media value fo the stylesheet, or null to not specify a specific media type
     */
    void addStylesheetLink(String stylesheetURL, String media);

    /**
     * Adds a script statement to the page's script block. A newline will be added after the script statement.
     *
     * @param script text to be added to the script block
     */
    void addScript(String script);

    /**
     * Adds a script statement to the page's script block.  The parameters are passed to {@link String#format(String,
     * Object[])} before being added to the script block.  A newline will be added after the formatted statement.
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

    /**
     * Invoked to set focus on a rendered field. Takes into account priority, meaning that a field with errors will take
     * precendence over a merely required field, and over a field that is optional.  The value {@link
     * org.apache.tapestry5.FieldFocusPriority#OVERRIDE} can be used to force a particular field to receive focus.
     *
     * @param priority focus is set only if the provided priority is greater than the current priority
     * @param fieldId  id of client-side element to take focus
     */
    void autofocus(FieldFocusPriority priority, String fieldId);
}
