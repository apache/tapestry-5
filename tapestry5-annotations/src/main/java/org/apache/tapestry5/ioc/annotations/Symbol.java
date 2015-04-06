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

package org.apache.tapestry5.ioc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.*;

/**
 * Used to inject a symbol value, via a symbol name. This is used much like {@link
 * org.apache.tapestry5.ioc.annotations.Value} annotation, except that symbols are not expanded ... the entire value is
 * a symbol name. This allows the annotation to reference a public constant variable.
 *
 *
 * The injected value may be coerced from string to an alternate type (defined by the field or parameter to which
 * the @Symbol annotation is attached). For better control, use the {@link IntermediateType} annotation as well, which
 * allows the string to be coerced to an alternate type before being coerced a second time to the field or parameter type.
 */
@Target(
        {PARAMETER, FIELD})
@Retention(RUNTIME)
@Documented
@UseWith({COMPONENT, MIXIN, PAGE, SERVICE})
public @interface Symbol
{
    /**
     * The name of the symbol to inject.
     */
    String value();
}
