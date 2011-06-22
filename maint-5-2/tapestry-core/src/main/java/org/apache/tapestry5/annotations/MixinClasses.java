// Copyright 2006, 2009 The Apache Software Foundation
//
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

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.COMPONENT;
import org.apache.tapestry5.ioc.annotations.UseWith;

/**
 * Used to attach one or more instance mixins to an embedded component. Each mixin is specified as a specific class.
 * This annotation is only recognized when used in conjuction with the {@link Component} annotation.
 *
 * @see Mixins
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
@UseWith(COMPONENT)
public @interface MixinClasses
{
    Class[] value();

    /**
     * Specifies the ordering constraints for each mixin specified by value. Order may be a 0-length array signifying
     * no ordering for any mixin specified by value.  Otherwise, it must be the same length as value.
     * Each String can specify multiple constraints, separated by ; (before:mixina;after:mixinb).
     * Alternatively, the entry may be null or the empty string to specify no ordering constraints for that particular
     * mixin.
     * @since 5.2.0.0
     */
    String[] order() default {};

}
