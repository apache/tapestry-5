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

package org.apache.tapestry.ioc.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Injection based on an object reference. Whereas
 * {@link org.apache.tapestry.ioc.annotations.InjectService}'s value is always a service id, thsi
 * annotation is more flexible. The value is an object reference, which is used to select a
 * {@link org.apache.tapestry.ioc.ObjectProvider} that ultimately provides the value to be injected
 * via the annotated parameter.
 * <p>
 * There are several builtin providers, including "service". Thus this annotation with "service:Foo"
 * is identical to InjectService with value "Foo". In both cases, the visibility of services,
 * relative the module in which the injections take place, is honored.
 * 
 * 
 */
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
public @interface Inject {
    /** Object reference identifying the object to be injected. */
    String value();
}
