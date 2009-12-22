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

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Used in conjunction with {@link org.apache.tapestry5.ioc.annotations.Inject} to inject a literal value, rather than a
 * service. Symbols in the value are expanded and the resulting string is coerced to the desired type. For IoC, this
 * annotation is only applied to parameters (on service builder methods, and on service constructors); for components,
 * it may also be applied to field.
 *
 * @see org.apache.tapestry5.ioc.services.SymbolSource
 * @see org.apache.tapestry5.ioc.services.TypeCoercer
 */
@Target(
        { PARAMETER, FIELD })
@Retention(RUNTIME)
@Documented
public @interface Value
{
    /**
     * The value to be coerced and injected.
     */
    String value();
}
