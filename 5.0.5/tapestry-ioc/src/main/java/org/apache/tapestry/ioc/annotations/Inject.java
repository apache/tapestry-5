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

package org.apache.tapestry.ioc.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.tapestry.ioc.ObjectProvider;

/**
 * Normally, resources take precedence over annotations when injecting. The Inject annotation
 * overrides this default, forcing the resolution of the parameters value via the master
 * {@link ObjectProvider}, even when the parameter's type matches a type that is normally a
 * resource. This is most often used in conjunction with {@link Value} annotation when injecting a
 * string, as normally, the String would be matched as the service id.
 */
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
public @interface Inject
{

}
