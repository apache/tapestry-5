// Copyright 2008, 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5;

import org.apache.tapestry5.internal.services.AssetDispatcher;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.javascript.JavaScriptStack;

/**
 * Defines the names of symbols used to configure Tapestry.
 * 
 * @see org.apache.tapestry5.ioc.services.SymbolSource
 */
public class SymbolConstants
{
    /**
     * Indicates whether Tapestry is running in production mode or developer mode. The primary difference is how
     * exceptions are reported.
     */
    public static final String PRODUCTION_MODE = "tapestry.production-mode";

    /**
     * A version of {@link #PRODUCTION_MODE} as a symbol reference. This can be used as the default value
     * of other symbols, to indicate that their default matches whatever PRODUCTION_MODE is set to, which is quite
     * common.
     * 
     * @since 5.2.0
     */
    public static final String PRODUCTION_MODE_VALUE = String.format("${%s}", PRODUCTION_MODE);

    /**
     * Symbol which may be set to "true" to force the use of absolute URIs (not relative URIs) exclusively.
     * 
     * @deprecated To be removed after Tapestry 5.2. URLs are now always absolute, since Tapestry 5.2.1.
     */
    public static final String FORCE_ABSOLUTE_URIS = "tapestry.force-absolute-uris";

    /**
     * If set to "true", then action requests will render a page markup response immediately, rather than sending a
     * redirect to render the response. "Action request" is an outdated term for "component event request" (i.e., most
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
     * The charset used when rendering page markup; the charset is also used as the request encoding when handling
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
     * Name of page used to report exceptions; the page must implement
     * {@link org.apache.tapestry5.services.ExceptionReporter}.
     * This is used by the default exception report handler service.
     */
    public static final String EXCEPTION_REPORT_PAGE = "tapestry.exception-report-page";

    /**
     * If true, then links for external JavaScript libraries are placed at the top of the document (just inside the
     * &lt;body&gt; element). If false, the default, then the libraries are placed at the bottom of the document.
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
     * Version number integrated into URLs for assets. This should be changed for each release, otherwise
     * out-of-date files may be used from the client's local cache (due to far-future expired headers). The default
     * value is semi-random and different for each execution, which will adversely affect client caching, but is
     * reasonable
     * for development.
     * 
     * @since 5.1.0.0
     * @see AssetDispatcher
     * @see AssetPathConstructor
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
     * annotation will be honored. If "false" (i.e., development mode), then the annotation and related HTTP/HTTPS
     * logic is ignored.
     * 
     * @since 5.1.0.1
     */
    public static final String SECURE_ENABLED = "tapestry.secure-enabled";

    /**
     * If "true" (the default), then the {@link org.apache.tapestry5.services.PersistentLocale} will be encoded into the
     * {@link org.apache.tapestry5.Link} path by the {@link org.apache.tapestry5.services.ComponentEventLinkEncoder}
     * service. If overridden to "false" this does not occur, but you should provide a
     * {@link org.apache.tapestry5.services.LinkCreationListener} (registered with the
     * {@link org.apache.tapestry5.services.LinkCreationHub}) in order to add the locale as a query parameter (or
     * provide some alternate means of persisting the locale between requests).
     * 
     * @since 5.1.0.1
     */
    public static final String ENCODE_LOCALE_INTO_PATH = "tapestry.encode-locale-into-path";

    /**
     * If "true" then JavaScript files in a {@link JavaScriptStack} will be combined into a single virtual JavaScript
     * file. Defaults to "true" in production mode.
     * 
     * @since 5.1.0.2
     */
    public static final String COMBINE_SCRIPTS = "tapestry.combine-scripts";

    /**
     * If "true" then Blackbird JavaScript console is enabled.
     * 
     * @since 5.2.0
     */
    public static final String BLACKBIRD_ENABLED = "tapestry.blackbird-enabled";

    /**
     * The default time interval that cookies created by Tapestry will be kept in the client web browser. The default is
     * "7 d" (that is, seven days).
     * 
     * @since 5.2.0
     */
    public static final String COOKIE_MAX_AGE = "tapestry.default-cookie-max-age";

    /**
     * The logical name of the start page, the page that is rendered for the root URL.
     * 
     * @since 5.2.0
     */
    public static final String START_PAGE_NAME = "tapestry.start-page-name";

    /**
     * The default stylesheet automatically injected into every rendered HTML page.
     * 
     * @since 5.2.0
     */
    public static final String DEFAULT_STYLESHEET = "tapestry.default-stylesheet";

    /**
     * The number of pages in the page pool (for a given page name / locale combination) before which Tapestry will
     * start to wait for existing pages to be made available.
     * Under this limit of pages, Tapestry will simply create a new page instance if no existing instance is readily
     * available.
     * Once the soft limit is reached, Tapestry will wait a short period of time (the soft wait interval) to see if an
     * existing page
     * instance is made available. It will then create a new page instance (unless the hard limit has been reached).
     * The default is 5 page instances. Remember that page pooling is done separately for each page (and localization of
     * the page).
     * 
     * @since 5.2.0
     */
    public static final String PAGE_POOL_SOFT_LIMIT = "tapestry.page-pool.soft-limit";

