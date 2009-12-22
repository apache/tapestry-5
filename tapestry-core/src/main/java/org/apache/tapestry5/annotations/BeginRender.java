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

package org.apache.tapestry5.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marker annotation for methods that should be executed at the start of rendering the component. This usually includes
 * rendering of the component's start tag.
 * <p/>
 * Such methods may optionally take a {@link org.apache.tapestry5.MarkupWriter} parameter, and may return void or
 * boolean. Returning true or void will allow the component to advance into the render template / render body phase. If
 * a body is present, the {@link org.apache.tapestry5.annotations.BeforeRenderBody} phase will execute. If a component
 * has a template, the {@link BeforeRenderTemplate} phase will execute (and the render body will only occur if the
 * template directs so).
 * <p/>
 * Either way, the {@link org.apache.tapestry5.annotations.AfterRender} phase will execute after the template and/or
 * body have rendered. A component with a body but without a template will still see the {@link
 * org.apache.tapestry5.annotations.BeforeRenderBody} phase execute.
 * <p/>
 * Returning false will skip rendering of the template and/or body, and jump directly to the {@link AfterRender} phase.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
public @interface BeginRender
{

}
