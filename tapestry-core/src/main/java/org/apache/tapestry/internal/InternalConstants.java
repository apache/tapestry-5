// Copyright 2006, 2007 The Apache Software Foundation
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
     * Init parameter used to identify the package from which application classes are loaded. Such
     * classes are in the pages, components and mixins sub-packages.
     */
    public static final String TAPESTRY_APP_PACKAGE_PARAM = "tapestry.app-package";

    /**
     * The application mode, generally "servlet", used to select the correct contributions to the
     * {@link Alias} service.
     */
    public static final String TAPESTRY_ALIAS_MODE_SYMBOL = "tapestry.alias-mode";

    /**
     * The name of the application (i.e., the name of the application filter). Used, for example, to
     * select additional resources related to the application.
     */
    public static final String TAPESTRY_APP_NAME_SYMBOL = "tapestry.app-name";

    /**
     * The extension used for Tapestry component template files. Template files are well-formed XML
     * files. This is also used as the extension for page render requests (perhaps these will be
     * split into two concepts later, especially if the we designate a particular extension for
     * Tapestry template files, such as .tsp).
     */
    public static final String TEMPLATE_EXTENSION = "html";

    /** All purpose CSS class name for anything related to Tapestry errors. */
    public static final String TAPESTRY_ERROR_CLASS = "t-error";

    /**
     * The name of the query parameter that stores the page activation context inside an action
     * request.
     */
    public static final String PAGE_CONTEXT_NAME = "t:ac";

    public static final String OBJECT_RENDER_DIV_SECTION = "t-env-data-section";

    private InternalConstants()
    {
    }
}