    /**
     * The absolute maximum number of page instances (for a particular page name / locale combination) that Tapestry
     * will create at any time.
     * If this number is reached, then requests will fail because a page instance is not available ... this can happen
     * as part of a denial of service attack.
     * For this value to have any meaning, it should be lower than the number of threads that the servlet container is
     * configured to use when processing requests.
     * The default is 20 page instances.
     * 
     * @deprecated The hard limit will be removed in a later release of Tapestry, as the maximum number of instance
     *             is easily controlled by limiting the number of request handling threads in the servlet container.
     * @since 5.2.0
     */
    public static final String PAGE_POOL_HARD_LIMIT = "tapestry.page-pool.hard-limit";

    /**
     * The time interval that Tapestry will wait for a page instance to become available before deciding whether to
     * create an entirely new page instance.
     * The default is "10 ms".
     * 
     * @since 5.2.0
     */
    public static final String PAGE_POOL_SOFT_WAIT = "tapestry.page-pool.soft-wait";

    /**
     * The time interval that an instantiated page instance may be cached before being removed. As pages are returned to
     * the pool, they are time stamped.
     * Periodically (as per the file check interval), the pool is scanned for page instances that have not been used
     * recently; those that are outside the
     * active window are discarded. This is used to free up unnecessary page instances after a request surge. The
     * default is "10 m" (10 minutes).
     * 
     * @since 5.2.0
     */
    public static final String PAGE_POOL_ACTIVE_WINDOW = "tapestry.page-pool.active-window";

    /**
     * The Asset path to the embedded copy of script.aculo.us packaged with Tapestry.
     * 
     * @since 5.2.0
     */
    public static final String SCRIPTACULOUS = "tapestry.scriptaculous";

    /**
     * The Asset path to the embedded datepicker.
     * 
     * @since 5.2.0
     */
    public static final String DATEPICKER = "tapestry.datepicker";

    /**
     * The Asset path to the embedded copy of blackbird packaged with Tapestry.
     * 
     * @since 5.2.0
     */
    public static final String BLACKBIRD = "tapestry.blackbird";

    /**
     * The Asset path of the default javascript (tapestry.js) automatically injected into every rendered HTML page.
     * 
     * @since 5.2.0
     */
    public static final String DEFAULT_JAVASCRIPT = "tapestry.default-javascript";

    /**
     * If "true", then JSON page initialization content is compressed; if "false"
     * then extra white space is added (pretty printing). Defaults to "true" in production mode.
     * 
     * @since 5.2.0
     */
    public static final String COMPACT_JSON = "tapestry.compact-json";

    /**
     * If "true", then Tapestry 5.1 (and earlier) style page pooling will be used. The default is "false", to
     * allow full use of page singleton. Enabling page pooling is only necessary if an application (or library)
     * has created {@linkplain ComponentClassTransformWorker class transformations} that introduce new, mutable
     * fields into component classes. That's a very rare thing (most created fields contain immutable data).
     * 
     * @deprecated To be removed, along with the remnants of page pooling, in Tapestry 5.3.
     * @since 5.2.0
     */
    public static final String PAGE_POOL_ENABLED = "tapestry.page-pool-enabled";
    
    /**
     * If "true" and {@link #PRODUCTION_MODE} is off, comments will be rendered before and after the rendering of any component
     * allowing more visibility into which components rendered which markup. Defaults to "false". Component render tracing may be
     * enabled per-request by the presence of a request parameter "t:component-trace" with a value of "true".
     * 
     * @since 5.2.5
     */
    public static final String COMPONENT_RENDER_TRACING_ENABLED = "tapestry.component-render-tracing-enabled";
    
    /**
     * The hostname that application should use when constructing an absolute URL. The default is "", i.e. an empty string,
     * in which case system will use request.getServerName(). Not the same as environment variable HOSTNAME, but you can also 
     * contribute "$HOSTNAME" as the value to make it the same as the environment variable HOSTNAME.
     * 
     * @since 5.2.5
     */
    public static final String HOSTNAME = "tapestry.hostname";

    /**
     * The hostport that application should use when constructing an absolute URL. The default is "0", i.e. use the port value from
     * the request.
     * 
     * @since 5.2.5
     */
    public static final String HOSTPORT = "tapestry.hostport";
    
    /**
     * The secure (https) hostport that application should use when constructing an absolute URL. The default is "0", i.e. use 
     * the value from the request.
     * 
     * @since 5.2.5
     */
    public static final String HOSTPORT_SECURE = "tapestry.hostport-secure";
}
