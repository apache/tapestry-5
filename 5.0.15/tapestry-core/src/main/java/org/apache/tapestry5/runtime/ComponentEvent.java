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

package org.apache.tapestry5.runtime;

import org.apache.tapestry5.ComponentResourcesCommon;
import org.apache.tapestry5.EventContext;

/**
 * An event that may originate in application logic, or as a result of a client interaction (a GET or POST from the
 * client).
 *
 * @see ComponentResourcesCommon#triggerEvent(String, Object[], org.apache.tapestry5.ComponentEventCallback)
 * @see org.apache.tapestry5.ComponentEventCallback
 */
public interface ComponentEvent extends Event
{
    /**
     * Returns true if the event matches the provided criteria.
     *
     * @param eventType      the type of event (case insensitive match)
     * @param componentId    component is to match against (case insensitive), or the empty string
     * @param parameterCount minimum number of context values
     * @return true if the event matches.
     */
    boolean matches(String eventType, String componentId, int parameterCount);

    /**
     * Coerces a context value to a particular type. The context is an array of objects; typically it is an array of
     * strings of extra path information encoded into the action URL.
     *
     * @param index           the index of the context value
     * @param desiredTypeName the desired type
     * @return the coerced value (a wrapper type if the desired type is a primitive)
     */
    Object coerceContext(int index, String desiredTypeName);

    /**
     * Returns the underlying {@link org.apache.tapestry5.EventContext} as a (possibly empty) array.
     */
    Object[] getContext();

    /**
     * Returns the underlying event context.
     */
    EventContext getEventContext();
}
