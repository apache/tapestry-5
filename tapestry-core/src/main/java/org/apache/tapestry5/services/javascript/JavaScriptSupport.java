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

package org.apache.tapestry5.services.javascript;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.EnvironmentalShadowBuilder;

/**
 * The JavaScriptSupport environmental is very stateful, accumulating JavaScript stacks, libraries and initialization
 * code until the end of the main page render; it then updates the rendered DOM (adding &lt;script&gt; tags to the
 * &lt;head&gt; and &lt;body&gt;) before the document is streamed to the client.
 *
 * JavaScriptSupport is normally accessed within a component by using the {@link Environmental} annotation on a
 * component field. In addition, JavaScriptSupport may also be accessed as a service (the service
 * {@linkplain EnvironmentalShadowBuilder internally delegates to the current environmental instance}), which is useful
 * for service-layer objects.
 *
 * The term "import" is used on many methods to indicate that the indicated resource (stack, library or stylesheet) will
 * only be added to the final cocument once, even when there are repeated calls.
 *
 * The name is slightly a misnomer, since there's a side-line of {@linkplain #importStylesheet(StylesheetLink)} as well.
 *
 * JavaScriptSupport works equally well inside an Ajax request that produces a JSON-formatted partial page update response.
 *
 * @see org.apache.tapestry5.internal.services.DocumentLinker
 * @since 5.2.0
 */
public interface JavaScriptSupport
{
    /**
     * Allocates a unique id based on the component's id. In some cases, the return value will not precisely match the
     * input value (an underscore and a unique index value may be appended).
     *
     * @param id
     *         the component id from which a unique id will be generated
     * @return a unique id for this rendering of the page
     * @see org.apache.tapestry5.ioc.util.IdAllocator
     */
    String allocateClientId(String id);

    /**
     * As with {@link #allocateClientId(String)} but uses the id of the component extracted from the resources.
     *
     * @param resources
     *         of the component which requires an id
     * @return a unique id for this rendering of the page
     */
    String allocateClientId(ComponentResources resources);

    /**
     * Adds initialization script at {@link InitializationPriority#NORMAL} priority.
     *
     * @param format
     *         format string (as per {@link String#format(String, Object...)}
     * @param arguments
     *         arguments referenced by format specifiers
     * @deprecated Deprecated in 5.4; refactor to use {@linkplain #require(String) JavaScript modules} instead
     */
    void addScript(String format, Object... arguments);

    /**
     * Adds initialization script at the specified priority.
     *
     * @param priority
     *         priority to use when adding the script
     * @param format
     *         format string (as per {@link String#format(String, Object...)}
     * @param arguments
     *         arguments referenced by format specifiers
     * @deprecated Deprecated in 5.4; refactor to use {@linkplain #require(String) JavaScript modules} instead
     */
    void addScript(InitializationPriority priority, String format, Object... arguments);

    /**
     * Adds a call to a client-side function inside the Tapestry.Initializer namespace. Calls to this
     * method are aggregated into a call to the Tapestry.init() function. Initialization occurs at
     * {@link InitializationPriority#NORMAL} priority.
     *
     * @param functionName
     *         name of client-side function (within Tapestry.Initializer namespace) to execute
     * @param parameter
     *         object to pass to the client-side function
     * @deprecated Deprecated in 5.4; refactor to use {@linkplain #require(String) JavaScript modules} instead
     */
    void addInitializerCall(String functionName, JSONObject parameter);

    /**
     * Adds a call to a client-side function inside the Tapestry.Initializer namespace. Calls to this
     * method are aggregated into a call to the Tapestry.init() function. Initialization occurs at
     * {@link InitializationPriority#NORMAL} priority.
     *
     * @param functionName
     *         name of client-side function (within Tapestry.Initializer namespace) to execute
     * @param parameter
     *         array of parameters to pass to the client-side function
     * @since 5.3
     * @deprecated Deprecated in 5.4; refactor to use {@linkplain #require(String) JavaScript modules} instead
     */
    void addInitializerCall(String functionName, JSONArray parameter);

    /**
     * Adds a call to a client-side function inside the Tapestry.Initializer namespace. Calls to this
     * method are aggregated into a call to the Tapestry.init() function. Initialization occurs at
     * {@link InitializationPriority#NORMAL} priority.
     *
     * @param functionName
     *         name of client-side function (within Tapestry.Initializer namespace) to execute
     * @param parameter
     *         array of parameters to pass to the client-side function
     * @since 5.3
     * @deprecated Deprecated in 5.4; refactor to use {@linkplain #require(String) JavaScript modules} instead
     */
    void addInitializerCall(InitializationPriority priority, String functionName, JSONArray parameter);

    /**
     * Adds a call to a client-side function inside the Tapestry.Initializer namespace. Calls to this
     * method are aggregated into a call to the Tapestry.init() function. Initialization occurs at
     * the specified priority.
     *
     * @param priority
     *         priority to use when adding the script
     * @param functionName
     *         name of client-side function (within Tapestry.Initializer namespace) to execute
     * @param parameter
     *         object to pass to the client-side function
     * @deprecated Deprecated in 5.4; refactor to use {@linkplain #require(String) JavaScript modules} instead
     */
    void addInitializerCall(InitializationPriority priority, String functionName, JSONObject parameter);

