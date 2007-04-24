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

package org.apache.tapestry.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marker annotation for component methods associated with the terminal phase for the component
 * rendering state machine. Methods may optionally take a {@link org.apache.tapestry.MarkupWriter}
 * annotation. Generally, methods marked with this annotation are used to perform post-render
 * cleanup. In addition, a method may return true to return to the
 * {@link org.apache.tapestry.annotations.SetupRender} phase. Returning false, or void, is the
 * normal course.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
public @interface CleanupRender {

}
