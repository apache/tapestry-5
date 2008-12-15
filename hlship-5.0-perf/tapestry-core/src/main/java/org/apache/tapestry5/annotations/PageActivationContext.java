// Copyright 2008 The Apache Software Foundation
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
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Annotation for a field for which the page activation context handlers (onActivate and onPassivate) should be created.
 * In order to use this annotation you must contribute a {@link org.apache.tapestry5.ValueEncoder} for the class of the
 * annotated property.
 * <p/>
 * You should not use this annotation within a class that already has an onActivate() or onPassivate() method; doing so
 * will result in a runtime exception.
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface PageActivationContext
{
    /**
     * Whether to create an activate event handler.
     */
    boolean activate() default true;

    /**
     * Whether to create a passivate event handler
     */
    boolean passivate() default true;
}
