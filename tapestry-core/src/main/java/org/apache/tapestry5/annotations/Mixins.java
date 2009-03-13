// Copyright 2006 The Apache Software Foundation
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

/**
 * Used to attach one ore more instance mixin to an embedded component. Each mixin is specified in terms of a logical
 * mixin type name. This annotation is only recognized when used in conjuction with the {@link Component} annotation.
 *
 * @see MixinClasses
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface Mixins
{

    /**
     * One or more mixin type names, from which actual mixin class names can be resolved.
     */
    String[] value();
}
