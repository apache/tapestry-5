// Copyright 2006, 2007 The Apache Software Foundation
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
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marker annotation placed on a service builder method to indicate that the service should be eagerly loaded: realized
 * as if a service method had been invoked. Service realization invokes the service builder method and applys any
 * decorators to the service.
 * <p/>
 * This annotation may also be placed directly on a service implementation class, when using autobuilding via the {@link
 * org.apache.tapestry5.ioc.ServiceBinder}.
 */
@Target(
        { TYPE, METHOD })
@Retention(RUNTIME)
@Documented
public @interface EagerLoad
{

}
