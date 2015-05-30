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
 * Sets the priority for JavaScript initialization scripting. InitializationPriority allows coarse-grained control
 * over the order in which initialization occurs on the client. The default is normally {@link #NORMAL}. Starting in 5.4,
 * these values have less meaning, as the {@linkplain JavaScriptSupport#require(String) dynamic loading of modules} may
 * have unexpected effects on the exact order in which initialization occurs as some initializations may be deferred until
 * a referenced module, or a dependency of a referenced module, has been loaded.
 *
 * @since 5.2.0
 */
public enum InitializationPriority
{
    /**
     * Provided JavaScript will be executed immediately (it is not deferred until the page loads). Execution occur via
     * JavaScript's {@code eval}, and occurs once all {@linkplain JavaScriptSupport#importJavaScriptLibrary(org.apache.tapestry5.Asset) JavaScript libraries}
     * (but not modules) for the page have been loaded.
     *
     *
     * @deprecated Deprecated in 5.4; this is now treated as "earlier than {@linkplain #EARLY early}".
     */
    IMMEDIATE,

    /**
     * All early execution occurs before {@link #NORMAL}.
     */
    EARLY,

    /**
     * This is the typical priority.
     */
    NORMAL,

    /**
     * Execution occurs after {@link #NORMAL}.
     */
    LATE
}
