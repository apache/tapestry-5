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
 * Allows a a page (really, the root component of the page) to be injected into another component as a read-only field.
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface InjectPage
{
    /**
     * The name of the page to inject, which is used when the field type is not sufficient to identify the page (for
     * example, when the field type is an interface implemented by the page). A non-blank value here overrides the
     * lookup by class name (from the field type).
     */
    String value() default "";
}
