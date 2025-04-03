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

/**
 * Superinterface with the parts shared by {@linkplain Initialization} and {@linkplain EsModuleInitialization}.
 *
 * @since 5.10.0
 */
public interface AbstractInitialization<T extends AbstractInitialization<?>>
{

    /**
     * Specifies the function to invoke.  If this method is not invoked, then the module is expected to export
     * just a single function (which may, or may not, take {@linkplain #with(Object...) parameters}).
     *
     * @param functionName
     *         name of a function exported by the module.
     * @return this Initialization, for further configuration
     */
    T invoke(String functionName);

    /**
     * Specifies the arguments to be passed to the function. Often, just a single {@link org.apache.tapestry5.json.JSONObject}
     * is passed. When multiple Initializations exist with the same function name (or no function name), and no arguments,
     * they are coalesced into a single Initialization: it is assumed that an initialization with no parameters needs to
     * only be invoked once.
     *
     * @param arguments
     *         any number of values. Each value may be one of: null, String, Boolean, Number,
     *         {@link org.apache.tapestry5.json.JSONObject}, {@link org.apache.tapestry5.json.JSONArray}, or
     *         {@link org.apache.tapestry5.json.JSONLiteral}.
     */
    void with(Object... arguments);
}
