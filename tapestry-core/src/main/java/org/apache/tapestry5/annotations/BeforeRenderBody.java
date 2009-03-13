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
 * Marks methods to be invoked when the component rendering state machine hits the point in the component's template
 * where the body element occurs. Such methods may optionally take a {@link org.apache.tapestry5.MarkupWriter}
 * parameter, and may return void or boolean.
 * <p/>
 * Returning true (or void) will queue up the component's body for rendering.
 * <p/>
 * Returning false will skip the component's body, but continue rendering the template. The {@link
 * org.apache.tapestry5.annotations.AfterRenderBody} phase will still execute after the template finishes rendering.
 * <p/>
 * This phase is skipped for components which do not have a body.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
public @interface BeforeRenderBody
{

}
