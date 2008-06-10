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

package org.apache.tapestry5.annotations;


import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Used in conjunction with the {@link Inject} annotation to identify a service <em>by name</em> and not by type. This
 * is most useful when there are multiple services with the same service interface and a particular one needs to be
 * selected.
 */
@Target(
        { FIELD, PARAMETER })
@Documented
@Retention(RUNTIME)
public @interface Service
{
    /**
     * The name of the service. Symbols will be expanded and case is not relevant.
     */
    String value();
}
