// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.dom.Document;

/**
 * Responsible for injecting script and style links into the &lt;head&gt; element of the rendered
 * HTML document.
 */
public interface DocumentHeadBuilder
{
    /**
     * Adds a link to load a script. Scripts will be loaded only once.
     */
    void addScriptLink(String scriptURL);

    /**
     * Adds a link to load a CSS stylesheet. Stylesheets are loaded only once.
     *
     * @param styleURL URL of stylesheet to load
     * @param media    media value (or null to omit the media attribute)
     */
    void addStylesheetLink(String styleURL, String media);

    /**
     * Adds JavaScript code. The code is collected into a single block that is injected just before
     * the close body tag of the page.
     *
     * @param script statement to add to the block (a newline will be appended as well)
     */
    void addScript(String script);

    /**
     * Updates the supplied Document, locating the html/body element and adding script links (to the
     * top) and a script block (to the end).
     *
     * @param document to be updated
     */
    void updateDocument(Document document);
}
