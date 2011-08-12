// Copyright 2011 The Apache Software Foundation
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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.MIXIN;

import org.apache.tapestry5.ioc.annotations.UseWith;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows embedding mixins in other mixins. This annotation may be placed only on a mixin field in a mixin class. The
 * embedded mixin is applied to embedded components of the component the parent mixin is applied to.
 * 
 * @since 5.3
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
@UseWith(MIXIN)
public @interface EmbeddedMixin
{
    String value();

    /**
     * Defines an ordering constraint for when the embedded mixin should be applied in relation to other embedded
     * mixins. The string is analogous exactly to the strings used to define ordered contributions. Eg:
     * 
     * @EmbeddedMixin(order={"before:mixina","after:mixinb" ).
     */
    String[] order() default {};
}
