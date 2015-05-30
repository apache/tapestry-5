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

package org.apache.tapestry5.annotations;

import org.apache.tapestry5.ioc.annotations.UseWith;
import org.apache.tapestry5.services.javascript.JavaScriptStack;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.lang.annotation.*;

import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.*;

/**
 * Annotations to control the importing of JavaScript stacks and libraries as well as stylesheets. This annotation may
 * be placed on a class, in which case importing will occur as part of the {@link SetupRender} render phase.
 * Alternately, the annotation maybe placed on any method (though typically it will be placed on a render phase
 * method) and the import operations will be associated of that method.
 *
 * Use of this annotation is translated into invocations against the {@link org.apache.tapestry5.services.javascript.JavaScriptSupport}
 * environmental; all imports there will implicitly import the core stack.
 *
 * Assets are localized during the {@link org.apache.tapestry5.runtime.PageLifecycleAdapter#containingPageDidLoad()} lifecycle
 * method.
 *
 * @since 5.2.0
 */
@Target(
        {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@UseWith(
        {COMPONENT, MIXIN, PAGE})
public @interface Import
{
    /**
     * JavaScript Stacks to import. Stacks are imported before individual libraries. Note that
     * stacks themselves may have {@linkplain JavaScriptStack#getStacks() dependencies on other
     * stacks}.
     *
     * @see JavaScriptStack
     * @see JavaScriptSupport#importStack(String)
     */
    String[] stack() default {};

    /**
     * JavaScript libraries to import. Each value is an asset path; symbols in each path are expanded. The library may
     * be localized.
     *
     * @see JavaScriptSupport#importJavaScriptLibrary(org.apache.tapestry5.Asset)
     */
    String[] library() default {};

    /**
     * Stylesheets to import. Each value is an asset path; symbols in each path are expanded. The stylesheet may be
     * localized. The stylesheet is imported with no options.
     *
     * @see JavaScriptSupport#importStylesheet(org.apache.tapestry5.Asset)
     */
    String[] stylesheet() default {};

    /**
     * Names of modules to import. A module name consists of a path, with the terms seperated by a slash character. The first
     * term is the library name (or "app" for the application), e.g. <code>flash/gordon</code> would map to the file
     * <code>META-INF/modules/flash/gordon.js</code>.  Alternately a function name can be included, after a colon seperator.
     * e.g., <code>flash/gordon:setup</code>.
     *
     * Module initializations specified this way may not have an parameters, so they are typically doing single-use
     * setup.
     *
     * @see org.apache.tapestry5.services.javascript.ModuleManager
     * @see JavaScriptSupport#require(String)
     * @since 5.4
     */
    String[] module() default {};
}
