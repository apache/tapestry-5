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

package org.apache.tapestry.internal;

import org.apache.tapestry.services.Alias;

public final class InternalConstants
{
    /**
     * Init parameter used to identify the package from which application classes are loaded. Such classes are in the
     * pages, components and mixins sub-packages.
     */
    public static final String TAPESTRY_APP_PACKAGE_PARAM = "tapestry.app-package";

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
     * The name of a query parameter that stores the active page (used in action links when the page containing the
     * component is not the same as the page that was rendering).
     */
    public static final String ACTIVE_PAGE_NAME = "t:ap";


    public static final String OBJECT_RENDER_DIV_SECTION = "t-env-data-section";

    public static final String MIXINS_SUBPACKAGE = "mixins";
    public static final String COMPONENTS_SUBPACKAGE = "components";
    public static final String PAGES_SUBPACKAGE = "pages";
    public static final String BASE_SUBPACKAGE = "base";


    /**
     * Used in some Ajax scenarios to set the content type for the response early, when the Page instance (the authority
     * on content types) is known. The value is of type {@link org.apache.tapestry.ContentType}.
     */
    public static final String CONTENT_TYPE_ATTRIBUTE_NAME = "content-type";
    public static final String CHARSET_CONTENT_TYPE_PARAMETER = "charset";

    /**
     * Request attribute that stores a {@link org.apache.tapestry.internal.structure.Page} instance that will be
     * rendered as the {@linkplain org.apache.tapestry.TapestryConstants#SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS_SYMBOL
     * immediate mode response}.
     */
    public static final String IMMEDIATE_RESPONSE_PAGE_ATTRIBUTE = "tapestry.immediate-response-page";

    private InternalConstants()
    {
    }
}
