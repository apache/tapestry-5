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
 * Marker annotation for methods that should be executed during the SetupRender phase. Such methods may optionally take
 * a {@link org.apache.tapestry5.MarkupWriter} parameter, and may return void or boolean. Returning true or void will
 * advance to the {@link org.apache.tapestry5.annotations.BeginRender} phase. Return false to skip the BeginRender phase
 * and procede directly to the {@link org.apache.tapestry5.annotations.CleanupRender} phase.
 */
@Target(ElementType.METHOD)
@Retention(RUNTIME)
@Documented
public @interface SetupRender
{

}
