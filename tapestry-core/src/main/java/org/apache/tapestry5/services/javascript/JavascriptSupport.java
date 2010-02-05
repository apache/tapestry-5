// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.services.javascript;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.IncludeJavaScriptLibrary;
import org.apache.tapestry5.json.JSONObject;

public interface JavascriptSupport
{
    /**
     * Allocates a unique id based on the component's id. In some cases, the return value will not precisely match the
     * input value (an underscore and a unique index value may be appended).
     * 
     * @param id
     *            the component id from which a unique id will be generated
     * @return a unique id for this rendering of the page
     * @see org.apache.tapestry5.ioc.internal.util.IdAllocator
     */
    String allocateClientId(String id);

    /**
     * As with {@link #allocateClientId(String)} but uses the id of the component extracted from the resources.
     * 
     * @param resources
     *            of the component which requires an id
     * @return a unique id for this rendering of the page
     */
    String allocateClientId(ComponentResources resources);

    /**
     * Adds initialization script at {@link InitializationPriority#NORMAL} priority.
     * 
     * @param format
     *            format string (as per {@link String#format(String, Object...)}
     * @param arguments
     *            arguments referenced by format specifiers
     */
    void addScript(String format, Object... arguments);

    /**
     * Adds initialization script at the specified priority.
     * 
     * @param priority
     *            priority to use when adding the script
     * @param format
     *            format string (as per {@link String#format(String, Object...)}
     * @param arguments
     *            arguments referenced by format specifiers
     */
    void addScript(InitializationPriority priority, String format, Object... arguments);

    /**
     * Adds a call to a client-side function inside the Tapestry.Initializer namespace. Calls to this
     * method are aggregated into a call to the Tapestry.init() function. Initialization occurs at
     * {@link InitializationPriority#NORMAL} priority.
     * 
     * @param functionName
     *            name of client-side function (within Tapestry.Initializer namespace) to execute
     * @param parameter
     *            object to pass to the client-side function
     */
    void addInitializerCall(String functionName, JSONObject parameter);

    /**
     * Adds a call to a client-side function inside the Tapestry.Initializer namespace. Calls to this
     * method are aggregated into a call to the Tapestry.init() function. Initialization occurs at
     * the specified priority.
     * 
     * @param priority
     *            priority to use when adding the script
     * @param functionName
     *            name of client-side function (within Tapestry.Initializer namespace) to execute
     * @param parameter
     *            object to pass to the client-side function
     */
    void addInitializerCall(InitializationPriority priority, String functionName, JSONObject parameter);

    /**
     * Adds a call to a client-side function inside the Tapestry.Initializer namespace. Calls to this
     * method are aggregated into a call to the Tapestry.init() function. Initialization occurs at
     * {@link InitializationPriority#NORMAL} priority.
     * 
     * @param functionName
     *            name of client-side function (within Tapestry.Initializer namespace) to execute
     * @param parameter
     *            string to pass to function (typically, a client id)
     */
    void addInitializerCall(String functionName, String parameter);

    /**
     * Adds a call to a client-side function inside the Tapestry.Initializer namespace. Calls to this
     * method are aggregated into a call to the Tapestry.init() function. Initialization occurs at
     * the specified priority.
     * 
     * @param priority
     *            priority to use when adding the script
     * @param functionName
     *            name of client-side function (within Tapestry.Initializer namespace) to execute
     * @param parameter
     *            string to pass to function (typically, a client id)
     */
    void addInitializerCall(InitializationPriority priority, String functionName, String parameter);
    
    /**
     * Imports a JavaScript library as part of the rendered page. Libraries are added in the order
     * they are first imported; duplicate imports are ignored.
     * 
     * @see IncludeJavaScriptLibrary
     */
    void importJavascriptLibrary(Asset asset);
}
