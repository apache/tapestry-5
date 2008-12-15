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

package org.apache.tapestry5.ioc.annotations;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * This annotation serves is something of the Swiss Army knife for operations related to injection of dependencies into
 * an arbitrary method of Java Bean.
 * <p/>
 * <p>It marks parameters that should be injected in the IoC container, and it marks fields that should be injected
 * inside Tapestry components.
 * <p/>
 * In terms of the IoC container; normally, resources take precedence over annotations when injecting. The Inject
 * annotation overrides this default, forcing the resolution of the parameters value via the master {@link
 * org.apache.tapestry5.ioc.ObjectProvider}, even when the parameter's type matches a type that is normally a resource.
 * This is most often used in conjunction with {@link org.apache.tapestry5.ioc.annotations.Value} annotation when
 * injecting a string, as normally, the String would be matched as the service id.
 * <p/>
 * For service implementations, module classes, and other objects constructed via  {@link
 * org.apache.tapestry5.ioc.ObjectLocator#autobuild(Class)}, this annotation indicates that an injection is desired on
 * the field, as with fields of a Tapestry component.
 * <p/>
 * In terms of the IoC container, the Inject annotation is only used on parameters to service builder methods (and
 * contributor and decorator methods) and on module class constructors. constructors. However, inside Tapestry
 * components (<em>and only inside components</em>), it may be applied to fields. On fields that require injection, the
 * Inject annotation is <em>required</em>.
 * <p/>
 * Finally, on a constructor, this is used to indicate <em>which</em> constructor should be used when more than one is
 * available.
 *
 * @see org.apache.tapestry5.ioc.ObjectProvider
 */
@Target(
        {PARAMETER, FIELD, CONSTRUCTOR})
@Retention(RUNTIME)
@Documented
public @interface Inject
{

}
