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
 * Used to attach the name of a Translator used to convert the associated property between server-side and
 * client-side representations.
 *
 * May be placed on any getter or setter method, or on the matching field.
 * 
 * @see Validate
 * @since 5.2.0
 */
@Target(
{ ElementType.FIELD, ElementType.METHOD })
@Retention(RUNTIME)
@Documented
@UseWith(
{ BEAN, COMPONENT, MIXIN, PAGE })
public @interface Translate
{
    String value();
}
