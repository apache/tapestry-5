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
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Attached to a module class, this annotation identifies other module classes that should also be added to the
 * Registry. This is often easier than updating the JAR Manifest.
 *
 * @deprecated Deprecated in 5.4, use {@link org.apache.tapestry5.ioc.annotations.ImportModule} instead.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@UseWith(AnnotationUseContext.MODULE)
public @interface SubModule
{

    /**
     * One or more classes that are also modules and should also be loaded.
     */
    Class[] value();
}
