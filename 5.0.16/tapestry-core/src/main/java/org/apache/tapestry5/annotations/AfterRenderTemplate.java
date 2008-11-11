// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
 * Corresponds to {@link BeforeRenderTemplate}, allowing additional markup after rendering the component's template.
 * Returning true (the default), will progress to the {@link AfterRender} phase. Return false to return to the {@link
 * BeforeRenderTemplate} phase.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
public @interface AfterRenderTemplate
{

}
