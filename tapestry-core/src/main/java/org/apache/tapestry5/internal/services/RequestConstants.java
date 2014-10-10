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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.services.javascript.JavaScriptStack;

/**
 * Constants used when processing requests from the client web browser.
 */
public final class RequestConstants
{

    /**
     * Virtual folder name for assets that are actually stored in the context, but are exposed (much like classpath
     * assets) to gain far-future expires headers and automatic content compression.
     *
     * @since 5.1.0.0
     */
    public static final String CONTEXT_FOLDER = "ctx";

    /**
     * Folder for combined {@link JavaScriptStack} JavaScript files. The path consists of the locale (as a folder) and
     * the name
     * of the stack (suffixed with ".js").
     *
     * @since 5.2.0
     */
    public static final String STACK_FOLDER = "stack";

    /**
     * Name of parameter, in an Ajax update, that identifies the client-side id of the {@link Form} being extended. Used
     * with {@link Zone} and other similar components that may be contained within a form.
     *
     * @since 5.2.0
     */
    public static final String FORM_CLIENTID_PARAMETER = "t:formid";

    /**
     * The server-side part of {@link #FORM_CLIENTID_PARAMETER} identifying the server-side component id.
     *
     * @since 5.2.0
     */
    public static final String FORM_COMPONENTID_PARAMETER = "t:formcomponentid";
}
