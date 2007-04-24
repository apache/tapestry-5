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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An optional annotation that may be placed on a service building method of a module. The
 * annotation overrides the default lifecycle for services (the default being a global singleton
 * that is instantiated on demand) for an alternate lifecycle. Alternate lifecycles are typically
 * used to bind a service implementation to a single thread or request.
 * 
 * 
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Lifecycle {
    /** An identifier used to look up a non-default lifecycle. */
    String value();
}
