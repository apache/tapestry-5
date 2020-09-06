// Copyright 2010 The Apache Software Foundation
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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation that may be placed on a decorator method of a module. The annotation may/should be used in combination with
 * marker annotations to disambiguate the service to advise. This annotation was introduced as an alternative
 * to the naming convention for decorator methods.
 *
 * @since 5.2.2
 *
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Decorate
{
    /**
     * Type of the service to decorate.
     */
    Class serviceInterface() default Object.class;
   
    /**
     * Id of the decorator.
     */
    String id() default "";
}
