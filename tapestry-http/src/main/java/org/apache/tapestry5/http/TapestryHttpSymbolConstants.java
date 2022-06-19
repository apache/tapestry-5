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
package org.apache.tapestry5.http;

import org.apache.tapestry5.http.services.CorsHandler;
import org.apache.tapestry5.http.services.CorsHandlerHelper;
import org.apache.tapestry5.http.services.CorsHttpServletRequestFilter;

/**
 * Class defining constants for Tapestry HTTP symbols.
 */
final public class TapestryHttpSymbolConstants {

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
     * This is an alias to {@link TapestryHttpSymbolConstants#HOSTPORT_SECURE}.
     *
     * @since 5.3
     */
    public static final String HOSTPORT_SECURE = "tapestry.hostport-secure";
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
     * @since 5.3
     */
    public static final String CLUSTERED_SESSIONS = "tapestry.clustered-sessions";
    /**
     * If true (the default), then Tapestry will apply locking semantics around access to the {@link javax.servlet.http.HttpSession}.
     * Reading attribute names occurs with a shared read lock; getting or setting an attribute upgrades to an exclusive write lock.
     * This can tend to serialize threads when a number of simultaneous (Ajax) requests from the client arrive ... however,
     * many implementations of HttpSession are not thread safe, and often mutable objects are stored in the session and shared
     * between threads. Leaving this on the default will yield a more robust application; setting it to false may speed
     * up processing for more Ajax intensive applications (but care should then be given to ensuring that objects shared inside
     * the session are themselves immutable or thread-safe).
     *
     * @since 5.4
     */
    public static final String SESSION_LOCKING_ENABLED = "tapestry.session-locking-enabled";
    /**
     * Version number of the application. Prior to 5.4, this version number was integrated into asset URLs. Starting
     * with 5.4, a checksum of the individual asset's content is used instead, and this version number is only used
     * for documentation purposes; it appears in the default exception report page, for example.
     *
     * The default value is "0.0.1".  In 5.3 and earlier, the default value was a random hexadecimal string.
     *
     * @since 5.1.0.0
     */
    public static final String APPLICATION_VERSION = "tapestry.application-version";
    
    /**
     * Indicates whether Tapestry is running in production mode or developer mode. This affects a large
     * number of Tapestry behaviors related to performance and security, including how exceptions are
     * reported, whether far-future expire headers are emitted, whether JavaScript files may be combined,
     * whether JSON is compressed, whether component field and parameter values are shadowed to instance
     * variables (to assist with debugging), and more.
     */
    public static final String PRODUCTION_MODE = "tapestry.production-mode";
    
    /**
     * The version number of the core Tapestry framework, or UNKNOWN if the version number is not available (which
     * should only occur when developing Tapestry).
     */
    public static final String TAPESTRY_VERSION = "tapestry.version";
    
    /**
     * Identifies the context path of the application, as determined from {@link javax.servlet.ServletContext#getContextPath()}.
     * This is either a blank string or a string that starts with a slash but does not end with one.
     *
     * @since 5.4
     */
    public static final String CONTEXT_PATH = "tapestry.context-path";
    /**
     * A comma separated list of execution modes used to control how the application is initialized.
     * Each modes can contribute a list (comma separated) of Module classes to be loaded during startup,
     * the order in which they appear is preserved.
     * The default value is: <code>production</code>.
     */
    public static final String EXECUTION_MODE = "tapestry.execution-mode";
    /**
     * The charset used when rendering page markup; the charset is also used as the request encoding when handling
     * incoming requests. The default is "UTF-8".
     */
    public static final String CHARSET = "tapestry.charset";
    /**
     * Minimum output stream size, in bytes, before output is compressed using GZIP. Shorter streams are not compressed.
     * Tapestry buffers this amount and switches to a GZIP output stream as needed. The default is "100".
     *
     * @see #GZIP_COMPRESSION_ENABLED
     * @since 5.1.0.0
     */
    public static final String MIN_GZIP_SIZE = "tapestry.min-gzip-size";
    /**
     * If "true" (the default) then GZip compression is enabled for dynamic requests and for static assets. If you are
     * using a server that handles GZip compression for you, or you don't want to use the extra processing power
     * necessary to GZIP requests, then override this to "false".
     *
     * @see #MIN_GZIP_SIZE
     * @see org.apache.tapestry5.http.services.ResponseCompressionAnalyzer
     * @see org.apache.tapestry5.http.services.CompressionAnalyzer
     * @since 5.1.0.0
     */
    public static final String GZIP_COMPRESSION_ENABLED = "tapestry.gzip-compression-enabled";
    /**
     * Defines whether the CORS (Cross-Origing Resource Sharing) support 
     * should be enabled or not. Default value is "false". If you set this to "true",
     * you should also set {@link #CORS_ALLOWED_ORIGINS}.
     * @see CorsHandler
     * @see CorsHttpServletRequestFilter
     * @since 5.8.2
     */
    public static final String CORS_ENABLED = "tapestry.cors-enabled";

    /**
     * Comma-delimited of origins allowed for CORS. The special value "*" means allowing all origins.
     * This is used by the default implementation of {@link CorsHandlerHelper#getAllowedOrigin(javax.servlet.http.HttpServletRequest)}.
     * Default value is the empty string (i.e. no origins allowed and CORS actually disabled).
     * @since 5.8.2
     */
    public static final String CORS_ALLOWED_ORIGINS = "tapestry.cors-allowed-origins";
    
    /**
     * Boolean value defining whether the Access-Control-Allow-Credentials HTTP header
     * should be set automatically in the response for CORS requests. Default value is
     * <code>false</code>. This is used by the default implementation of {@link CorsHandlerHelper#configureCredentials(javax.servlet.http.HttpServletResponse)}.
     * @since 5.8.2
     */
    public static final String CORS_ALLOW_CREDENTIALS = "tapestry.cors-allow-credentials";

    /**
     * Value to be used in the Access-Control-Allow-Methods in CORS preflight request responses.
     * This is used by {@link CorsHandlerHelper#configureMethods(javax.servlet.http.HttpServletResponse)}.
     * Default value is <code>GET,HEAD,PUT,PATCH,POST,DELETE</code>.
     * @since 5.8.2
     */
    public static final String CORS_ALLOW_METHODS = "tapestry.cors-allow-methods";
    
    /**
     * Value to be used in the Access-Control-Allow-Headers in CORS preflight request responses.
     * This is used by {@link CorsHandlerHelper#configureAllowedHeaders(javax.servlet.http.HttpServletResponse, javax.servlet.http.HttpServletRequest)},
     * which only sets the header if the value isn't empty.
     * Default value is the empty string.
     * @since 5.8.2
     */
    public static final String CORS_ALLOWED_HEADERS = "tapestry.cors-allowed-headers";
    
    /**
     * Value to be used in the Access-Control-Expose-Headers in CORS preflight request responses.
     * This is used by the default implementation of {@link CorsHandlerHelper#configureExposeHeaders(javax.servlet.http.HttpServletResponse)},
     * which only sets the header if the value isn't empty.
     * Default value is the empty string.
     * @since 5.8.2
     */
    public static final String CORS_EXPOSE_HEADERS = "tapestry.cors-expose-headers";
    
    /**
     * Value to be used in the Access-Control-Max-Age in responses to preflight CORS requests.
     * This is used by {@link CorsHandlerHelper#configureMaxAge(javax.servlet.http.HttpServletResponse)},
     * which only sets the header if the value isn't empty.
     * Default value is the empty string.
     * This is an alias for {@link TapestryHttpSymbolConstants#CORS_MAX_AGE}.
     * @since 5.8.2
     */
    public static final String CORS_MAX_AGE = "tapestry.cors-max-age";

}
