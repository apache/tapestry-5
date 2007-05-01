// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.beaneditor;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Placed on either a property getter or a property setter method to control the order in which the
 * properties are presented to the user. The properties will be sorted in ascending order by the
 * value. Properties with no value will be treated as though they have the value 0. When multiple
 * properties have the same order value, they will be sorted alphabetically.
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Order {
    /** The sort order for this property. */
    int value();
}
