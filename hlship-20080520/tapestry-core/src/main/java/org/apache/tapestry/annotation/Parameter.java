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

package org.apache.tapestry.annotation;

import org.apache.tapestry.BindingConstants;
import org.apache.tapestry.services.BindingFactory;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Annotation placed on a field to indicate that it is, in fact, an parameter. Parameters may be optional or required.
 * Required parameters must be bound.
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface Parameter
{

    /**
     * The name of the parameter. If not specified, the name of the parameter is derived from the name of the field
     * (after stripping off leading punctuation) from the field name.
     */
    String name() default "";

    /**
     * If true, the parameter is required and and must be bound. If false (the default), then the parameter is
     * optional.
     */
    boolean required() default false;

    /**
     * If true (the default), then the value for the parameter is cached while the component is, itself, rendering.
     * Values from invariant bindings (such as literal strings) are always cached, regardless of this setting. Set this
     * attribute to false to force the parameter to be {@link org.apache.tapestry.Binding#get() re-read} every time the
     * field is accessed, even while the component is rendering.
     */
    boolean cache() default true;

    /**
     * The default value for the parameter if not bound (at not the empty string). This is a binding expression,
     * typically the name of a property of the component to bind.
     */
    String value() default "";

    /**
     * The default binding prefix for the parameter, if no specific binding prefix is provided with the binding. There
     * is <em>rarely</em> a reason to override this. Typically, non-standard default binding prefixes are paired with
     * specific {@link BindingFactory} implementations, and used with parameters whose name reflects the binding
     * prefix.
     *
     * @see org.apache.tapestry.BindingConstants
     */
    String defaultPrefix() default BindingConstants.PROP;

    /**
     * Used to mark a parameter as requiring earlier initialization than other parameters. This is used when default
     * bindings for secondary parameters rely on a principal parameter, which itself may have a default value. This
     * ensures that the binding for the principal parameter(s) are initialized, possibly involving a defaulter method,
     * before the secondary parameters are initialized (as they may need to know if the principal parameter is bound,
     * and what type of value it is bound to). This is rarely used, and it is highly unlikely a single component would
     * have more than a single principal parameter.
     */
    boolean principal() default false;
}
