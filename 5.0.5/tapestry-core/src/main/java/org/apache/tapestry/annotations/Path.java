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

package org.apache.tapestry.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.tapestry.Asset;

/**
 * Used in conjunction with the {@link Inject} annotation to inject an {@link Asset} based on a
 * path.
 */
@Target(
{ FIELD, PARAMETER })
@Documented
@Retention(RUNTIME)
public @interface Path
{
    /**
     * The path to the resource; if prefixed (say with "classpath:") then its a complete path within
     * the identified namespace; otherwise it's a relative path from the class containing the
     * annotation. Symbols will be expanded.
     * 
     * @return
     */
    String value();
}
