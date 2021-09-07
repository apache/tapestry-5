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

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.PAGE;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.COMPONENT;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.MIXIN;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.tapestry5.ioc.annotations.UseWith;

/**
 * Annotation that may be placed on parameters of event handler methods to define them
 * as having a static value. If, in a given request, the activation context value for that
 * parameter doesn't match the static value, the event handler method won't be called.
 * This is particularly useful for REST endpoint event handler methods in pages. 
 *  
 * @since 5.8.0
 */
@Target(
{ PARAMETER })
@Retention(RUNTIME)
@Documented
@UseWith(
{ PAGE, COMPONENT, MIXIN })
public @interface StaticActivationContextValue
{
    /** 
     * The activation context value, case sensitive. Empty values or ones with spaces are forbidden. 
     */
    String value();
}
