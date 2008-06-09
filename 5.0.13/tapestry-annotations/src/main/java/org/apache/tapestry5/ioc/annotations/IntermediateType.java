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

package org.apache.tapestry5.ioc.annotations;

import java.lang.annotation.*;


/**
 * Used to guide Tapestry when coercing from a raw type to a field or parameter type, by forcing Tapestry to coerce to
 * the intermediate type.  This was introduced to allow coercion from string to a time period (in milliseconds) via
 * {@link org.apache.tapestry5.ioc.util.TimeInterval}.
 *
 * @see org.apache.tapestry5.ioc.annotations.Value
 * @see org.apache.tapestry5.ioc.annotations.Symbol
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IntermediateType
{
    /**
     * The intermediate to coerce through.
     */
    Class value();
}
