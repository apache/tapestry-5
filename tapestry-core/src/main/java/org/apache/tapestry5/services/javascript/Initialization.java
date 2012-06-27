package org.apache.tapestry5.services.javascript;

/**
 * Provided by {@link JavaScriptSupport#require(String)} to allow additional, optional, details of the module-based page initialization
 * to be configured.
 *
 * @since 5.4
 */
public interface Initialization
{

    /**
     * Specifies the function to invoke.  If not invoked, then the module is expected to export
     * just a single function.
     *
     * @param functionName
     *         name of a function exported by the module.
     * @return this Initialization, for further configuration
     */
    Initialization invoke(String functionName);

    /**
     * Changes the initialization priority of the initialization from its default, {@link InitializationPriority#NORMAL}.
     *
     * @param priority
     *         new priority
     * @return this Initialization, for further configuration
     */
    Initialization priority(InitializationPriority priority);

    /**
     * Specifies the arguments to be passed to the function. Normally, just a single {@link org.apache.tapestry5.json.JSONObject}
     * is passed.
     *
     * @param arguments
     *         any number of values. Each value may be one of: null, String, Boolean, Number,
     *         {@link org.apache.tapestry5.json.JSONObject}, {@link org.apache.tapestry5.json.JSONArray}, or
     *         {@link org.apache.tapestry5.json.JSONLiteral}.
     */
    void with(Object... arguments);
}
