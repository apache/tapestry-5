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

import org.apache.tapestry5.services.Session;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Identifies a field as persistent, meaning its value persists from one request to the next. Different strategies exist
 * for how this is accomplished, the most common being the default, "session", which stores the field's value in the
 * {@link Session}.
 * <p/>
 * In most cases, the value will be omitted and will default to the empty string. This forces a search for the correct
 * strategy. Starting with the component (or mixin) itself, a check is made for the {@link Meta meta data property}
 * <code>tapestry.persistence-strategy</code>. If a value is found, it is used, otherwise the search continues up the
 * inheritance hierarchy, towards the page. If not found, then the "session" strategy is used.
 * <p/>
 * In this way, the session persistence strategy for a component and all of its sub-components can be controlled by the
 * containing component.
 *
 * @see org.apache.tapestry5.services.MetaDataLocator
 * @see org.apache.tapestry5.PersistenceConstants
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface Persist
{

    /**
     * The strategy used to persist the value. The default value, the empty string, allows persistence to be decided by
     * the containing component and component hierarchy.
     */
    String value() default "";
}