    /**
     * Adds a call to a client-side function inside the Tapestry.Initializer namespace. Calls to this
     * method are aggregated into a call to the Tapestry.init() function. Initialization occurs at
     * {@link InitializationPriority#NORMAL} priority.
     *
     * @param functionName
     *         name of client-side function (within Tapestry.Initializer namespace) to execute
     * @param parameter
     *         string to pass to function (typically, a client id)
     * @deprecated Deprecated in 5.4; refactor to use {@linkplain #require(String) JavaScript modules} instead
     */
    void addInitializerCall(String functionName, String parameter);

    /**
     * Adds a call to a client-side function inside the Tapestry.Initializer namespace. Calls to this
     * method are aggregated into a call to the Tapestry.init() function. Initialization occurs at
     * the specified priority.
     *
     * @param priority
     *         priority to use when adding the script
     * @param functionName
     *         name of client-side function (within Tapestry.Initializer namespace) to execute
     * @param parameter
     *         string to pass to function (typically, a client id)
     * @deprecated Deprecated in 5.4; refactor to use {@linkplain #require(String) JavaScript modules} instead
     */
    void addInitializerCall(InitializationPriority priority, String functionName, String parameter);

    /**
     * Imports a JavaScript library as part of the rendered page. Libraries are added in the order
     * they are first imported; duplicate imports are ignored. Libraries are added to the page serially
     * (whereas modules may be loaded in parallel), and all libraries are added before any modules are loaded.
     * Because of this, it is preferrable to organize your JavaScript into modules, rather than libraries.
     *
     * @return this JavaScriptSupport, for further configuration
     * @see org.apache.tapestry5.annotations.Import
     */
    JavaScriptSupport importJavaScriptLibrary(Asset asset);

    /**
     * A convenience method that wraps the Asset as a {@link StylesheetLink}.
     *
     * @param stylesheet
     *         asset for the stylesheet
     * @return this JavaScriptSupport, for further configuration
     * @see #importStylesheet(StylesheetLink)
     */
    JavaScriptSupport importStylesheet(Asset stylesheet);

    /**
     * Imports a Cascading Style Sheet file as part of the rendered page. Stylesheets are added in the
     * order they are first imported; duplicate imports are ignored. Starting in 5.4, importing a stylesheet
     * imports the core stack as well (with its stylesheets); this ensures that the imported stylesheet(s) can
     * override rules from Tapestry's default stylesheets.
     *
     * @param stylesheetLink
     *         encapsulates the link URL plus any additional options
     * @return this JavaScriptSupport, for further configuration
     */
    JavaScriptSupport importStylesheet(StylesheetLink stylesheetLink);

    /**
     * Imports a {@link JavaScriptStack} by name, a related set of JavaScript libraries and stylesheets.
     * Stacks are contributions to the {@link JavaScriptStackSource} service. When
     * {@linkplain SymbolConstants#COMBINE_SCRIPTS JavaScript aggregation} in enabled, the stack will be represented by
     * a single virtual URL; otherwise the individual asset URLs of the stack
     * will be added to the document.
     *
     * Please refer to the {@linkplain #importJavaScriptLibrary(Asset) notes about libraries vs. modules}.
     *
     * @param stackName
     *         the name of the stack (case is ignored); the stack must exist
     * @return this JavaScriptSupport, for further configuration
     */
    JavaScriptSupport importStack(String stackName);

    /**
     * Import a Javascript library with an arbitrary URL.
     *
     * Please refer to the {@linkplain #importJavaScriptLibrary(Asset) notes about libraries vs. modules}.
     */
    JavaScriptSupport importJavaScriptLibrary(String libraryURL);

    /**
     * Invoked to set focus on a rendered field. Takes into account priority, meaning that a field with errors will take
     * precedence over a merely required field, and over a field that is optional. The value
     * {@link org.apache.tapestry5.FieldFocusPriority#OVERRIDE} can be used to force a particular field to receive
     * focus.
     *
     * @param priority
     *         focus is set only if the provided priority is greater than the current priority
     * @param fieldId
     *         id of client-side element to take focus
     */
    JavaScriptSupport autofocus(FieldFocusPriority priority, String fieldId);


    /**
     * Requires a JavaScript module by name. On the client, this will <code>require()</code> the module and
     * (optionally) de-reference a function exported by the module (or, treat the module as exporting a single
     * implicit function). The function will be invoked. Use the returned {@link Initialization} to specify the function name
     * to invoke, and the parameters to pass to the function.
     *
     * In some cases, a module exports no functions, but performs some initialization (typically, adding document-level
     * event handlers), in which case a call to require() is sufficient. In cases where the module, or a function
     * within the module, are invoked with no parameters, the calls will be collapsed into a single invocation.
     *
     * If the module is part of a {@linkplain org.apache.tapestry5.services.javascript.JavaScriptStack#getModules() JavaScript stack},
     * then the stack will be imported; this is important when {@linkplain SymbolConstants#COMBINE_SCRIPTS JavaScript aggregation is enabled},
     * but also ensures that libraries in the stack are loaded before the module (for cases where the
     * module has dependencies on libraries not wrapped as AMD modules).
     *
     * @param moduleName
     *         the name of the module to require
     * @return Initialization instance, used to configure function name, arguments, etc.
     * @since 5.4
     */
    Initialization require(String moduleName);

    /**
     * Adds a module configuration callback for this request.
     *
     * @param callback
     *         a {@link ModuleConfigurationCallback}. It cannot be null.
     * @see DocumentLinker#addModuleConfigurationCallback(ModuleConfigurationCallback)
     * @since 5.4
     */
    void addModuleConfigurationCallback(ModuleConfigurationCallback callback);

}
