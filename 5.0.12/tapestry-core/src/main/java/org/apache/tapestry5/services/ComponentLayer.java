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

package org.apache.tapestry5.services;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marker annotation used to identify a service from the component layer that conflicts, in terms of service interface,
 * with a service from elsewhere. In particular, this is used to disambiguate {@link
 * org.apache.tapestry5.ioc.services.ClassFactory} which has one implementation (marked with {@link
 * org.apache.tapestry5.ioc.services.Builtin} and another with this annotation.
 */
@Target(
        { PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
public @interface ComponentLayer
{

}
