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
 * Defines a field of a component class that is replaced at runtime with a read-only value obtained from the {@link
 * org.apache.tapestry5.services.Environment} service. <p>Most commonly, the field will be of type {@link
 * org.apache.tapestry5.RenderSupport}, {@link org.apache.tapestry5.services.Heartbeat} or {@link
 * org.apache.tapestry5.services.FormSupport}, though many other options are possible.
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface Environmental
{
    /**
     * The value determines if the environmental service to be injected is required or not. In most cases, it is, so the
     * default is true.
     */
    boolean value() default true;
}
