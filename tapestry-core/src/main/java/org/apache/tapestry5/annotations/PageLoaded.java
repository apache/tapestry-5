// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use tis file except in compliance with the License.
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

import org.apache.tapestry5.ioc.annotations.UseWith;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.*;

/**
 * Method annotation used for methods that should be invoked once the page is fully loaded. This is useful for one-time
 * component initializations that can't be done at instance initialization time, such as references to embedded
 * components or blocks.
 *
 * PageLoaded methods should take no parameters and return void. They must either have this annotation, or be named
 * "pageLoaded".
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
@UseWith({COMPONENT,MIXIN,PAGE})
public @interface PageLoaded
{

}
