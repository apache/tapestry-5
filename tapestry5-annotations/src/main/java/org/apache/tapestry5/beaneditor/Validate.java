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

package org.apache.tapestry5.beaneditor;

import org.apache.tapestry5.ioc.annotations.UseWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.*;

/**
 * Used to attach validation constraints directly to a property (either the getter or the setter method). The annotation
 * value is a comma separated list of <em>validation constraints</em>, each one identifying a validator type (such as
 * "required", "minlength") and optionally, a constraint value. Most validators need a constraint value, which is
 * separated from the type by an equals size (i.e., "maxlength=30"). In addition, the value may include
 * validator macros.
 *
 * May be placed on any getter or setter method, or on the matching field.
 * 
 * @see Translate
 */
@Target(
{ ElementType.FIELD, ElementType.METHOD })
@Retention(RUNTIME)
@Documented
@UseWith(
{ BEAN, COMPONENT, MIXIN, PAGE })
public @interface Validate
{
    String value();
}
