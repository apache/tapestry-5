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

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An optional annotation that may be placed on a service building method of a module, or on the implementation class
 * (when using service binding). The annotation overrides the default scope for services (the default being a global
 * singleton that is instantiated on demand) for an alternate lifecycle. Alternate lifecycles are typically used to bind
 * a service implementation to a single thread or request. Modules may define new scopes. Each scope should have a
 * corresponding <a href="https://tapestry.apache.org/current/apidocs/org/apache/tapestry5/ioc/ServiceLifecycle.html">ServiceLifecycle</a> implementation. The linkage from scope name to service lifecycle occurs via a
 * contribution to the <a href="https://tapestry.apache.org/current/apidocs/org/apache/tapestry5/ioc/services/ServiceLifecycleSource.html">ServiceLifecycleSource</a> service configuration.
 *
 * The annotation may also be placed directly on a service implementation class, when using service binding (via
 * the <a href="https://tapestry.apache.org/current/apidocs/org/apache/tapestry5/ioc/ServiceBinder.html">ServiceBinder</a>).
 *
 * @see <a href="https://tapestry.apache.org/current/apidocs/org/apache/tapestry5/ioc/ServiceBinder.html">ScopeConstants</a>.
 */
@Target(
        {TYPE, METHOD})
@Retention(RUNTIME)
@Documented
@UseWith({AnnotationUseContext.SERVICE, AnnotationUseContext.MODULE})
public @interface Scope
{
    /**
     * An identifier used to look up a non-default <a href="https://tapestry.apache.org/current/apidocs/org/apache/tapestry5/ioc/ServiceBinder.html">ServiceLifecycle</a>.
     */
    String value();
}
