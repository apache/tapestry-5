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
package org.apache.tapestry5.annotations;

import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Disables strict checks Tapestry-IoC or Tapestry may perform on methods. So far, this annotation
 * can be used in Tapestry event handler methods to disable the check on whether they match
 * a component id which actually exists.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(METHOD)
public @interface DisableStrictChecks 
{

}
