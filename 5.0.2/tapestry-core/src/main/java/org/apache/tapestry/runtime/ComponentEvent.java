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

package org.apache.tapestry.runtime;

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ComponentResourcesCommon;

/**
 * An event that may originate in application logic, or as a result of a client interaction (a GET
 * or POST from the client).
 * 
 * @see ComponentResourcesCommon#triggerEvent(String, Object[],
 *      org.apache.tapestry.ComponentEventHandler)
 * @see ComponentEventHandler
 */
public interface ComponentEvent extends Event
{
    /**
     * Returns true if the component event's type matches any of the provided values. Comparison is
     * caseless.
     * 
     * @param eventTypes
     * @return true if there is any match
     */
    boolean matchesByEventType(String[] eventTypes);

    /**
     * Returns true if the originating component matches any of the components identified by their
     * ids. This filter is only relevent in the immediate container of the originating component (it
     * will never match at higher levels). Comparison is caseless.
     */
    boolean matchesByComponentId(ComponentResources resources, String[] componentId);

    /**
     * Coerces a context value to a particular type. The context is an array of objects; typically
     * it is an array of strings of extra path information encoded into the action URL.
     * 
     * @param <T>
     * @param index
     *            the index of the context value
     * @param desiredTypeName
     *            the desired type
     * @param methodDescription
     *            the method for which the conversion will take place (used if reporting an error)
     * @return the coerced value (a wrapper type if the desired type is a primitive)
     */
    Object coerceContext(int index, String desiredTypeName);

    /** Returns the raw context as a (possibly empty) array. */
    Object[] getContext();
}