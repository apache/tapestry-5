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

import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.corelib.components.AjaxFormLoop;
import org.apache.tapestry5.corelib.components.BeanDisplay;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.corelib.components.BeanEditor;
import org.apache.tapestry5.corelib.components.Errors;
import org.apache.tapestry5.corelib.mixins.FormGroup;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.internal.services.AssetDispatcher;
import org.apache.tapestry5.modules.NoBootstrapModule;
import org.apache.tapestry5.services.Html5Support;
import org.apache.tapestry5.services.assets.AssetPathConstructor;
import org.apache.tapestry5.services.assets.ResourceMinimizer;
import org.apache.tapestry5.services.compatibility.Trait;
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
     * This is an alias for {@link TapestryHttpSymbolConstants#EXECUTION_MODE}.
     */
    public static final String EXECUTION_MODE = TapestryHttpSymbolConstants.EXECUTION_MODE;
    /**
     * Indicates whether Tapestry is running in production mode or developer mode. This affects a large
     * number of Tapestry behaviors related to performance and security, including how exceptions are
     * reported, whether far-future expire headers are emitted, whether JavaScript files may be combined,
     * whether JSON is compressed, whether component field and parameter values are shadowed to instance
     * variables (to assist with debugging), and more.
     * 
     * This is an alias to {@link TapestryHttpSymbolConstants#PRODUCTION_MODE}.
     */
    public static final String PRODUCTION_MODE = TapestryHttpSymbolConstants.PRODUCTION_MODE;

    /**
     * A version of {@link TapestryHttpSymbolConstants#PRODUCTION_MODE} as a symbol reference. This can be used as the default value
     * of other symbols, to indicate that their default matches whatever PRODUCTION_MODE is set to, which is quite
     * common.
     *
     * @since 5.2.0
     */
    public static final String PRODUCTION_MODE_VALUE = String.format("${%s}", TapestryHttpSymbolConstants.PRODUCTION_MODE);

    /**
     * The list of locales supported by the application; locales identified in the incoming request are "narrowed" to
     * one of these values. The first locale name in the list is the default locale used when no proper match can be
     * found.
     */
    public static final String SUPPORTED_LOCALES = "tapestry.supported-locales";

    /**
     * Controls whether whitespace is compressed by default in templates, or left as is. The factory default is to
     * compress whitespace. (This can also be overridden using the xml:space attribute inside template elements.)
     */
    public static final String COMPRESS_WHITESPACE = "tapestry.compress-whitespace";

    /**
     * Time interval defining how often Tapestry will check for updates to local files (including classes).
     * Starting with 5.3, this is only used when not running in production mode. The default is "1 s"
     * (one second), which is appropriate for development. With Tapestry 5.2 and earlier this number
     * should be raised in a production environment.
     */
    public static final String FILE_CHECK_INTERVAL = "tapestry.file-check-interval";

    /**
     * Time interval that sets how long Tapestry will wait to obtain the exclusive lock needed to check local files. The
     * default is "50 ms". Starting with 5.3, this is only used when not running in production mode.
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
     * This is an alias for {@link TapestryHttpSymbolConstants#CHARSET}
     */
    public static final String CHARSET = TapestryHttpSymbolConstants.CHARSET;

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
     * This is an alias to {@link TapestryHttpSymbolConstants#MIN_GZIP_SIZE}.
     *
     * @see TapestryHttpSymbolConstants#GZIP_COMPRESSION_ENABLED
     * @since 5.1.0.0
     */
    public static final String MIN_GZIP_SIZE = TapestryHttpSymbolConstants.MIN_GZIP_SIZE;

    /**
     * Version number of the application. Prior to 5.4, this version number was integrated into asset URLs. Starting
     * with 5.4, a checksum of the individual asset's content is used instead, and this version number is only used
     * for documentation purposes; it appears in the default exception report page, for example.
     *
     * The default value is "0.0.1".  In 5.3 and earlier, the default value was a random hexadecimal string.
     * 
     * This is an alias to {@link TapestryHttpSymbolConstants#APPLICATION_VERSION}.
     *
     * @see AssetDispatcher
     * @see AssetPathConstructor
     * @since 5.1.0.0
     */
    public static final String APPLICATION_VERSION = TapestryHttpSymbolConstants.APPLICATION_VERSION;

    /**
     * Used to omit the normal Tapestry framework generator meta tag. The meta tag is rendered by default, but clients
     * who do not wish to advertise their use of Tapestry may set this symbol to "true".
     *
     * @since 5.1.0.0
     */
    public static final String OMIT_GENERATOR_META = "tapestry.omit-generator-meta";

    /**
     * If "true" (the default) then GZip compression is enabled for dynamic requests and for static assets. If you are
     * using a server that handles GZip compression for you, or you don't want to use the extra processing power
     * necessary to GZIP requests, then override this to "false".
     * This is an alias to {@link TapestryHttpSymbolConstants#GZIP_COMPRESSION_ENABLED}.
     *
     * @see TapestryHttpSymbolConstants#MIN_GZIP_SIZE
     * @see org.apache.tapestry5.http.services.ResponseCompressionAnalyzer
     * @see org.apache.tapestry5.http.services.CompressionAnalyzer
     * @since 5.1.0.0
     */
    public static final String GZIP_COMPRESSION_ENABLED = TapestryHttpSymbolConstants.GZIP_COMPRESSION_ENABLED;

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
     * {@link org.apache.tapestry5.http.Link} path by the {@link org.apache.tapestry5.services.ComponentEventLinkEncoder}
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
     * The default stylesheet automatically inserted into every rendered HTML page when
     * no Bootstrap version is enabled (i.e both {@link Trait#BOOTSTRAP_3} and {@link Trait#BOOTSTRAP_4}
     * traits are disabled, something done by {@linkplain NoBootstrapModule}). 
     * 
     * It was deprecated in 5.4 with no replacement (the stylesheet is now associated with the core {@link JavaScriptStack}.),
     * but undeprecated in 5.5.0 with the caveat described above.
     *
     * @see NoBootstrapModule
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
     * If "true", then JSON page initialization content is compressed; if "false"
     * then extra white space is added (pretty printing). Defaults to "true" in production mode.
     *
     * @since 5.2.0
     */
    public static final String COMPACT_JSON = "tapestry.compact-json";

    /**
     * If "true" and {@link TapestryHttpSymbolConstants#PRODUCTION_MODE} is off, comments will be rendered before and after the rendering of any
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
     * This is an alias to {@link TapestryHttpSymbolConstants#HOSTNAME}.
     *
     * @since 5.3
     */
    public static final String HOSTNAME = TapestryHttpSymbolConstants.HOSTNAME;

    /**
     * The hostport that application should use when constructing an absolute URL. The default is "0", i.e. use the port
     * value from
     * the request. This is an alias to {@link TapestryHttpSymbolConstants#HOSTPORT}.
     *
     * @since 5.3
     */
    public static final String HOSTPORT = TapestryHttpSymbolConstants.HOSTPORT;

    /**
     * The secure (https) hostport that application should use when constructing an absolute URL. The default is "0",
     * i.e. use
     * the value from the request.
     * This is an alias to {@link TapestryHttpSymbolConstants#HOSTPORT_SECURE}.
     *
     * @since 5.3
     */
    public static final String HOSTPORT_SECURE = TapestryHttpSymbolConstants.HOSTPORT_SECURE;

    /**
     * If "true", then resources (individually or when aggregated into stacks) will be minimized via the
     * {@link ResourceMinimizer} service. If "false", then minification is disabled. Tracks production mode
     * (minification is normally disabled in development mode).
     *
     * Note that Tapestry's default implementation of {@link ResourceMinimizer} does nothing; minification is provided
     * by add-on libraries.
     *
     * @since 5.3
     */
    public static final String MINIFICATION_ENABLED = "tapestry.enable-minification";

    /**
     * If "true" then at the end of each request the
     * {@link org.apache.tapestry5.http.services.SessionPersistedObjectAnalyzer} will be called on each session persisted
     * object that was accessed during the request.
     *
     * This is provided as a performance enhancement for servers that do not use clustered sessions.
     *
     * The default is {@code true}, to preserve 5.2 behavior. For non-clustered applications (the majority), this value should be
     * overridden to {@code false}. A future release of Tapestry may change the default.
     * 
     * This is an alias to {@link TapestryHttpSymbolConstants#CLUSTERED_SESSIONS}.
     *
     * @since 5.3
     */
    public static final String CLUSTERED_SESSIONS = TapestryHttpSymbolConstants.CLUSTERED_SESSIONS;

    /**
     * The name of a folder in which the Tapestry application executes. Prior to 5.3, a Tapestry application always responded to all
     * URLs in the context under the context root; by setting this to the name of a folder, the T5 URLs will be inside that folder only, and should
     * match a corresponding entry in the {@code web.xml} configuration file.  This is useful when running multiple servlets within the same web application (such as when migrating
     * from Tapestry 4 or some other framework, to Tapestry 5).
     * Effectively, if this symbol is set to a value, that folder name will be placed after the context path
     * (typically "/") and before the locale, page name, or other prefix.  For example, if this symbol is set to "app", the {@code web.xml <url-pattern>} should be set to {@code /app/*}, and Tapestry will
     * only be in invoked by the servlet container for requests inside the virtual {@code app} folder.
     *
     * This also affects the search for page templates (which are allowed within the web context). When set to a non-blank value, page templates are searched for in the folder, rather than in the root context.
     *
     * The default value is the empty string, which preserves Tapestry 5.2 behavior (and continues to be appropriate for most applications).
     *
     * Note that while Tapestry is case-insensitive, the servlet container is not, so the configured value must exactly match
     * the folder name inside the {@code <url-parameter>} value, including case.
     *
     * @since 5.3
     */
    public static final String APPLICATION_FOLDER = "tapestry.application-folder";

    /**
     * Boolean value to indicate if every {@link  org.apache.tapestry5.Asset} should be fully qualified or not.
     * Default to <code>false</code> meaning no Asset URL will be fully qualified.
     *
     * @since 5.3
     */
    public static final String ASSET_URL_FULL_QUALIFIED = "tapestry.asset-url-fully-qualified";

    /**
     * Prefix to be used for all resource paths, used to recognize which requests are for assets. This value
     * is appended to the context path and the (optional {@linkplain #APPLICATION_FOLDER application folder}.
     * It may contain slashes, but should not begin or end with one.
     *
     * The default is "assets".
     */
    public static final String ASSET_PATH_PREFIX = "tapestry.asset-path-prefix";


    /**
     * Prefix used for all module resources. This may contain slashes, but should not being or end with one.
     * Tapestry will create two {@link org.apache.tapestry5.http.services.Dispatcher}s from this: one for normal
     * modules, the other for GZip compressed modules (by appending ".gz" to this value).
     *
     * The default is "modules".
     *
     * @since 5.4
     */
    public static final String MODULE_PATH_PREFIX = "tapestry.module-path-prefix";

    /**
     * Identifies the context path of the application, as determined from {@link javax.servlet.ServletContext#getContextPath()}.
     * This is either a blank string or a string that starts with a slash but does not end with one.
     * This is an alias to {@link TapestryHttpSymbolConstants#CONTEXT_PATH}.
     *
     * @since 5.4
     */
    public static final String CONTEXT_PATH = TapestryHttpSymbolConstants.CONTEXT_PATH;

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

    /**
     * The root asset path for Twitter Bootstrap; if your application uses a modified version of Bootstrap,
     * you can override this symbol to have Tapestry automatically use your version. The value should be a path
     * to a folder (under "classpath:" or "context:") and should not include a trailing slash.
     *
     * @since 5.4
     */
    public static final String BOOTSTRAP_ROOT = "tapestry.bootstrap-root";

    /**
     * The root asset path for Font Awesome; if your application uses a modified version of it,
     * you can override this symbol to have Tapestry automatically use your version. The value should be a path
     * to a folder (under "classpath:" or "context:") and should not include a trailing slash.
     *
     * @since 5.5
     */
    public static final String FONT_AWESOME_ROOT = "tapestry.font-awesome-root";

    /**
     * Tapestry relies on an underlying client-side JavaScript infrastructure framework to handle DOM manipulation,
     * event handling, and Ajax requests. Through Tapestry 5.3, the foundation was
     * <a href="http://http://prototypejs.org/">Prototype</a>. In 5.4, support for
     * <a href="http://jquery.org/">jQuery</a> has been added, and it is possible to add others. This symbol defines a value that is used to select
     * a resource that is provided to the {@link org.apache.tapestry5.services.javascript.ModuleManager} service
     * as a {@link org.apache.tapestry5.services.javascript.JavaScriptModuleConfiguration} to provide a specific implementation
     * of the {@code t5/core/dom} module. Tapestry 5.4 directly supports "prototype" or "jquery".  To support
     * other foundation frameworks, override this symbol value and supply your own module configuration.
     *
     * In Tapestry 5.4, this defaults to "prototype" for compatibility with 5.3. This will likely change in
     * 5.5 to default to "jquery". At some point in the future, Prototype support will no longer be present.
     *
     * @since 5.4
     */
    public static final String JAVASCRIPT_INFRASTRUCTURE_PROVIDER = "tapestry.javascript-infrastructure-provider";

    /**
     * If true (the default), then Tapestry will apply locking semantics around access to the {@link javax.servlet.http.HttpSession}.
     * Reading attribute names occurs with a shared read lock; getting or setting an attribute upgrades to an exclusive write lock.
     * This can tend to serialize threads when a number of simultaneous (Ajax) requests from the client arrive ... however,
     * many implementations of HttpSession are not thread safe, and often mutable objects are stored in the session and shared
     * between threads. Leaving this on the default will yield a more robust application; setting it to false may speed
     * up processing for more Ajax intensive applications (but care should then be given to ensuring that objects shared inside
     * the session are themselves immutable or thread-safe).
     * 
     * This is an alias to {@link TapestryHttpSymbolConstants#SESSION_LOCKING_ENABLED}.
     *
     * @since 5.4
     */
    public static final String SESSION_LOCKING_ENABLED = TapestryHttpSymbolConstants.SESSION_LOCKING_ENABLED;

    /**
     * If true (the default), then Tapestry will automatically include the "core" stack in all
     * pages.
     *
     * @see <a href="https://issues.apache.org/jira/browse/TAP5-2169">TAP5-2169</a>
     * @since 5.4
     */
    public static final String INCLUDE_CORE_STACK = "tapestry.include-core-stack";

    /**
     * Defines the CSS class that will be given to HTML element (usually a div) &lt;div&gt; generated by
     * the {@linkplain FormGroup} mixin and the
     * {@linkplain BeanEditForm} and {@linkplain BeanEditor}
     * components surrounding the label and the field. If the value isn't 
     * {@code form-group}, the div will have {@code class="form-group [value]}.
     * The default value is <code>form-group</code>.
     *
     * @see <a href="https://issues.apache.org/jira/browse/TAP5-2182">TAP5-2182</a>
     * @since 5.4
     */
    public static final String FORM_GROUP_WRAPPER_CSS_CLASS = "tapestry.form-group-wrapper-css-class";

    /**
     * Defines the name of the HTML element that will surround the HTML form field generated by
     * the {@linkplain FormGroup} mixin and the {@linkplain BeanEditForm} and {@linkplain BeanEditor}.
     * If this symbol is null or an empty string, no element will be generated surrouding the
     * form field. The default value is the empty string (no wrapping).
     *
     * @see <a href="https://issues.apache.org/jira/browse/TAP5-2182">TAP5-2182</a>
     * @see #FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS
     * @since 5.4
     */
    public static final String FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME = "tapestry.form-group-form-field-wrapper-element-name";

    /**
     * Defines the CSS class of the HTML element generated by
     * the {@linkplain FormGroup} mixin and the {@linkplain BeanEditForm} and {@linkplain BeanEditor}.
     * when {@linkplain #FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME} is not set to null or the empty string.
     * The default value is the empty string (no CSS class added).
     *
     * @see <a href="https://issues.apache.org/jira/browse/TAP5-2182">TAP5-2182</a>
     * @since 5.4
     */
    public static final String FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS = "tapestry.form-group-form-field-wrapper-element-css-class";

    /**
     * Defines the CSS class that will be given to &lt;label&gt; element generated by
     * the {@linkplain FormGroup} mixin and the
     * {@linkplain BeanEditForm} and {@linkplain BeanEditor}
     * components. The default value is <code>control-label</code>.
     *
     * @see <a href="https://issues.apache.org/jira/browse/TAP5-2182">TAP5-2182</a>
     * @since 5.4
     */
    public static final String FORM_GROUP_LABEL_CSS_CLASS = "tapestry.form-group-label-css-class";

    /**
     * Defines the CSS class that will be given to form field components which are
     * {@linkplain AbstractField} subclasses. The default value is <code>form-control</code>.
     *
     * @see <a href="https://issues.apache.org/jira/browse/TAP5-2182">TAP5-2182</a>
     * @since 5.4
     */
    public static final String FORM_FIELD_CSS_CLASS = "tapestry.form-field-css-class";

    /**
     * Defines the CSS class that will be given to the &lt;dl&gt; HTML element generated by
     * {@linkplain BeanDisplay}. The default value is <code>well dl-horizontal</code>.
     *
     * @since 5.5
     */
    public static final String BEAN_DISPLAY_CSS_CLASS = "tapestry.bean-display-css-class";

    /**
     * Defines the CSS class that will be given to the &lt;div&gt; HTML element generated by
     * {@linkplain BeanEditor}/{@linkplain BeanEditForm} for boolean properties.
     * The default value is <code>input-group</code>.
     *
     * @since 5.5
     */
    public static final String BEAN_EDITOR_BOOLEAN_PROPERTY_DIV_CSS_CLASS = "tapestry.bean-editor-boolean-property-div-css-class";

    /**
     * Defines the CSS class that will be given to the HTML element generated by
     * {@linkplain Error}. If the value isn't <code>help-block</code>, the class attribute
     * will be <code>help-block [symbol value]</code>
     * The default value is <code>help-block</code>.
     *
     * @since 5.5
     */
    public static final String ERROR_CSS_CLASS = "tapestry.error-css-class";

    /**
     * Defines the CSS class that will be given to the add row link generated by
     * {@linkplain AjaxFormLoop}. The default value is <code>btn btn-default btn-sm</code>.
     *
     * @since 5.5
     */
    public static final String AJAX_FORM_LOOP_ADD_ROW_LINK_CSS_CLASS = "tapestry.ajax-form-loop-add-row-link-css-class";
    
    /**
     * Defines the prefix of the CSS class that will be given to the outer &lt;div&gt; element generated by
     * {@linkplain Errors}. The value of {@linkplain Errors}'s <code>class</code> parameter appended
     * after the prefix and a space character. The default value is <code>alert-dismissable</code>.
     *
     * @since 5.5
     */
    public static final String ERRORS_BASE_CSS_CLASS = "tapestry.errors-base-css-class";

    /**
     * Defines the default value of the {@linkplain Errors}'s <code>class</code> parameter.
     * The default value for this symbol is <code>alert alert-danger</code>.
     *
     * @since 5.5
     */
    public static final String ERRORS_DEFAULT_CLASS_PARAMETER_VALUE = "tapestry.errors-default-class-parameter-value";

    /**
     * Defines the CSS class that will be given to the close &lt;button&gt; generated by
     * {@linkplain Errors}. The default value is <code>close</code>.
     *
     * @since 5.5
     */
    public static final String ERRORS_CLOSE_BUTTON_CSS_CLASS = "tapestry.errors-close-button-css-class";

    /**
     * Defines whether {@link java.text.DateFormat} instances created by Tapestry should be
     * lenient or not by default. The default value is <code>false</code>.
     *
     * @since 5.4
     */
    public static final String LENIENT_DATE_FORMAT = "tapestry.lenient-date-format";

    /**
     * The directory to which exception report files should be written. The default is appropriate
     * for development: {@code build/exceptions}, and should be changed for production.
     *
     * @see org.apache.tapestry5.services.ExceptionReporter
     * @since 5.4
     */

    public static final String EXCEPTION_REPORTS_DIR = "tapestry.exception-reports-dir";

    /**
     * Defines whether {@link org.apache.tapestry5.internal.services.assets.CSSURLRewriter} will throw an exception when a CSS file
     * references an URL which doesn't exist. The default value is <code>false</code>.
     *
     * @since 5.4
     */
    public static final String STRICT_CSS_URL_REWRITING = "tapestry.strict-css-url-rewriting";

    /**
     * When an asset (typically, a JavaScript module) is streamed without an explicit expiration header, then
     * this value is sent as the {@code Cache-Control} header; the default is "max-age=60, must-revalidate". Setting
     * max-age to a value above zero significantly reduces the number of client requests for module content, as client
     * browsers will then cache previously downloaded versions. For normal assets, which are immutable, and fingerprinted with
     * a content hash, there is no need to set max age, and instead, a far-future expiration date is provided.
     *
     * @since 5.4
     */
    public static final String OMIT_EXPIRATION_CACHE_CONTROL_HEADER = "tapestry.omit-expiration-cache-control-header";

    /**
     * Defines whether HTML5 features should be used. Value used in the default implementation of
     * {@link Html5Support#isHtml5SupportEnabled()}. Default value: <code>false</code>.
     *
     * @see Html5Support#isHtml5SupportEnabled()
     * @since 5.4
     */
    public static final String ENABLE_HTML5_SUPPORT = "tapestry.enable-html5-support";

    /**
     * A general switch for restrictive environments, such as Google App Engine, which forbid some useful operations,
     * such as creating files or directories. Defaults to false.
     *
     * @since 5.4
     */
    public static final String RESTRICTIVE_ENVIRONMENT = "tapestry.restrictive-environment";

    /**
     * If true, then when a page includes any JavaScript, a {@code script} block is added to insert
     * a pageloader mask into the page; the pageloader mask ensure that the user can't interact with the page
     * until after the page is fully initialized.
     *
     * @since 5.4
     */
    public static final String ENABLE_PAGELOADING_MASK = "tapestry.enable-pageloading-mask";

    /**
     * Controls in what environment page preloading should occur. By default, preloading only occurs
     * in production.
     *
     * @see org.apache.tapestry5.services.pageload.PagePreloader
     * @see org.apache.tapestry5.services.pageload.PreloaderMode
     * @since 5.4
     */
    public static final String PRELOADER_MODE = "tapestry.page-preload-mode";
}
