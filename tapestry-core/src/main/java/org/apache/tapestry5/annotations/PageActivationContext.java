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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.PAGE;

/**
 * Annotation for a field for which the page activation context handlers (onActivate and onPassivate) should be created.
 * In order to use this annotation you must contribute a {@link org.apache.tapestry5.ValueEncoder} for the class of the
 * annotated property.
 *
 * If using this annotation more than once per page class you must specify unique indexes for each. Indexes must start
 * at 0 and increment by 1 (eg. if 3 annotations are present they must have indexes of 0, 1 and 2)
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
@UseWith(PAGE)
public @interface PageActivationContext
{
    /**
     * Whether to create an activate event handler.
     */
    boolean activate() default true;

    /**
     * Whether to create a passivate event handler
     */
    boolean passivate() default true;
    
    /**
     * The index of the page activation context parameter (default 0)
     * @since 5.4
     */
    int index() default 0;
}
