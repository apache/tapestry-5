// Copyright 2008, 2009, 2010, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.ResourceMinimizer;
import org.apache.tapestry5.services.javascript.JavaScriptStack;

/**
 * Defines the names of symbols used to configure Tapestry.
 *
 * @see org.apache.tapestry5.ioc.services.SymbolSource
 */
public class SymbolConstants
{
    /**
     * A comma separated list of execution modes used to control how the application is initialized.
     * Each modes can contribute a list (comma separated) of Module classes to be loaded during startup,
     * the order in which they appear is preserved.
     * The default value is: <code>production</code>.
     */
    public static final String EXECUTION_MODE = "tapestry.execution-mode";
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
     * If set to "true", then action requests will render a page markup response immediately, rather than sending a
     * redirect to render the response. "Action request" is an outdated term for "component event request" (i.e., most
     * links and all form submissions).
     *
     * @deprecated In 5.3, to be removed (along with the support it implies) in 5.4
     */
    @Deprecated
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
     * @see AssetDispatcher
     * @see AssetPathConstructor
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
     * {@link org.apache.tapestry5.services.LinkCreationListener2} (registered with the
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
     * @deprecated in 5.3, with no replacement (due to removal of Blackbird console entirely)
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
     * @deprecated in 5.3 with no replacement
     */
    public static final String BLACKBIRD = "tapestry.blackbird";

    /**
     * If "true", then JSON page initialization content is compressed; if "false"
     * then extra white space is added (pretty printing). Defaults to "true" in production mode.
     *
     * @since 5.2.0
     */
    public static final String COMPACT_JSON = "tapestry.compact-json";

    /**
     * If "true" and {@link #PRODUCTION_MODE} is off, comments will be rendered before and after the rendering of any
     * component
     * allowing more visibility into which components rendered which markup. Defaults to "false". Component render
     * tracing may be
     * enabled per-request by the presence of a request parameter "t:component-trace" with a value of "true".
     *
     * @since 5.2.5
     */
    public static final String COMPONENT_RENDER_TRACING_ENABLED = "tapestry.component-render-tracing-enabled";

    /**
     * The hostname that application should use when constructing an absolute URL. The default is "", i.e. an empty
     * string,
     * in which case system will use request.getServerName(). Not the same as environment variable HOSTNAME, but you can
     * also
     * contribute "$HOSTNAME" as the value to make it the same as the environment variable HOSTNAME.
     *
     * @since 5.3
     */
    public static final String HOSTNAME = "tapestry.hostname";

    /**
     * The hostport that application should use when constructing an absolute URL. The default is "0", i.e. use the port
     * value from
     * the request.
     *
     * @since 5.3
     */
    public static final String HOSTPORT = "tapestry.hostport";

    /**
     * The secure (https) hostport that application should use when constructing an absolute URL. The default is "0",
     * i.e. use
     * the value from the request.
     *
     * @since 5.3
     */
    public static final String HOSTPORT_SECURE = "tapestry.hostport-secure";

    /**
     * If "true", then resources (individually or when aggregated into stacks) will be minimized via the
     * {@link ResourceMinimizer} service. If "false", then minification is disabled. Tracks production mode
     * (minification is normally disabled in development mode).
     * <p/>
     * Note that Tapestry's default implementation of {@link ResourceMinimizer} does nothing; minification is provided
     * by add-on libraries.
     *
     * @since 5.3
     */
    public static final String MINIFICATION_ENABLED = "tapestry.enable-minification";

    /**
     * If "true" then at the end of each request the
     * {@link org.apache.tapestry5.services.SessionPersistedObjectAnalyzer} will be called on each session persisted
     * object that was accessed during the request.
     * <p/>
     * This is provided as a performance enhancement for servers that do not use clustered sessions.
     * <p/>
     * The default is {@code true}, to preserve 5.2 behavior. For non-clustered applications (the majority), this value should be
     * overridden to {@code false}. A future release of Tapestry may change the default.
     *
     * @since 5.3
     */
    public static final String CLUSTERED_SESSIONS = "tapestry.clustered-sessions";

    /**
     * The fix for <a href="https://issues.apache.org/jira/browse/TAP5-1596">TAP5-1596</a> means that component ids referenced
     * by event handler methods (either the naming convention, or the {@link org.apache.tapestry5.annotations.OnEvent} annotation)
     * can cause a page load error if there is no matching component in the component's template. Although this is correct behavior,
     * it can make the upgrade from 5.2 to 5.3 difficult if an existing app had some "left over" event handler methods. Changing
     * this symbol to {@code false} is a temporary approach to resolving this problem.
     * <p/>
     * This symbol will be <em>ignored</em> in release 5.4 and removed in 5.5.
     *
     * @since 5.3
     * @deprecated Deprecated in 5.3, a future release will always enforce that component ids referenced by event handler methods actually exist.
     */
    @Deprecated
    public static final String UNKNOWN_COMPONENT_ID_CHECK_ENABLED = "tapestry.compatibility.unknown-component-id-check-enabled";

    /**
     * The name of a folder in which the Tapestry application executes. Prior to 5.3, a Tapestry application always responded to all
     * URLs in the context under the context root; by setting this to the name of a folder, the T5 URLs will be inside that folder only, and should
     * match a corresponding entry in the {@code web.xml} configuration file.  This is useful when running multiple servlets within the same web application (such as when migrating
     * from Tapestry 4 or some other framework, to Tapestry 5).
     * <p>Effectively, if this symbol is set to a value, that folder name will be placed after the context path
     * (typically "/") and before the locale, page name, or other prefix.  For example, if this symbol is set to "app", the {@code web.xml <url-pattern>} should be set to {@code /app/*}, and Tapestry will
     * only be in invoked by the servlet container for requests inside the virtual {@code app} folder.
     * <p/>
     * This also affects the search for page templates (which are allowed within the web context). When set to a non-blank value, page templates are searched for in the folder, rather than in the root context.
     * <p/>
     * The default value is the empty string, which preserves Tapestry 5.2 behavior (and continues to be appropriate for most applications).
     * <p/>
     * Note that while Tapestry is case-insensitive, the servlet container is not, so the configured value must exactly match
     * the folder name inside the {@code <url-parameter>} value, including case.
     *
     * @since 5.3
     */
    public static final String APPLICATION_FOLDER = "tapestry.application-folder";

    /**
     * Boolean value to indicate if every {@link  org.apache.tapestry5.Asset2} should be fully qualified or not.
     * Default to <code>false</code> meaning no Asset URL will be fully qualified.
     * @since 5.3
     */
    public static final String ASSET_URL_FULL_QUALIFIED = "tapestry.asset-url-fully-qualified";

    /**
     * Prefix to be used for all asset paths
     */
    public static final String ASSET_PATH_PREFIX = "tapestry.asset-path-prefix";

    /**
     * A passphrase used as the basis of hash-based message authentication (HMAC) for any object stream data stored on
     * the client.  The default phrase is the empty string, which will result in a logged runtime <em>error</em>.
     * You should configure this to a reasonable value (longer is better) and ensure that all servers in your cluster
     * share the same value (configuring this in code, rather than the command line, is preferred).
     *
     * @see org.apache.tapestry5.services.ClientDataEncoder
     * @since 5.3.6
     */
    public static final String HMAC_PASSPHRASE = "tapestry.hmac-passphrase";
}
