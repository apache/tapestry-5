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
 * Annotation for fields for which accessor methods (getters and setters) should be created.  This can help when
 * defining the kind of placeholder properties often used in components, though the disadvantage is that you can't
 * access the fields in a unit test, and you may get compiler warnings about unused private variables.
 * <p/>
 * The annotation <em>will not</em> overwrite an existing getter or setter method; if you put a Property annotation on a
 * field that already has a getter or a setter you will see a runtime exception.
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface Property
{
    /**
     * Whether to create a readable property (i.e., generate a getter method).
     */
    boolean read() default true;

    /**
     * Whether to create a writeable property (i.e., generate a setter method).
     */
    boolean write() default true;
}
