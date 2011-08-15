// Copyright 2010, 2011 The Apache Software Foundation
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
 * <p/>
 * Assets are localized during the {@link org.apache.tapestry5.runtime.PageLifecycleAdapter#containingPageDidLoad()} lifecycle
 * method.
 *
 * @see JavaScriptSupport
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
}
