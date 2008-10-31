// Copyright 2008 The Apache Software Foundation
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
 * Defines the names of symbols used to configure Tapestry.
 *
 * @see org.apache.tapestry5.ioc.services.SymbolSource
 */
public class SymbolConstants
{
    /**
     * Indicates whether Tapestry is running in production mode or developer mode.  The primary difference is how
     * exceptions are reported.
     */
    public static final String PRODUCTION_MODE = "tapestry.production-mode";

    /**
     * Symbol which may be set to "true" to force the use of absolute URIs (not relative URIs) exclusively.
     */
    public static final String FORCE_ABSOLUTE_URIS = "tapestry.force-absolute-uris";

    /**
     * If set to true, then action requests will render a page markup response immediately, rather than sending a
     * redirect to render the response.
     */
    public static final String SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS = "tapestry.suppress-redirect-from-action-requests";

    /**
     * The list of locales supported by the application; locales identified in the incoming request are "narrowed" to
     * one of these values.
     */
    public static final String SUPPORTED_LOCALES = "tapestry.supported-locales";

    /**
     * Controls whether whitespace is compressed by default in templates, or left as is. The factory default is to
     * compress whitespace. This can be overridden using the xml:space attribute inside template elements.
     */
    public static final String COMPRESS_WHITESPACE = "tapestry.compress-whitespace";

    /**
     * Time interval defining how often Tapestry will check for updates to local files (including classes). This number
     * can be raised in a production environment.
     */
    public static final String FILE_CHECK_INTERVAL = "tapestry.file-check-interval";

    /**
     * Time interval that sets how long Tapestry will wait to obtain the exclusive lock needed to check local files.
     */
    public static final String FILE_CHECK_UPDATE_TIMEOUT = "tapestry.file-check-update-timeout";

    /**
     * The version number of the core Tapestry framework, or UNKNOWN if the version number is not available (which
     * should only occur when developing Tapestry).
     */
    public static final String TAPESTRY_VERSION = "tapestry.version";

    /**
     * The location of the application-wide component messages catalog, relative to the web application context. This
     * will normally be <code>WEB-INF/app.properties</code>.
     */
    public static final String APPLICATION_CATALOG = "tapestry.app-catalog";

    /**
     * The  charset used when rendering page markup; the charset is also used as ther request encoding when handling
     * incoming requests. The default is "UTF-8".
     */
    public static final String CHARSET = "tapestry.charset";

    /**
     * Used as the default for the Form's autofocus and clientValidation parameters. If overridden to "false", then
     * Forms will not (unless explicitly specified) use client validation or autofocus, which in turn, means that most
     * pages with Forms will not make use of the Tapestry JavaScript stack.
     */
    public static final String FORM_CLIENT_LOGIC_ENABLED = "tapestry.form-client-logic-enabled";

    /**
     * Name of page used to report exceptions; the page must implement {@link org.apache.tapestry5.services.ExceptionReporter}.
     * This is used by the default exception report handler service.
     */
    public static final String EXCEPTION_REPORT_PAGE = "tapestry.exception-report-page";

    /**
     * If true, then links for external JavaScript libraries are placed at the top of the document (just inside the
     * &lt;body&gt; element).  If false, the default, then the libraries are placed at the bottom of the document.
     * Per-page initialization always goes at the bottom.
     */
    public static final String SCRIPTS_AT_TOP = "tapestry.script-at-top";
}
