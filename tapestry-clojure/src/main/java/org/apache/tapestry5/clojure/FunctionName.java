// Copyright 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.clojure;

import org.apache.tapestry5.ioc.annotations.UseWith;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.SERVICE;

/**
 * Overrides the default mapping from method name to Clojure function name.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
@UseWith(SERVICE)
public @interface FunctionName
{
    /**
     * @return the name of the Clojure function to map the method to. This may be a simple name, in which case
     * it will be a name within the {@link Namespace} of the service interface, or it may be a fully qualified name
     * of an arbitrary Clojure function.
     */
    String value();
}
