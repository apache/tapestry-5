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

package org.apache.tapestry.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.commons.logging.Log;
import org.apache.tapestry.Asset;
import org.apache.tapestry.Block;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.Messages;

/**
 * Allows injection of various objects into a component class. In certain cases, the type of the
 * field guides the interpretation of the value. For {@link Asset}, the value is the path, relative
 * to the component's class file, to the resource from which the Asset is obtained.
 * <p>
 * In most other cases, the value is an object reference. A common example:
 * 
 * <pre>
 * @Inject(&quot;infrastructure:Request&quot;)
 * private Request _request;
 * </pre>
 * 
 * <p>
 * There is a complex relationship between the type of the field and the interpretation of the
 * annotation and its value.
 * <p>
 * For type {@link Asset}, the value is a relative path to the asset.
 * <p>
 * For type {@link Block}, the value is the id of a block in the component's template. When the
 * value is omitted, it is deduced from the field name.
 * <p>
 * Finally, for certain specific types, the value is omitted entirely, and an object appropriate to
 * the component instance is injected. This includes {@link ComponentResources}, Locale (for the
 * containing page's locale), {@link Messages} (the component's message catalog), {@link Log} to log
 * errors or debugging data, and String for the component's complete id.
 * <p>
 * And if nothing else matches and the value is omitted, then a search for Tapestry IOC service
 * implementing the interface defined by the fields type occurs.
 * @see org.apache.tapestry.services.InjectionProvider
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface Inject {

    /**
     * Identifies the value to be injected, when the type by itself is insufficient. Omitted in many
     * cases.
     */
    String value() default "";
}
