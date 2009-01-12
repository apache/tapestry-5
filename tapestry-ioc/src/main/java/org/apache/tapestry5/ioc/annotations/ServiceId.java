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

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * An optional annotation that may be placed on a service building method of a module, or on the implementation class
 * (when using service binding via the {@link org.apache.tapestry5.ioc.ServiceBinder}). The annotation overrides the
 * default id for services (the default service id is the simple name of the service interface).
 */
@Target({ TYPE, METHOD })
@Retention(RUNTIME)
@Documented
public @interface ServiceId
{
    /**
     * An identifier of a service.
     */
    String value();
}
