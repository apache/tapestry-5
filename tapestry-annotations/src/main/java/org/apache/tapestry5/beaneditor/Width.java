// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.beaneditor;

import java.lang.annotation.*;

/**
 * Defines the desired width of the field used to edit the property.  Note that width (generally equivalent to the size
 * attribute of an HTML &lt;input&gt; element) is only used for presentation; validation must be used to actually
 * enforce a maximum input length.
 * <p/>
 * <p/>
 * May be placed on an getter or setter method, or on the matching field.
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Width
{
    /**
     * The width used to display the field for the property (values less than one indicate unspecified).
     */
    int value();
}
