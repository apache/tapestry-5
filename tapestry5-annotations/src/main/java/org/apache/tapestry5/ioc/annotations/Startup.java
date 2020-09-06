// Copyright 2010 The Apache Software Foundation
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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation that may be placed on a startup method of a module. A startup method is an simple way 
 * to provide extra logic to be executed at {@link org.apache.tapestry5.ioc.Registry#performRegistryStartup()}.
 * Instead of making contributions to the <i>RegistryStartup</i> service configuration you can provide startup 
 * methods inside your modules.
 * 
 * @since 5.2.0
 *
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Startup
{
}
