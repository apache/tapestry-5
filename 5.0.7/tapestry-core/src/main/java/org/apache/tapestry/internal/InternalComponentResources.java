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

package org.apache.tapestry.internal;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.PersistentFieldManager;

/**
 * An extension of {@link org.apache.tapestry.ComponentResources} that represents additional methods
 * that are private to the framework and not exposed in any public APIs. Ideally, there will not be
 * any need for this interface (we'll see as we go).
 */
public interface InternalComponentResources extends ComponentResources, InternalComponentResourcesCommon
{
    /**
     * Reads the value of a parameter, via the parameter's {@link org.apache.tapestry.Binding}.
     *
     * @param <T>
     * @param parameterName the name of the parameter to read
     * @param expectedType  the expected type of parameter
     * @return the value for the parameter, or null if the parameter is not bound.
     */
    <T> T readParameter(String parameterName, Class<T> expectedType);

    /**
     * Updates a parameter. It is an error to update a parameter which is not bound. The parameter
     * {@link org.apache.tapestry.Binding binding} may also not support updates.
     *
     * @param <T>
     * @param parameterName  of parameter to update
     * @param parameterValue new value (which may be null)
     */
    <T> void writeParameter(String parameterName, T parameterValue);

    /**
     * Returns true if the named parameter's {@link org.apache.tapestry.Binding} is invariant, false
     * if otherwise, or if the parameter is not bound. Invariant bindings are cached more
     * aggressively than variant bindings.
     *
     * @param parameterName the name of parameter to check for invariance
     * @return
     */
    boolean isInvariant(String parameterName);

    /**
     * Posts a change to a persistent field. If the component is still loading, then this change is
     * ignored. Otherwise, it is propagated, via the
     * {@link Page#persistFieldChange(org.apache.tapestry.ComponentResources, String, Object) page}
     * to the {@link PersistentFieldManager}.
     */
    void persistFieldChange(String fieldName, Object newValue);
}
