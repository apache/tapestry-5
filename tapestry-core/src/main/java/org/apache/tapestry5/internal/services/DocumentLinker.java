// Copyright 2007, 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.StylesheetLink;

/**
 * Responsible for injecting script and style links into the &lt;head&gt; and &lt;body&gt; element of the rendered HTML
 * document.
 */
public interface DocumentLinker
{
    /**
     * Adds a link to load a JavaScript library. . The &lt;script&gt; elements will be added inside
     * the document's &lt;head&gt;.
     */
    void addScriptLink(String scriptURL);

    /**
     * Adds a link to load a CSS stylesheet.
     */
    void addStylesheetLink(StylesheetLink stylesheet);

    /**
     * Adds JavaScript code. The code is collected into a single block that is injected just before the close body tag
     * of the page (in a full page render) and collected as the "script" property of the partial page render response.
     * The JavaScript is executed after the page loads (or in an Ajax update, after external JavaScript libraries are
     * loaded and the DOM is updated).
     * <p>
     * This method may be called multiple times for the same priority and the script will be accumulated.
     * 
     * @param priority
     *            when to execute the provided script
     * @param script
     *            statement to add to the block (a newline will be appended as well)
     */
    void addScript(InitializationPriority priority, String script);

    /**
     * Adds a call to the Tapestry.init() function. This may be called multiple times and the init() calls will occur
     * in order. In a normal page render, the init() calls will be added to the main JavaScript block, but in a partial
     * page render Ajax response, the initialization will be property "init" of the partial page render response.
     * <p>
     * This method should only be invoked at most once per priority.
     * 
     * @since 5.2.0
     */
    void setInitialization(InitializationPriority priority, JSONObject initialization);
}
