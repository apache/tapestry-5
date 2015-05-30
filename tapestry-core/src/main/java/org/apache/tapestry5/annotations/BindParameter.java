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

import org.apache.tapestry5.internal.transform.BindParameterWorker;
import org.apache.tapestry5.ioc.annotations.UseWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.MIXIN;

/**
 * Designates a field in a mixin which is bound to the parameter of the containing
 * component corresponding to the value of the annotation. If no value is specified,
 * the bound parameter name is assumed to match the field name of the mixin.
 * For example, a mixin intended to work with form fields would define a field named
 * "value", marked by this annotation. The user-variable bound to the component's value
 * parameter would ultimately be bound in a chain:
 * user-variable {@code <=>} mixin.value {@code <=>} component.value.
 * Changes to any one value in the chain will be propagated accordingly.
 * 
 * @since 5.2.0
 * @see BindParameterWorker
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@UseWith(MIXIN)
public @interface BindParameter
{

    /**
     * @return the name of the mixin bound-parameter, exactly as for the Parameter annotation.
     */
    String name() default "";

    /**
     * @return the name(s) of the parent parameter to bind. Defaults to the name of the mixin field.
     *         If more than one
     *         name is specified, the first name matching a declared parameter of the core component
     *         will be used.
     */
    String[] value() default
    { "" };
}
