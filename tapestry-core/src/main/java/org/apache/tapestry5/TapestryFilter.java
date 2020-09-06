// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5;

import javax.servlet.ServletContext;

import org.apache.tapestry5.modules.TapestryModule;

/**
 * The TapestryFilter is responsible for intercepting all requests into the web application. It
 * identifies the requests
 * that are relevant to Tapestry, and lets the servlet container handle the rest. It is also
 * responsible for
 * initializing Tapestry.
 *
 * The application is primarily configured via context-level init parameters.
 *
 * <dl>
 * <dt>tapestry.app-package</dt>
 * <dd>The application package (used to search for pages, components, etc.)</dd>
 * </dl>
 *
 * In addition, a JVM system property affects configuration: <code>tapestry.execution-mode</code>
 * (with default value "production"). This property is a comma-separated list of execution modes.
 * For each mode, an additional init parameter is checked for:
 * <code>tapestry.<em>mode</em>-modules</code>; this is a comma-separated list of module class names
 * to load. In this way, more precise control over the available modules can be obtained which is
 * often needed during testing.
 */
public class TapestryFilter extends org.apache.tapestry5.http.TapestryFilter {
    
    public TapestryFilter() {
        super();
    }

    /**
     * Overridden in subclasses to provide additional module classes beyond those normally located. This implementation
     * returns an empty array.
     *
     * @since 5.3
     */
    protected Class[] provideExtraModuleClasses(ServletContext context)
    {
        return new Class[] { TapestryModule.class };
    }

}
