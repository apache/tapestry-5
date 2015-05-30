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

package org.apache.tapestry5.annotations;

import org.apache.tapestry5.ioc.annotations.UseWith;

import java.lang.annotation.*;

import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.*;

/**
 * Indicates that a method should only be evaluated once per request and the result cached.
 * Further calls to the method during the same request will return the cached result.
 * However, if the method's component occurs more than once within an enclosing component,
 * the cached results will be distinct for each occurrence.
 *
 * This annotation is commonly used on getters for component properties:
 * <pre>
 * &#064;Cached
 * Date getNow() {
 *     new Date();
 * }
 * </pre>
 * You may not apply &#064;Cached to void methods or methods with parameters.
 *
 * Note that this annotation is inheritance-safe; if a subclass calls a superclass method that
 * has &#064;Cached then the value the subclass method gets is the cached value.
 *
 * The watch parameter can be passed a binding expression which will be evaluated each time the method is called. The
 * method will then only be executed the first time it is called and after that only when the value of the binding
 * changes. This can be used, for instance, to have the method only evaluated once per iteration of a loop by setting
 * watch to the value or index of the loop.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@UseWith({COMPONENT,MIXIN,PAGE})
public @interface Cached
{
    /**
     * The optional binding to watch (default binding prefix is "prop").
     */
    String watch() default "";
}
