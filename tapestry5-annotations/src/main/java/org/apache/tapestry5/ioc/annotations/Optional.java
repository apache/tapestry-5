// Copyright 2011, 2012 The Apache Software Foundation
//
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

package org.apache.tapestry5.ioc.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks a service contribution method within a module as being optional: it is not an error if the contribution does not match against an actual service. In that case, the
 * method will simply never be invoked. This is occasionally useful when a module is designed to work with another module <em>if the second module is present</em>. Without
 * optional contributions, you would see hard errors when registry is created, and have to create a layer cake of small modules to prevent such errors.
 *
 * @see Contribute
 * @see org.apache.tapestry5.ioc.def.ContributionDef3#isOptional()
 * @since 5.3
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
@UseWith(AnnotationUseContext.MODULE)
public @interface Optional
{
}
