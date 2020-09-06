// Copyright 2010, 2011 The Apache Software Foundation
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
package org.apache.tapestry5.ioc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * An annotation that may be placed on a contributor method of a module. The annotation may/should be used in combination with
 * {@link Marker} annotation to disambiguate the service to contribute into. This annotation was introduced as an alternative
 * to the naming convention for contributor methods.
 *
 * @see Optional
 * @since 5.2.0
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Contribute
{
    /**
     * Type of the service to contribute into.
     */
    Class value();
}
