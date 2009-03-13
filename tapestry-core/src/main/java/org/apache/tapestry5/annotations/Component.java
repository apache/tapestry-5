// Copyright 2006, 2008, 2009 The Apache Software Foundation
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
 * Used to define an <em>embedded component</em> within another component.
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface Component
{

    /**
     * The id of the component. When left blank (the default), the component id is determined from the field name.
     */
    String id() default "";

    /**
     * The component type. When this is left unspecified, then the annotated field's type is used directly as the
     * component type.
     */
    String type() default "";

    /**
     * Parameter bindings for the component. Each value in the array is of the form "name=value". The value is a binding
     * expression, with a default binding prefix of "prop:".
     */
    String[] parameters() default { };

    /**
     * If true, then the component will inherit all informal parameters from its parent component. The default is
     * false.
     */
    boolean inheritInformalParameters() default false;

    /**
     * A comma-separated list of parameters of the component that should be published as parameters of the containing
     * component.   Binding the parameter of the outer component will bind the inner component's parameter, as with the
     * "inhert:" binding prefix.
     *
     * @since 5.1.0.0
     */
    String publishParameters() default "";
}
