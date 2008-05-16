package org.apache.tapestry;

/**
 * Defines the names of symbols used to configure Tapestry.
 *
 * @see org.apache.tapestry.ioc.services.SymbolSource
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
}
