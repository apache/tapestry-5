// Copyright 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.services.transform;

import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.services.ComponentEventHandler;

/**
 * Additional utilities, beyond {@link PlasticClass}, needed when transforming.
 *
 * @since 5.3
 */
public interface TransformationSupport
{
    /**
     * @param typeName Java type name (which may be a primitive type or array, or fully qualified class name)
     * @return corresponding Java Class
     */
    Class toClass(String typeName);

    /**
     * Returns true if the class being transformed is a root class: it does not inherit
     * from another transformed class, but instead inherits from Object.
     *
     * @return true if root
     */
    boolean isRootTransformation();

    /**
     * Adds an event handler. Added event handlers execute <em>before</em> calls to super-class event handlers,
     * or calls to event handler methods.
     *
     * @param eventType            type of event
     * @param minContextValues     number of context values required to activate the handler
     * @param operationDescription Used with {@link org.apache.tapestry5.ioc.OperationTracker} when invoking the handler
     * @param handler              code to execute when the event matches
     */
    void addEventHandler(String eventType, int minContextValues, String operationDescription, ComponentEventHandler handler);

}
