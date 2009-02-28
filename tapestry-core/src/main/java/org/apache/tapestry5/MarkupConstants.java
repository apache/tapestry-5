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

package org.apache.tapestry5;

/**
 * Constants used when rendering page markup.
 *
 * @since 5.1.0.1
 */
public class MarkupConstants
{

    /**
     * Handler for the onclick event (for links) or other events (such as forms) to ensure the page is loaded before
     * allowing the click event to occur. Refrences a client-side JavaScript function that displays a standard "wait for
     * page to load" modal dialog.
     */
    public static final String WAIT_FOR_PAGE = "javascript:Tapestry.waitForPage(event);";

    /**
     * Name of attribute for intercepting the user clicking a link.
     *
     * @see #WAIT_FOR_PAGE
     */
    public static final String ONCLICK = "onclick";
}
