// Copyright 2006, 2007 The Apache Software Foundation
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
 * Defines an <em>implementation</em> mixin for a component.
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface Mixin
{

    /**
     * Defines the type of mixin, using a logical mixin name. This value takes precedence over the type of field (to
     * which the annotation is attached). In such cases, it is presumed that the field's type is an interface
     * implemented by the actual mixin. The default value (the empty string) directs Tapestry to use the field type as
     * the mixin class to instantiate and attach to the component.
     */
    String value() default "";
}
