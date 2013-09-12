// Copyright 2006-2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.dom.MarkupModel;
import org.apache.tapestry5.ioc.util.TimeInterval;
import org.apache.tapestry5.services.javascript.JavaScriptStack;

public final class InternalConstants
{
    /**
     * Init parameter used to identify the package from which application classes are loaded. Such
     * classes are in the
     * pages, components and mixins sub-packages.
     */
    public static final String TAPESTRY_APP_PACKAGE_PARAM = "tapestry.app-package";

    /**
     * Turns off loading of default modules (as driven by JAR file Manifest entries).
     */
    public static final String DISABLE_DEFAULT_MODULES_PARAM = "tapestry.disable-default-modules";

    /**
     * The name of the query parameter that stores the page activation context inside an action
     * request.
     */
    public static final String PAGE_CONTEXT_NAME = "t:ac";

    /**
     * Name of event triggered by Grid sub-components when an in-place Grid is updated.
     */
    public static final String GRID_INPLACE_UPDATE = "inplaceupdate";

    /**
     * The name of a query parameter that stores the containing page (used in action links when the
     * page containing the
     * component is not the same as the page that was rendering). The active page (the page which
     * initiated the render)
     * is encoded into the URL, and the containing page is tacked on as this query parameter.
     */
    public static final String CONTAINER_PAGE_NAME = "t:cp";

    public static final String MIXINS_SUBPACKAGE = "mixins";

    public static final String COMPONENTS_SUBPACKAGE = "components";

    public static final String PAGES_SUBPACKAGE = "pages";

    public static final String BASE_SUBPACKAGE = "base";

    /**
     * Used in some Ajax scenarios to set the content type for the response early, when the Page
     * instance (the authority
     * on content types) is known. The value is of type {@link org.apache.tapestry5.ContentType}.
     */
    public static final String CONTENT_TYPE_ATTRIBUTE_NAME = "content-type";

    public static final String CHARSET_CONTENT_TYPE_PARAMETER = "charset";

    /**
     * As above but to store the name of the page. Necessary for determining the correct
     * {@link MarkupModel} for the response.
     */
    public static final String PAGE_NAME_ATTRIBUTE_NAME = "page-name";

    /**
     * Required MIME type for JSON responses. If this MIME type is not used, the client-side
     * Prototype code will not
     * recognize the response as JSON, and the Ajax.Response.responseJSON property will be null.
     */
    public static final String JSON_MIME_TYPE = "application/json";

    /**
     * Request attribute key; if non-null, then automatic GZIP compression of response stream is
     * suppressed. This is
     * useful when the code opening the response stream wants to explicitly control whether GZIP
     * compression occurs or
     * not.
     *
     * @since 5.1.0.0
     */
    public static final String SUPPRESS_COMPRESSION = "tapestry.supress-compression";

    /**
     * Name of response header for content encoding.
     *
     * @since 5.1.0.0
     */
    public static final String CONTENT_ENCODING_HEADER = "Content-Encoding";

    /**
     * Response content encoding value indicating use of GZIP compression.
     *
     * @since 5.1.0.0
     */
    public static final String GZIP_CONTENT_ENCODING = "gzip";

    /**
     * Identifies the start of an expansion inside a template.
     */
    public static final String EXPANSION_START = "${";

    /**
     * Special prefix for parameters that are inherited from named parameters of their container.
     */
    public static final String INHERIT_BINDING_PREFIX = "inherit:";

    public static final long TEN_YEARS = new TimeInterval("10y").milliseconds();

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Name of the core {@link JavaScriptStack}, which supplies the basic JavaScript infrastructure
     * on the client.
     *
     * @since 5.2.0
     */
    public static final String CORE_STACK_NAME = "core";

    /**
     * Virtual folder name for the core library. The core library is special as any component not present in another
     * library (including the application library) is searched for inside core.
     *
     * @since 5.3
     */
    public static final String CORE_LIBRARY = "core";

    /**
     * The names of the standard controlled subpackages.
     *
     * @since 5.3
     */
    public static final String[] SUBPACKAGES =
            {PAGES_SUBPACKAGE, COMPONENTS_SUBPACKAGE, MIXINS_SUBPACKAGE, BASE_SUBPACKAGE};

    /**
     * The element name for a submit input element used to cancel the form (rather than
     * submit it normally).
     *
     * @since 5.3
     */
    public static final String CANCEL_NAME = "cancel";

    /**
     * Request attribute that can be set to bypass page activation.
     *
     * @see org.apache.tapestry5.internal.services.StreamPageContentResultProcessor
     * @see org.apache.tapestry5.internal.services.PageRenderRequestHandlerImpl
     * @since 5.4
     */
    public static final String BYPASS_ACTIVATION = "tapestry.bypass-page-activation";

    /**
     * Key inside the response that contains the partial page render keys that are used
     * to update the client.
     *
     * @since 5.4
     */
    public static final String PARTIAL_KEY = "_tapestry";

    /**
     * Request attribute, set to true once the active page (as identified in the incoming
     * component event or page render request) has been successfully loaded. This is very important
     * to the {@link org.apache.tapestry5.corelib.pages.ExceptionReport} page, which can possibly
     * fail (resulting in a servlet container 500 response) if the page can't be loaded (because
     * if the page can't be loaded, then a link to the page can't be created).
     *
     * @since 5.4
     */
    public static final String ACTIVE_PAGE_LOADED = "tapestry.active-page-loaded";
}
