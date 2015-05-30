// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.runtime.ComponentEvent;

/**
 * Supplies one parameter value when invoking a component event handler method. In general,
 * this involves extracting a value form the event's context and coercing it to a type
 * appropriate to the parameter.
 *
 * These values are accumulated and used to invoke the event handler method.
 *
 * @since 5.2.0
 */
public interface EventHandlerMethodParameterProvider
{
    /**
     * Extract the value and coerce it to the correct type.
     *
     * @return value that can be passed as a method parameter
     */
    Object valueForEventHandlerMethodParameter(ComponentEvent event);
}
