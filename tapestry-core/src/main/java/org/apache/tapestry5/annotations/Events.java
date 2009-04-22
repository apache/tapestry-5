// Copyright 2009 The Apache Software Foundation
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

import java.lang.annotation.*;

/**
 * Annotation applied to components to document what events a component may trigger. This is primarily used when
 * generating component reference documentation. It is expressly <em>not</em> checked used at runtime.
 *
 * @since 5.1.0.4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface Events
{
    /**
     * A number of strings that define the events; each string is an event name optionally followed by whitespace and
     * documentation about when the event is triggered.
     */
    String[] value();
}
