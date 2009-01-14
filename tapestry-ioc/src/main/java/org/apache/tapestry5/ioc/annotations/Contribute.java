// Copyright 2009 The Apache Software Foundation
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
 * As an alternative to the naming convention, this annotation may be used to mark a method of a module
 * as a service contributor method. If several implementations of a service interface are provided you should 
 * disambiguate by providing marker annotations or use the naming convention (method starts with 'contribute').
 */
@Target({ METHOD })
@Retention(RUNTIME)
@Documented
public @interface Contribute
{
    /**
     * A type of a service to contribute into.
     */
    Class value();
}
