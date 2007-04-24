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

package org.apache.tapestry.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Describes a class as a component class, one for which a number of annotation-based
 * transformations should occur when loaded. To be a component class, a class must be in an
 * appropriate package, and must have the ComponentClass annotation (or extend from a base class
 * that does have the annotation).
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface ComponentClass {
    /**
     * Allows meta data associated with the class to be specified. Meta data is in the form of
     * key/data pairs.
     * 
     * @return
     */
    String[] meta() default {};
}
