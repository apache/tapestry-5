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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.services.Alias;

public final class InternalConstants
{
    /**
     * Init parameter used to identify the package from which application classes are loaded. Such classes are in the
     * pages, components and mixins sub-packages.
     */
    public static final String TAPESTRY_APP_PACKAGE_PARAM = "tapestry.app-package";

    /**
     * Turns off loading of default modules (as driven by JAR file Manifest entries).
     */
    public static final String DISABLE_DEFAULT_MODULES_PARAM = "tapestry.disable-default-modules";

    /**
     * The application mode, generally "servlet", used to select the correct contributions to the {@link Alias}
     * service.
     */
    public static final String TAPESTRY_ALIAS_MODE_SYMBOL = "tapestry.alias-mode";

    /**
     * The name of the application (i.e., the name of the application filter). Used, for example, to select additional
     * resources related to the application.
     */
    public static final String TAPESTRY_APP_NAME_SYMBOL = "tapestry.app-name";

    /**
     * The extension used for Tapestry component template files, <em>T</em>apestry <em>M</em>arkup <em>L</em>anguage.
     * Template files are well-formed XML files.
     */
    public static final String TEMPLATE_EXTENSION = "tml";

    /**
     * The name of the query parameter that stores the page activation context inside an action request.
     */
    public static final String PAGE_CONTEXT_NAME = "t:ac";

    /**
     * Name of event triggered by Grid sub-components when an in-place Grid is updated.
     */
    public static final String GRID_INPLACE_UPDATE = "inplaceupdate";

    /**
     * The name of a query parameter that stores the containing page (used in action links when the page containing the
     * component is not the same as the page that was rendering). The active page (the page which initiated the render)
     * is encoded into the URL, and the containing page is tacked on as this query parameter.
     */
    public static final String CONTAINER_PAGE_NAME = "t:cp";

    public static final String OBJECT_RENDER_DIV_SECTION = "t-env-data-section";

    public static final String MIXINS_SUBPACKAGE = "mixins";

    public static final String COMPONENTS_SUBPACKAGE = "components";

    public static final String PAGES_SUBPACKAGE = "pages";

    public static final String BASE_SUBPACKAGE = "base";

    /**
     * Used in some Ajax scenarios to set the content type for the response early, when the Page instance (the authority
     * on content types) is known. The value is of type {@link org.apache.tapestry5.ContentType}.
     */
    public static final String CONTENT_TYPE_ATTRIBUTE_NAME = "content-type";

    public static final String CHARSET_CONTENT_TYPE_PARAMETER = "charset";

    /**
     * Request attribute that stores a {@link org.apache.tapestry5.internal.structure.Page} instance that will be
     * rendered as the {@linkplain org.apache.tapestry5.SymbolConstants#SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS immediate
     * mode response}.
     */
    public static final String IMMEDIATE_RESPONSE_PAGE_ATTRIBUTE = "tapestry.immediate-response-page";

    /**
     * Request attribute that forces {@link org.apache.tapestry5.internal.services.RequestPathOptimizer} to use not
     * optimize URLs (this is necessitated by {@link org.apache.tapestry5.services.PageDocumentGenerator}). Any non-null
     * value will force the URLs to be non-optimized.
     */
    public static final String GENERATING_RENDERED_PAGE = "tapestry.generating-rendered-page";

    /**
     * Required MIME type for JSON responses. If this MIME type is not used, the client-side Prototype code will not
     * recognize the response as JSON, and the Ajax.Response.responseJSON property will be null.
     */
    public static final String JSON_MIME_TYPE = "application/json";
}
