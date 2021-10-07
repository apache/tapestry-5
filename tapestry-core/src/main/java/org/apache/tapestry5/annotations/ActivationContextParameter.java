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
 * Annotation that may be placed on parameters of event handler methods to define their names
 * in OpenAPI description. This is <em>not</em> needed for an event handler parameter to be
 * a parameter in a REST endpoint event handler method.
 *  
 * @since 5.8.0
 */
@Target(
{ PARAMETER })
@Retention(RUNTIME)
@Documented
@UseWith(
{ PAGE, COMPONENT, MIXIN })
public @interface ActivationContextParameter
{
    /** 
     * The name to be used for this parameter in the OpenAPI description.
     */
    String value();
}
