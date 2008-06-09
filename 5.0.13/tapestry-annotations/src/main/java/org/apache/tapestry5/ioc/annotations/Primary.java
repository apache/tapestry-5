// Copyright 2007, 2008 The Apache Software Foundation
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
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marker annotation used to denote a service that is the primary instance of some common interface. This is often used
 * when a service is a {@linkplain org.apache.tapestry5.ioc.services.ChainBuilder chain of command} or {@linkplain
 * org.apache.tapestry5.ioc.services.StrategyBuilder strategy-based} and, therefore, many services will implement the
 * same interface.
 */
@Target(
        { PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
public @interface Primary
{

}
