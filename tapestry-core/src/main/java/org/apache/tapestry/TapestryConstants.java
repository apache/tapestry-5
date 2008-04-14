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

package org.apache.tapestry;

/**
 * Collection of common constant values used throughout Tapestry.
 */
public final class TapestryConstants
{
    /**
     * Default client event name, "action", used in most situations.
     */
    public static final String ACTION_EVENT = "action";

    /**
     * Event triggered when a page is activated (for rendering). The component event handler will be passed the context
     * provided by the passivate event.
     */
    public static final String ACTIVATE_EVENT = "activate";

    /**
     * Event triggered when a link for a page is generated. The event handler for the page may provide an object, or an
     * array of objects, as the context for the page. These values will become part of the page's context, and will be
     * provided back when the page is activated.
     */
    public static final String PASSIVATE_EVENT = "passivate";

    /**
     * Request path prefix that identifies an internal (on the classpath) asset.
     */
    public static final String ASSET_PATH_PREFIX = "/assets/";

    /**
     * Binding expression prefix used for literal strings.
     */
    public static final String LITERAL_BINDING_PREFIX = "literal";

    /**
     * Binding expression prefix used to bind to a property of the component.
     */
    public static final String PROP_BINDING_PREFIX = "prop";

    /**
     * Meta data key applied to pages that sets the response content type. A factory default provides the value
     * "text/html" when not overridden.
     */
    public static final String RESPONSE_CONTENT_TYPE = "tapestry.response-content-type";

    /**
     * Meta data key applied to pages that may only be accessed via secure methods (HTTPS).
     */
    public static final String SECURE_PAGE = "tapestry.secure-page";

    /**
     * Meta data key applied to pages that sets the response encoding. A factory default provides the value "UTF-8" when
     * not overriden. Content type may also be specified in the {@link #RESPONSE_CONTENT_TYPE content type} as parameter
     * "charset", i.e., "text/html;charset=UTF-8".
     */
    public static final String RESPONSE_ENCODING = "tapestry.response-encoding";

    /**
     * Indicates whether Tapestry is running in production mode or developer mode.  The primary difference is how
     * exceptions are reported.
     */
    public static final String PRODUCTION_MODE_SYMBOL = "tapestry.production-mode";

    /**
     * CSS class name that causes a rendered element to be invisible on the client side.
     */
    public static final String INVISIBLE_CLASS = "t-invisible";
    /**
     * All purpose CSS class name for anything related to Tapestry errors.
     */
    public static final String ERROR_CLASS = "t-error";


    /**
     * Symbol which may be set to "true" to force the use of absolute URIs (not relative URIs) exclusively.
     */
    public static final String FORCE_ABSOLUTE_URIS_SYMBOL = "tapestry.force-absolute-uris";


    /**
     * If set to true, then action requests will render a page markup response immediately, rather than sending a
     * redirect to render the response.
     */
    public static final String SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS_SYMBOL = "tapestry.suppress-redirect-from-action-requests";

    /**
     * The list of locales supported by the application; locales identified in the incoming request are "narrowed" to
     * one of these values.
     */
    public static final String SUPPORTED_LOCALES_SYMBOL = "tapestry.supported-locales";

    /**
     * Controls whether whitespace is compressed by default in templates, or left as is. The factory default is to
     * compress whitespace. This can be overridden using the xml:space attribute inside template elements.
     */
    public static final String COMPRESS_WHITESPACE_SYMBOL = "tapestry.compress-whitespace";

    /**
     * Time interval defining how often Tapestry will check for updates to local files (including classes). This number
     * can be raised in a production environment.
     */
    public static final String FILE_CHECK_INTERVAL_SYMBOL = "tapestry.file-check-interval";

    /**
     * Time interval that sets how long Tapestry will wait to obtain the exclusive lock needed to check local files.
     */
    public static final String FILE_CHECK_UPDATE_TIMEOUT_SYMBOL = "tapestry.file-check-update-timeout";

    /**
     * The page field persistence strategy that stores data in the session until the next request.
     */
    public static final String FLASH_PERSISTENCE_STRATEGY = "flash";
}
