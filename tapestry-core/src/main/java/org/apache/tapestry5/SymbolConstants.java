// Copyright 2008, 2009 The Apache Software Foundation
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
     * If set to "true", then action requests will render a page markup response immediately, rather than sending a
     * redirect to render the response.  "Action request" is an outdated term for "component event request" (i.e., most
     * links and all form submissions).
     */
    public static final String SUPPRESS_REDIRECT_FROM_ACTION_REQUESTS = "tapestry.suppress-redirect-from-action-requests";

    /**
     * The list of locales supported by the application; locales identified in the incoming request are "narrowed" to
     * one of these values. The first locale name in the list is the default locale used when no proper match can be
     * found.
     */
    public static final String SUPPORTED_LOCALES = "tapestry.supported-locales";

    /**
     * Controls whether whitespace is compressed by default in templates, or left as is. The factory default is to
     * compress whitespace. This can be overridden using the xml:space attribute inside template elements.
     */
    public static final String COMPRESS_WHITESPACE = "tapestry.compress-whitespace";

    /**
     * Time interval defining how often Tapestry will check for updates to local files (including classes). This number
     * can be raised in a production environment. The default is "1 s" (one second), which is appropriate for
     * development.
     */
    public static final String FILE_CHECK_INTERVAL = "tapestry.file-check-interval";

    /**
     * Time interval that sets how long Tapestry will wait to obtain the exclusive lock needed to check local files. The
     * default is "50 ms".
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
     * The  charset used when rendering page markup; the charset is also used as the request encoding when handling
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
     *
     * @deprecated since 5.1.0.1; scripts are now always at the top (see TAP5-544)
     */
    public static final String SCRIPTS_AT_TOP = "tapestry.script-at-top";

    /**
     * Identifies the default persistence strategy for all pages that do not provide an override (using this value as
     * {@link org.apache.tapestry5.annotations.Meta key}).
     *
     * @since 5.1.0.0
     */
    public static final String PERSISTENCE_STRATEGY = "tapestry.persistence-strategy";


    /**
     * Minimum output stream size, in bytes, before output is compressed using GZIP. Shorter streams are not compressed.
     * Tapestry buffers this amount and switches to a GZIP output stream as needed. The default is "100".
     *
     * @see #GZIP_COMPRESSION_ENABLED
     * @since 5.1.0.0
     */
    public static final String MIN_GZIP_SIZE = "tapestry.min-gzip-size";

    /**
     * Version number integrated into URLs for context assets. This should be changed for each release, otherwise
     * out-of-date files may be used from the client's local cache (due to far-future expired headers). The default
     * value is semi-random and different for each execution, which will adversely affect client caching, but is reasonable
     * for development.
     *
     * @since 5.1.0.0
     */
    public static final String APPLICATION_VERSION = "tapestry.application-version";

    /**
     * Used to omit the normal Tapestry framework generator meta tag. The meta tag is rendered by default, but clients
     * who do not wish to advertise their use of Tapestry may set this symbol to "true".
     *
     * @since 5.1.0.0
     */
    public static final String OMIT_GENERATOR_META = "tapestry.omit-generator-meta";

    /**
     * If "true" (the default) then GZip compression is enabled for dynamic requests and for static assets. If you are
     * using a server that handles GZip compression for you, or you don't want to ue the extra processing power
     * necessary to GZIP requests, then override this to "false".
     *
     * @see #MIN_GZIP_SIZE
     * @since 5.1.0.0
     */
    public static final String GZIP_COMPRESSION_ENABLED = "tapestry.gzip-compression-enabled";

    /**
     * If "true" (which itself defaults to production mode), then the {@link org.apache.tapestry5.annotations.Secure}
     * annotation will be honored.  If "false" (i.e., development mode), then the annotation and related HTTP/HTTPS
     * logic is ignored.
     *
     * @since 5.1.0.1
     */
    public static final String SECURE_ENABLED = "tapestry.secure-enabled";

    /**
     * If "true" (the default), then the {@link org.apache.tapestry5.services.PersistentLocale} will be encoded into the
     * {@link org.apache.tapestry5.Link} path by the {@link org.apache.tapestry5.services.ComponentEventLinkEncoder}
     * service. If overriden to "false" this does not occur, but you should provide a {@link
     * org.apache.tapestry5.services.LinkCreationListener} (registered with the {@link
     * org.apache.tapestry5.services.LinkCreationHub}) in order to add the locale as a query parameter (or provide some
     * alternate means of persisting the locale between requests).
     *
     * @since 5.1.0.1
     */
    public static final String ENCODE_LOCALE_INTO_PATH = "tapestry.encode-locale-into-path";

    /**
     * If "true" then JavaScript files will be combined into a single virtual JavaScript file. Defaults to "true" is
     * production mode.
     *
     * @since 5.1.0.2
     */
    public static final String COMBINE_SCRIPTS = "tapestry.combine-scripts";

}
