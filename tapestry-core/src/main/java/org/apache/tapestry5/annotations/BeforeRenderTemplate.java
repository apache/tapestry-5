// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Associated with components that have a template, this phase is invoked to allow the component to decorate its
 * template with additional markup. Returning true will cause the component's template to render (possibly including
 * additional components, or this component's body), and eventually reach the {@link AfterRenderTemplate} phase. Return
 * false to skip the template and body, and jump directly to the {@link AfterRenderTemplate} phase.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
public @interface BeforeRenderTemplate
{

}
